package com.example.BuildTwin._0.config;

import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ZoneRepository zoneRepository;
    private final WorkPackageRepository workPackageRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    // Exactly the 10 Stakeholder & User Roles from BuildTwin 360 SRS Section 7
    private static final List<String> EXACT_SPECIFICATION_ROLES = List.of(
            "ROLE_DIRECTOR",                   // 1. Director / Management
            "ROLE_PROJECT_MANAGER",            // 2. Project Manager
            "ROLE_SITE_ENGINEER",              // 3. Site Engineer
            "ROLE_SITE_SUPERVISOR",            // 4. Site Supervisor
            "ROLE_PROCUREMENT_STORE",          // 5. Procurement / Store
            "ROLE_QUANTITY_COST_COORDINATOR",  // 6. Quantity / Cost Coordinator
            "ROLE_QUALITY_ENGINEER",           // 7. Quality Engineer
            "ROLE_DATA_ANALYST",               // 8. Data / Management Analyst
            "ROLE_ADMIN",                      // 9. System Administrator
            "ROLE_AUDITOR"                     // 10. Auditor / Reviewer
    );

    @Override
    public void run(String... args) {
        fixUserRolesTableConstraints();
        cleanAndSyncExactRoles();
        seedOrUpdateAdminUser();
        seedDefaultProjectAndSites();
        seedDefaultWbsActivities();
    }

    private void fixUserRolesTableConstraints() {
        try {
            // Set default timestamp and drop not-null on created_at in user_roles join table
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at DROP NOT NULL;");
            log.info("Updated 'user_roles' table constraints for created_at.");
        } catch (Exception e) {
            log.debug("Database constraint adjustment on user_roles skipped: {}", e.getMessage());
        }
    }

    /**
     * Seeds strictly the 10 roles specified in BuildTwin 360 SRS Section 7
     * and removes any non-specification roles.
     */
    private void cleanAndSyncExactRoles() {
        // 1. Delete user role associations for any extra roles first
        try {
            jdbcTemplate.execute(
                    "DELETE FROM user_roles WHERE role_id IN (" +
                    "   SELECT id FROM roles WHERE name NOT IN (" +
                    "       'ROLE_DIRECTOR', 'ROLE_PROJECT_MANAGER', 'ROLE_SITE_ENGINEER', " +
                    "       'ROLE_SITE_SUPERVISOR', 'ROLE_PROCUREMENT_STORE', 'ROLE_QUANTITY_COST_COORDINATOR', " +
                    "       'ROLE_QUALITY_ENGINEER', 'ROLE_DATA_ANALYST', 'ROLE_ADMIN', 'ROLE_AUDITOR'" +
                    "   )" +
                    ");"
            );
            // 2. Delete the extra roles from roles table
            jdbcTemplate.execute(
                    "DELETE FROM roles WHERE name NOT IN (" +
                    "   'ROLE_DIRECTOR', 'ROLE_PROJECT_MANAGER', 'ROLE_SITE_ENGINEER', " +
                    "   'ROLE_SITE_SUPERVISOR', 'ROLE_PROCUREMENT_STORE', 'ROLE_QUANTITY_COST_COORDINATOR', " +
                    "   'ROLE_QUALITY_ENGINEER', 'ROLE_DATA_ANALYST', 'ROLE_ADMIN', 'ROLE_AUDITOR'" +
                    ");"
            );
        } catch (Exception e) {
            log.debug("Roles table cleanup skipped: {}", e.getMessage());
        }

        // 3. Ensure all 10 standard roles exist
        for (String roleName : EXACT_SPECIFICATION_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Initialized SRS Section 7 Role: {}", roleName);
            }
        }
    }

    private void seedOrUpdateAdminUser() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role directorRole = roleRepository.findByName("ROLE_DIRECTOR")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_DIRECTOR").build()));

        userRepository.findByUsername("admin").ifPresentOrElse(
                existingAdmin -> {
                    boolean hasAdmin = existingAdmin.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
                    if (!hasAdmin) {
                        existingAdmin.getRoles().add(adminRole);
                        existingAdmin.getRoles().add(directorRole);
                        userRepository.save(existingAdmin);
                        log.info("Granted ROLE_ADMIN & ROLE_DIRECTOR to existing 'admin' user");
                    }
                },
                () -> {
                    User adminUser = User.builder()
                            .username("admin")
                            .email("admin@buildtwin360.com")
                            .password(passwordEncoder.encode("Admin@123"))
                            .status("ACTIVE")
                            .roles(new HashSet<>(Set.of(adminRole, directorRole)))
                            .build();

                    userRepository.save(adminUser);
                    log.info("Initialized default administrator: username='admin', email='admin@buildtwin360.com'");
                }
        );
    }

    private void seedDefaultProjectAndSites() {
        String defaultCode = "PADUR-AG-01";
        if (!projectRepository.existsByCode(defaultCode)) {
            User admin = userRepository.findByUsername("admin").orElse(null);

            Project project = Project.builder()
                    .name("Ashok Grandeur - Padur, Chennai")
                    .code(defaultCode)
                    .description("Flagship 18-storey twin-tower residential community with 220 luxury units and club facilities in Padur, OMR, Chennai.")
                    .clientName("Ashok Builders & Developers")
                    .projectType("RESIDENTIAL")
                    .location("Old Mahabalipuram Road (OMR), Padur, Chennai - 603103")
                    .status("ACTIVE")
                    .plannedStartDate(LocalDate.of(2026, 9, 1))
                    .plannedEndDate(LocalDate.of(2028, 6, 30))
                    .actualStartDate(LocalDate.of(2026, 9, 5))
                    .estimatedBudget(BigDecimal.valueOf(45000000.00))
                    .currency("INR")
                    .totalBuiltUpAreaSqFt(350000.0)
                    .projectManagerId(admin != null ? admin.getId() : null)
                    .build();

            Project saved = projectRepository.save(project);
            log.info("Initialized master construction project: '{}' ({})", saved.getName(), saved.getCode());

            Site siteA = Site.builder()
                    .project(saved)
                    .code("PADUR-TWR-A")
                    .name("Tower A (Stilt + 18 Floors)")
                    .siteType("BUILDING_TOWER")
                    .location("North Sector, Ashok Grandeur Campus, Padur")
                    .status("ACTIVE")
                    .latitude(12.7932)
                    .longitude(80.2241)
                    .areaSqFt(180000.0)
                    .siteIncharge("Karthik Raman (PM)")
                    .build();

            Site siteB = Site.builder()
                    .project(saved)
                    .code("PADUR-TWR-B")
                    .name("Tower B (Stilt + 18 Floors)")
                    .siteType("BUILDING_TOWER")
                    .location("South Sector, Ashok Grandeur Campus, Padur")
                    .status("ACTIVE")
                    .latitude(12.7935)
                    .longitude(80.2245)
                    .areaSqFt(150000.0)
                    .siteIncharge("Suresh Kumar (Site Eng)")
                    .build();

            Site siteClub = Site.builder()
                    .project(saved)
                    .code("PADUR-CLUB-01")
                    .name("Clubhouse & Podium Amenities")
                    .siteType("AMENITIES")
                    .location("Central Podium, Ashok Grandeur Campus")
                    .status("ACTIVE")
                    .latitude(12.7930)
                    .longitude(80.2238)
                    .areaSqFt(20000.0)
                    .siteIncharge("Anand (QC Lead)")
                    .build();

            siteRepository.saveAll(List.of(siteA, siteB, siteClub));
            log.info("Initialized 3 physical sites under project '{}'", saved.getName());

            // 1. Seed Building
            Building bldTowerA = Building.builder()
                    .site(siteA)
                    .code("BLD-TWR-A")
                    .name("Tower A - Premium Suites")
                    .buildingType("RESIDENTIAL_TOWER")
                    .totalFloors(18)
                    .totalBuiltUpAreaSqFt(180000.0)
                    .status("UNDER_CONSTRUCTION")
                    .description("18-storey residential tower with stilt parking and terrace garden")
                    .build();
            Building savedBld = buildingRepository.save(bldTowerA);

            // 2. Seed Floors
            Floor stiltFloor = Floor.builder()
                    .building(savedBld)
                    .floorNumber(0)
                    .floorName("Stilt Floor (Parking & Entry)")
                    .floorType("STILT")
                    .builtUpAreaSqFt(10000.0)
                    .status("COMPLETED")
                    .build();

            Floor floor1 = Floor.builder()
                    .building(savedBld)
                    .floorNumber(1)
                    .floorName("First Typical Floor")
                    .floorType("TYPICAL")
                    .builtUpAreaSqFt(9500.0)
                    .status("IN_PROGRESS")
                    .build();

            floorRepository.saveAll(List.of(stiltFloor, floor1));

            // 3. Seed Zones under Floor 1
            Zone zone101 = Zone.builder()
                    .floor(floor1)
                    .code("FL1-UNIT-101")
                    .name("3BHK Luxury Flat 101")
                    .zoneType("RESIDENTIAL_UNIT")
                    .areaSqFt(1850.0)
                    .status("IN_PROGRESS")
                    .build();

            Zone zone102 = Zone.builder()
                    .floor(floor1)
                    .code("FL1-UNIT-102")
                    .name("3BHK Luxury Flat 102")
                    .zoneType("RESIDENTIAL_UNIT")
                    .areaSqFt(1850.0)
                    .status("IN_PROGRESS")
                    .build();

            Zone zoneCorridor = Zone.builder()
                    .floor(floor1)
                    .code("FL1-LIFT-LOBBY")
                    .name("Floor 1 Lift & Service Lobby")
                    .zoneType("COMMON_AREA")
                    .areaSqFt(800.0)
                    .status("IN_PROGRESS")
                    .build();

            zoneRepository.saveAll(List.of(zone101, zone102, zoneCorridor));

            // 4. Seed Work Packages under Project
            WorkPackage wpCivil = WorkPackage.builder()
                    .project(saved)
                    .site(siteA)
                    .code("WP-CIV-01")
                    .name("Substructure & RCC Framing Works")
                    .discipline("CIVIL")
                    .description("RCC columns, beams, and slab casting up to 10th floor")
                    .status("IN_PROGRESS")
                    .plannedStartDate(LocalDate.of(2026, 9, 1))
                    .plannedEndDate(LocalDate.of(2027, 4, 30))
                    .actualStartDate(LocalDate.of(2026, 9, 10))
                    .budgetAmount(BigDecimal.valueOf(18000000.00))
                    .assignedContractor("L&T Construction (Civil Div)")
                    .inchargeUserId(admin != null ? admin.getId() : null)
                    .build();

            WorkPackage wpMep = WorkPackage.builder()
                    .project(saved)
                    .site(siteA)
                    .code("WP-MEP-01")
                    .name("MEP - Plumbing, Conduit & Fire Piping")
                    .discipline("MEP")
                    .description("Conduit laying, soil/waste piping, and fire hydrant pipelines")
                    .status("PLANNED")
                    .plannedStartDate(LocalDate.of(2026, 11, 1))
                    .plannedEndDate(LocalDate.of(2027, 8, 31))
                    .budgetAmount(BigDecimal.valueOf(8500000.00))
                    .assignedContractor("Voltas MEP Solutions")
                    .inchargeUserId(admin != null ? admin.getId() : null)
                    .build();

            workPackageRepository.saveAll(List.of(wpCivil, wpMep));
            log.info("Initialized default Buildings, Floors, Zones, and Work Packages for Padur project.");
        }
    }

    private void seedDefaultWbsActivities() {
        if (wbsActivityRepository.count() == 0) {
            projectRepository.findByCode("PADUR-AG-01").ifPresent(project -> {
                User admin = userRepository.findByUsername("admin").orElse(null);

                workPackageRepository.findByProjectIdAndCode(project.getId(), "WP-CIV-01").ifPresent(wpCivil -> {
                    Site site = wpCivil.getSite();
                    Building bld = site != null ? buildingRepository.findBySiteId(site.getId()).stream().findFirst().orElse(null) : null;
                    Floor flr1 = bld != null ? floorRepository.findByBuildingIdOrderByFloorNumberAsc(bld.getId()).stream()
                            .filter(f -> f.getFloorNumber() == 1).findFirst().orElse(null) : null;
                    Zone zn101 = flr1 != null ? zoneRepository.findByFloorId(flr1.getId()).stream()
                            .filter(z -> "FL1-UNIT-101".equalsIgnoreCase(z.getCode())).findFirst().orElse(null) : null;

                    WbsActivity act1 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .zone(zn101)
                            .code("ACT-CIV-001")
                            .name("Floor 1 Column Starter & Rebar Tying")
                            .discipline("CIVIL")
                            .description("High-tensile Fe550D rebar tying and column shuttering")
                            .uom("MT")
                            .plannedQuantity(25.0)
                            .completedQuantity(25.0)
                            .progressPercentage(100.0)
                            .plannedStartDate(LocalDate.of(2026, 9, 10))
                            .plannedEndDate(LocalDate.of(2026, 9, 25))
                            .actualStartDate(LocalDate.of(2026, 9, 12))
                            .actualEndDate(LocalDate.of(2026, 9, 24))
                            .status("COMPLETED")
                            .assignedContractor("L&T Construction (Civil Div)")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(2.0)
                            .sequenceOrder(1)
                            .build();

                    WbsActivity act2 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .code("ACT-CIV-002")
                            .name("Floor 1 Beam Formwork & Slab Concreting")
                            .discipline("CIVIL")
                            .description("M35 Grade RMC pouring for Floor 1 deck slab and drop beams")
                            .uom("CUM")
                            .plannedQuantity(240.0)
                            .completedQuantity(120.0)
                            .progressPercentage(50.0)
                            .plannedStartDate(LocalDate.of(2026, 9, 26))
                            .plannedEndDate(LocalDate.of(2026, 10, 20))
                            .actualStartDate(LocalDate.of(2026, 9, 27))
                            .status("IN_PROGRESS")
                            .assignedContractor("L&T Construction (Civil Div)")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(3.0)
                            .sequenceOrder(2)
                            .build();

                    WbsActivity act3 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .code("ACT-CIV-003")
                            .name("Floor 1 Internal AAC Blockwork Masonry")
                            .discipline("CIVIL")
                            .description("200mm and 100mm AAC blocks laying with polymer mortar")
                            .uom("SQFT")
                            .plannedQuantity(6500.0)
                            .completedQuantity(0.0)
                            .progressPercentage(0.0)
                            .plannedStartDate(LocalDate.of(2026, 10, 21))
                            .plannedEndDate(LocalDate.of(2026, 11, 25))
                            .status("PLANNED")
                            .assignedContractor("L&T Construction (Civil Div)")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(2.0)
                            .sequenceOrder(3)
                            .build();

                    wbsActivityRepository.saveAll(List.of(act1, act2, act3));
                });

                workPackageRepository.findByProjectIdAndCode(project.getId(), "WP-MEP-01").ifPresent(wpMep -> {
                    Site site = wpMep.getSite();
                    Building bld = site != null ? buildingRepository.findBySiteId(site.getId()).stream().findFirst().orElse(null) : null;
                    Floor flr1 = bld != null ? floorRepository.findByBuildingIdOrderByFloorNumberAsc(bld.getId()).stream()
                            .filter(f -> f.getFloorNumber() == 1).findFirst().orElse(null) : null;

                    WbsActivity actMep1 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpMep)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .code("ACT-MEP-001")
                            .name("Floor 1 Slab Electrical Conduit Piping")
                            .discipline("ELECTRICAL")
                            .description("PVC conduit routing and junction box placement before slab pour")
                            .uom("POINTS")
                            .plannedQuantity(350.0)
                            .completedQuantity(0.0)
                            .progressPercentage(0.0)
                            .plannedStartDate(LocalDate.of(2026, 10, 5))
                            .plannedEndDate(LocalDate.of(2026, 10, 15))
                            .status("PLANNED")
                            .assignedContractor("Voltas MEP Solutions")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(1.0)
                            .sequenceOrder(1)
                            .build();

                    wbsActivityRepository.save(actMep1);
                });

                log.info("Initialized default WBS Activities for Padur project work packages.");
            });
        }
    }
}

package com.example.BuildTwin._0.config;

import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.model.Site;
import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.domain.identity.model.Role;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Initializes exact specification roles, default admin user, and sample hierarchical project data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ZoneRepository zoneRepository;
    private final WorkPackageRepository workPackageRepository;
    private final WbsActivityRepository wbsActivityRepository;

    /**
     * Exact 10 roles defined in BuildTwin 360 SRS Document (Section 7).
     */
    private static final List<String> EXACT_SPECIFICATION_ROLES = List.of(
            "ROLE_DIRECTOR",                   // 1. Executive / Board Level
            "ROLE_PROJECT_MANAGER",            // 2. Project Execution Head
            "ROLE_SITE_ENGINEER",              // 3. Field Execution & Verification
            "ROLE_SITE_SUPERVISOR",            // 4. Daily Site Reporting & Gang Tracking
            "ROLE_PROCUREMENT_STORE",          // 5. Material & Store Management
            "ROLE_QUANTITY_COST_COORDINATOR",  // 6. QS & Cost Estimation
            "ROLE_QUALITY_ENGINEER",           // 7. QA/QC & Material Testing
            "ROLE_DATA_ANALYST",               // 8. Progress & MIS Analytics
            "ROLE_ADMIN",                      // 9. System Administrator
            "ROLE_AUDITOR"                     // 10. Auditor / Reviewer
    );

    @Override
    public void run(String... args) {
        fixUserRolesTableConstraints();
        fixWbsActivitiesTableConstraints();
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

    private void fixWbsActivitiesTableConstraints() {
        try {
            jdbcTemplate.execute("ALTER TABLE IF EXISTS wbs_activities ADD COLUMN IF NOT EXISTS wbs_level integer DEFAULT 1;");
            jdbcTemplate.execute("UPDATE wbs_activities SET wbs_level = 1 WHERE wbs_level IS NULL;");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS wbs_activities ADD COLUMN IF NOT EXISTS wbs_path varchar(500);");
            jdbcTemplate.execute("UPDATE wbs_activities SET wbs_path = '/' || id WHERE wbs_path IS NULL;");
            log.info("Ensured 'wbs_activities' hierarchy columns wbs_level and wbs_path exist and are populated.");
        } catch (Exception e) {
            log.debug("Database constraint adjustment on wbs_activities skipped: {}", e.getMessage());
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
            }
        }
        log.info("Synchronized BuildTwin 360 roles to exact {} specification roles.", EXACT_SPECIFICATION_ROLES.size());
    }

    private void seedOrUpdateAdminUser() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

        userRepository.findByUsername("admin").ifPresentOrElse(
                user -> {
                    boolean needsUpdate = false;
                    if (!passwordEncoder.matches("Admin@123", user.getPassword())) {
                        user.setPassword(passwordEncoder.encode("Admin@123"));
                        needsUpdate = true;
                    }
                    if (!user.getRoles().contains(adminRole)) {
                        user.setRoles(Set.of(adminRole));
                        needsUpdate = true;
                    }
                    if (!"ACTIVE".equals(user.getStatus())) {
                        user.setStatus("ACTIVE");
                        needsUpdate = true;
                    }
                    if (needsUpdate) {
                        userRepository.save(user);
                        log.info("Updated existing 'admin' user with correct credentials and active status.");
                    }
                },
                () -> {
                    User admin = User.builder()
                            .username("admin")
                            .email("admin@buildtwin360.com")
                            .password(passwordEncoder.encode("Admin@123"))
                            .status("ACTIVE")
                            .roles(Set.of(adminRole))
                            .build();
                    userRepository.save(admin);
                    log.info("Created default system administrator user 'admin' (password: Admin@123).");
                }
        );
    }

    private void seedDefaultProjectAndSites() {
        if (projectRepository.findByCode("PADUR-AG-01").isEmpty()) {
            User admin = userRepository.findByUsername("admin").orElse(null);

            Project project = Project.builder()
                    .name("Ashok Grandeur - Padur, Chennai")
                    .code("PADUR-AG-01")
                    .description("High-rise residential and commercial gated community spanning 12 acres with 3 residential towers and a luxury clubhouse.")
                    .clientName("Ashok Residential Infrastructure Pvt Ltd")
                    .projectType("RESIDENTIAL")
                    .status("IN_PROGRESS")
                    .location("Old Mahabalipuram Road (OMR), Padur, Chennai, Tamil Nadu - 603103")
                    .plannedStartDate(LocalDate.of(2026, 1, 15))
                    .plannedEndDate(LocalDate.of(2028, 6, 30))
                    .actualStartDate(LocalDate.of(2026, 2, 1))
                    .estimatedBudget(BigDecimal.valueOf(185000000.00))
                    .currency("INR")
                    .totalBuiltUpAreaSqFt(485000.0)
                    .projectManagerId(admin != null ? admin.getId() : null)
                    .build();

            Project saved = projectRepository.save(project);
            log.info("Initialized default Project: '{}' (ID: {})", saved.getName(), saved.getId());

            Site siteA = Site.builder()
                    .project(saved)
                    .name("Tower A (Stilt + 18 Floors)")
                    .code("SITE-TWR-A")
                    .location("Padur OMR Chennai")
                    .latitude(12.8021)
                    .longitude(80.2274)
                    .siteIncharge("R. Sundararaman - Lead Site Engineer")
                    .siteType("RESIDENTIAL_TOWER")
                    .status("ACTIVE")
                    .areaSqFt(180000.0)
                    .build();

            Site siteB = Site.builder()
                    .project(saved)
                    .name("Tower B (Stilt + 18 Floors)")
                    .code("SITE-TWR-B")
                    .location("Padur OMR Chennai")
                    .latitude(12.8025)
                    .longitude(80.2280)
                    .siteIncharge("K. Manikandan - Senior Field Engineer")
                    .siteType("RESIDENTIAL_TOWER")
                    .status("PLANNED")
                    .areaSqFt(180000.0)
                    .build();

            Site siteClub = Site.builder()
                    .project(saved)
                    .name("Grandeur Luxury Clubhouse & Amenities")
                    .code("SITE-CLUB-01")
                    .location("Padur OMR Chennai")
                    .latitude(12.8018)
                    .longitude(80.2268)
                    .siteIncharge("S. Praveen - Facilities Engineer")
                    .siteType("COMMERCIAL_AMENITY")
                    .status("ACTIVE")
                    .areaSqFt(35000.0)
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
                    .areaSqFt(900.0)
                    .status("IN_PROGRESS")
                    .build();

            zoneRepository.saveAll(List.of(zone101, zone102, zoneCorridor));

            // 4. Seed Standard Work Packages
            WorkPackage wpCivil = WorkPackage.builder()
                    .project(saved)
                    .site(siteA)
                    .code("WP-CIV-01")
                    .name("Substructure & RCC Framing Works")
                    .discipline("CIVIL")
                    .description("Excavation, raft footing, columns, beams, and slab cast works")
                    .status("IN_PROGRESS")
                    .plannedStartDate(LocalDate.of(2026, 2, 1))
                    .plannedEndDate(LocalDate.of(2027, 3, 31))
                    .actualStartDate(LocalDate.of(2026, 2, 5))
                    .budgetAmount(BigDecimal.valueOf(45000000.00))
                    .assignedContractor("L&T Construction (Civil Div)")
                    .inchargeUserId(admin != null ? admin.getId() : null)
                    .build();

            WorkPackage wpMep = WorkPackage.builder()
                    .project(saved)
                    .site(siteA)
                    .code("WP-MEP-01")
                    .name("Electrical & Plumbing (MEP) First Fix")
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

                    // 1. Parent Summary Task: RCC Structural Works (Level 1)
                    WbsActivity parentRcc = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .level(1)
                            .code("WBS-CIV-01")
                            .name("Floor 1 RCC Structural Works")
                            .discipline("CIVIL")
                            .description("Summary Task covering column rebar, shuttering, and slab concreting")
                            .uom("PERCENT")
                            .plannedQuantity(100.0)
                            .completedQuantity(70.0)
                            .progressPercentage(70.0)
                            .plannedStartDate(LocalDate.of(2026, 9, 10))
                            .plannedEndDate(LocalDate.of(2026, 10, 20))
                            .actualStartDate(LocalDate.of(2026, 9, 12))
                            .status("IN_PROGRESS")
                            .assignedContractor("L&T Construction (Civil Div)")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(5.0)
                            .sequenceOrder(1)
                            .build();
                    WbsActivity savedParentRcc = wbsActivityRepository.save(parentRcc);
                    savedParentRcc.setWbsPath("/" + savedParentRcc.getId());
                    savedParentRcc = wbsActivityRepository.save(savedParentRcc);

                    // Child 1 under parentRcc (Level 2)
                    WbsActivity act1 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .parent(savedParentRcc)
                            .level(2)
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
                    WbsActivity savedAct1 = wbsActivityRepository.save(act1);
                    savedAct1.setWbsPath(savedParentRcc.getWbsPath() + "/" + savedAct1.getId());
                    wbsActivityRepository.save(savedAct1);

                    // Child 2 under parentRcc (Level 2)
                    WbsActivity act2 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .parent(savedParentRcc)
                            .level(2)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .zone(zn101)
                            .code("ACT-CIV-002")
                            .name("Floor 1 Beam Formwork & Slab Concreting")
                            .discipline("CIVIL")
                            .description("M25 Grade ready-mix concrete pouring for suspended floor slab")
                            .uom("CUM")
                            .plannedQuantity(240.0)
                            .completedQuantity(120.0)
                            .progressPercentage(50.0)
                            .plannedStartDate(LocalDate.of(2026, 9, 26))
                            .plannedEndDate(LocalDate.of(2026, 10, 20))
                            .actualStartDate(LocalDate.of(2026, 9, 28))
                            .status("IN_PROGRESS")
                            .assignedContractor("L&T Construction (Civil Div)")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(3.0)
                            .sequenceOrder(2)
                            .build();
                    WbsActivity savedAct2 = wbsActivityRepository.save(act2);
                    savedAct2.setWbsPath(savedParentRcc.getWbsPath() + "/" + savedAct2.getId());
                    wbsActivityRepository.save(savedAct2);

                    // 2. Parent Summary Task: Masonry Works (Level 1)
                    WbsActivity parentMasonry = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .level(1)
                            .code("WBS-CIV-02")
                            .name("Floor 1 Masonry Works")
                            .discipline("CIVIL")
                            .description("AAC blockwork and internal division partition walls")
                            .uom("PERCENT")
                            .plannedQuantity(100.0)
                            .completedQuantity(0.0)
                            .progressPercentage(0.0)
                            .plannedStartDate(LocalDate.of(2026, 10, 21))
                            .plannedEndDate(LocalDate.of(2026, 11, 20))
                            .status("PLANNED")
                            .assignedContractor("Shapoorji Masonry Contractors")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(3.0)
                            .sequenceOrder(2)
                            .build();
                    WbsActivity savedParentMasonry = wbsActivityRepository.save(parentMasonry);
                    savedParentMasonry.setWbsPath("/" + savedParentMasonry.getId());
                    savedParentMasonry = wbsActivityRepository.save(savedParentMasonry);

                    // Child 1 under parentMasonry (Level 2)
                    WbsActivity act3 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpCivil)
                            .parent(savedParentMasonry)
                            .level(2)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .zone(zn101)
                            .code("ACT-CIV-003")
                            .name("Internal AAC Blockwork Masonry")
                            .discipline("CIVIL")
                            .description("200mm thick AAC block external and 100mm internal partitions")
                            .uom("SQFT")
                            .plannedQuantity(6500.0)
                            .completedQuantity(0.0)
                            .progressPercentage(0.0)
                            .plannedStartDate(LocalDate.of(2026, 10, 21))
                            .plannedEndDate(LocalDate.of(2026, 11, 20))
                            .status("PLANNED")
                            .assignedContractor("Shapoorji Masonry Contractors")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(3.0)
                            .sequenceOrder(1)
                            .build();
                    WbsActivity savedAct3 = wbsActivityRepository.save(act3);
                    savedAct3.setWbsPath(savedParentMasonry.getWbsPath() + "/" + savedAct3.getId());
                    wbsActivityRepository.save(savedAct3);
                });

                workPackageRepository.findByProjectIdAndCode(project.getId(), "WP-MEP-01").ifPresent(wpMep -> {
                    Site site = wpMep.getSite();
                    Building bld = site != null ? buildingRepository.findBySiteId(site.getId()).stream().findFirst().orElse(null) : null;
                    Floor flr1 = bld != null ? floorRepository.findByBuildingIdOrderByFloorNumberAsc(bld.getId()).stream()
                            .filter(f -> f.getFloorNumber() == 1).findFirst().orElse(null) : null;

                    // Parent Summary Task: MEP First Fix (Level 1)
                    WbsActivity parentMep = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpMep)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .level(1)
                            .code("WBS-MEP-01")
                            .name("Floor 1 Electrical Installation")
                            .discipline("MEP")
                            .description("Slab conduit laying and electrical wiring first fix")
                            .uom("PERCENT")
                            .plannedQuantity(100.0)
                            .completedQuantity(0.0)
                            .progressPercentage(0.0)
                            .plannedStartDate(LocalDate.of(2026, 11, 1))
                            .plannedEndDate(LocalDate.of(2026, 12, 15))
                            .status("PLANNED")
                            .assignedContractor("Voltas MEP Solutions")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(4.0)
                            .sequenceOrder(1)
                            .build();
                    WbsActivity savedParentMep = wbsActivityRepository.save(parentMep);
                    savedParentMep.setWbsPath("/" + savedParentMep.getId());
                    savedParentMep = wbsActivityRepository.save(savedParentMep);

                    // Child under parentMep (Level 2)
                    WbsActivity act4 = WbsActivity.builder()
                            .project(project)
                            .workPackage(wpMep)
                            .parent(savedParentMep)
                            .level(2)
                            .site(site)
                            .building(bld)
                            .floor(flr1)
                            .code("ACT-MEP-001")
                            .name("Slab Conduit & Junction Box Laying")
                            .discipline("MEP")
                            .description("Heavy duty PVC conduits embedded in RCC slab before casting")
                            .uom("RMT")
                            .plannedQuantity(1200.0)
                            .completedQuantity(0.0)
                            .progressPercentage(0.0)
                            .plannedStartDate(LocalDate.of(2026, 11, 1))
                            .plannedEndDate(LocalDate.of(2026, 11, 25))
                            .status("PLANNED")
                            .assignedContractor("Voltas MEP Solutions")
                            .inchargeUserId(admin != null ? admin.getId() : null)
                            .weightage(4.0)
                            .sequenceOrder(1)
                            .build();
                    WbsActivity savedAct4 = wbsActivityRepository.save(act4);
                    savedAct4.setWbsPath(savedParentMep.getWbsPath() + "/" + savedAct4.getId());
                    wbsActivityRepository.save(savedAct4);
                });

                log.info("Initialized default hierarchical WBS Activities (Summary tasks & child activities) for Padur project.");
            });
        }
    }
}

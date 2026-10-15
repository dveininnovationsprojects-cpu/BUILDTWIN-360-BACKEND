package com.example.BuildTwin._0.config;

import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.model.Site;
import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.domain.identity.model.Role;
import com.example.BuildTwin._0.domain.identity.model.UserProjectRole;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityRepository;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord;
import com.example.BuildTwin._0.domain.cost.repository.BudgetRepository;
import com.example.BuildTwin._0.domain.cost.repository.ProjectEVMRecordRepository;
import com.example.BuildTwin._0.domain.materials.model.Supplier;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.repository.SupplierRepository;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.enums.ContractorType;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.equipment.model.Equipment;
import com.example.BuildTwin._0.domain.equipment.model.EquipmentUsage;
import com.example.BuildTwin._0.domain.equipment.repository.EquipmentRepository;
import com.example.BuildTwin._0.domain.equipment.repository.EquipmentUsageRepository;
import com.example.BuildTwin._0.domain.issues.model.ProjectIssue;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.repository.ProjectIssueRepository;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.repository.ProjectRiskRepository;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import com.example.BuildTwin._0.domain.quality.repository.QualityIssueRepository;
import com.example.BuildTwin._0.domain.procurement.model.MaterialRequest;
import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.repository.MaterialRequestRepository;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private final ActivityRepository activityRepository;
    private final BudgetRepository budgetRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final ContractorRepository contractorRepository;
    private final LabourDailyRepository labourDailyRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentUsageRepository equipmentUsageRepository;
    private final ProjectIssueRepository projectIssueRepository;
    private final QualityIssueRepository qualityIssueRepository;
    private final ProjectRiskRepository projectRiskRepository;
    private final IssueBlockerRepository issueBlockerRepository;
    private final ProjectEVMRecordRepository projectEVMRecordRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GrnRepository grnRepository;

    @org.springframework.beans.factory.annotation.Value("${app.seed.enabled:true}")
    private boolean seedEnabled = true;

    @org.springframework.beans.factory.annotation.Value("${app.seed.admin.username:admin}")
    private String seedAdminUsername = "admin";

    @org.springframework.beans.factory.annotation.Value("${app.seed.admin.email:admin@buildtwin360.com}")
    private String seedAdminEmail = "admin@buildtwin360.com";

    @org.springframework.beans.factory.annotation.Value("${app.seed.admin.password:Admin@123}")
    private String seedAdminPassword = "Admin@123";

    @org.springframework.beans.factory.annotation.Value("${app.seed.project.name:Ashok Grandeur - Padur, Chennai}")
    private String seedProjectName = "Ashok Grandeur - Padur, Chennai";

    @org.springframework.beans.factory.annotation.Value("${app.seed.project.code:PADUR-AG-01}")
    private String seedProjectCode = "PADUR-AG-01";

    @org.springframework.beans.factory.annotation.Value("${app.seed.project.budget:45000000.00}")
    private BigDecimal seedProjectBudget = new BigDecimal("45000000.00");

    @org.springframework.beans.factory.annotation.Value("${app.seed.project.client:Ashok Builders & Developers}")
    private String seedProjectClient = "Ashok Builders & Developers";

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
        if (seedEnabled) {
            seedAllSpecificationUsers();
            seedDefaultProjectAndSites();
        } else {
            log.info("Application demo data seeding is disabled via 'app.seed.enabled=false'.");
        }
    }

    private void fixUserRolesTableConstraints() {
        try {
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at DROP NOT NULL;");
            log.info("Updated 'user_roles' table constraints for created_at.");
        } catch (Exception e) {
            log.debug("Database constraint adjustment on user_roles skipped: {}", e.getMessage());
        }
    }

    /**
     * Seeds strictly the 10 roles specified in BuildTwin 360 SRS Section 7.
     */
    private void cleanAndSyncExactRoles() {
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

        for (String roleName : EXACT_SPECIFICATION_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Initialized SRS Section 7 Role: {}", roleName);
            }
        }
    }

    /**
     * Seeds login credentials for all 10 roles according to BuildTwin 360 SRS Section 7.
     */
    private void seedAllSpecificationUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role directorRole = roleRepository.findByName("ROLE_DIRECTOR").orElseThrow();

        // 1. System Administrator & Director
        userRepository.findByUsername(seedAdminUsername).ifPresentOrElse(
                existingAdmin -> {
                    boolean hasAdmin = existingAdmin.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
                    if (!hasAdmin) {
                        existingAdmin.getRoles().add(adminRole);
                        existingAdmin.getRoles().add(directorRole);
                        userRepository.save(existingAdmin);
                    }
                },
                () -> {
                    User adminUser = User.builder()
                            .username(seedAdminUsername)
                            .email(seedAdminEmail)
                            .password(passwordEncoder.encode(seedAdminPassword))
                            .status("ACTIVE")
                            .roles(new HashSet<>(Set.of(adminRole, directorRole)))
                            .build();
                    userRepository.save(adminUser);
                    log.info("Initialized default administrator: username='{}', email='{}'", seedAdminUsername, seedAdminEmail);
                }
        );

        // 2. Specialized Users for Each SRS Role
        createOrGetUser("director_rajesh", "director@buildtwin360.com", "Director@123", "ROLE_DIRECTOR");
        createOrGetUser("pm_karthik", "pm.karthik@buildtwin360.com", "Pm@123", "ROLE_PROJECT_MANAGER");
        createOrGetUser("se_suresh", "se.suresh@buildtwin360.com", "Engineer@123", "ROLE_SITE_ENGINEER");
        createOrGetUser("qc_anand", "qc.anand@buildtwin360.com", "Quality@123", "ROLE_QUALITY_ENGINEER");
        createOrGetUser("sup_manoj", "sup.manoj@buildtwin360.com", "Supervisor@123", "ROLE_SITE_SUPERVISOR");
        createOrGetUser("proc_ramesh", "proc.ramesh@buildtwin360.com", "Procurement@123", "ROLE_PROCUREMENT_STORE");
        createOrGetUser("cost_priya", "cost.priya@buildtwin360.com", "Cost@123", "ROLE_QUANTITY_COST_COORDINATOR");
        createOrGetUser("analyst_divya", "analyst.divya@buildtwin360.com", "Analyst@123", "ROLE_DATA_ANALYST");
        createOrGetUser("contractor_lnt", "contractor.lnt@buildtwin360.com", "Contractor@123", "ROLE_SITE_SUPERVISOR");
        createOrGetUser("auditor_vignesh", "auditor.vignesh@buildtwin360.com", "Auditor@123", "ROLE_AUDITOR");
    }

    private User createOrGetUser(String username, String email, String password, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .status("ACTIVE")
                    .roles(new HashSet<>(Set.of(role)))
                    .build();
            User saved = userRepository.save(u);
            log.info("Initialized specification user: username='{}' ({})", username, roleName);
            return saved;
        });
    }

    private void seedDefaultProjectAndSites() {
        User admin = userRepository.findByUsername(seedAdminUsername).orElse(null);

        Project project;
        if (!projectRepository.existsByCode(seedProjectCode)) {
            Project newProj = Project.builder()
                    .name(seedProjectName)
                    .code(seedProjectCode)
                    .description("Flagship 18-storey twin-tower residential community with 220 luxury units and club facilities in Padur, OMR, Chennai.")
                    .clientName(seedProjectClient)
                    .projectType("RESIDENTIAL")
                    .location("Old Mahabalipuram Road (OMR), Padur, Chennai - 603103")
                    .status("ACTIVE")
                    .plannedStartDate(LocalDate.of(2026, 9, 1))
                    .plannedEndDate(LocalDate.of(2028, 6, 30))
                    .actualStartDate(LocalDate.of(2026, 9, 5))
                    .estimatedBudget(seedProjectBudget)
                    .currency("INR")
                    .totalBuiltUpAreaSqFt(350000.0)
                    .projectManagerId(admin != null ? admin.getId() : null)
                    .build();

            project = projectRepository.save(newProj);
            log.info("Initialized master construction project: '{}' ({})", project.getName(), project.getCode());

            Site siteA = Site.builder()
                    .project(project)
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
                    .project(project)
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
                    .project(project)
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

            WorkPackage wpCivil = WorkPackage.builder()
                    .project(project)
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
                    .project(project)
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
        } else {
            project = projectRepository.findByCode(seedProjectCode).orElseThrow();
        }

        // Assign Project Roles for SRS Users
        assignProjectRole(project.getId(), "pm_karthik", "ROLE_PROJECT_MANAGER");
        assignProjectRole(project.getId(), "se_suresh", "ROLE_SITE_ENGINEER");
        assignProjectRole(project.getId(), "qc_anand", "ROLE_QUALITY_ENGINEER");
        assignProjectRole(project.getId(), "cost_priya", "ROLE_QUANTITY_COST_COORDINATOR");

        // Seed Core Construction Artifacts
        Site siteA = siteRepository.findByProjectId(project.getId()).stream().findFirst().orElse(null);
        Activity actRaft = seedProjectWbsAndBudgets(project);
        seedSuppliersAndMaterials(project);
        seedProcurementData(project, siteA, actRaft);
        seedContractorsAndEquipment(project, siteA);
        seedProjectIssues(project, actRaft != null ? actRaft.getId() : null);
        seedQualityIssues(project);
        seedProjectRisks(project);
        seedIssueBlockers(project);
        seedProjectEvmSnapshot(project);
    }

    private void assignProjectRole(Long projectId, String username, String roleName) {
        userRepository.findByUsername(username).ifPresent(user -> {
            roleRepository.findByName(roleName).ifPresent(role -> {
                if (!userProjectRoleRepository.existsByUserIdAndProjectIdAndRoleId(user.getId(), projectId, role.getId())) {
                    userProjectRoleRepository.save(UserProjectRole.builder()
                            .user(user)
                            .projectId(projectId)
                            .role(role)
                            .build());
                }
            });
        });
    }

    private Activity seedProjectWbsAndBudgets(Project project) {
        List<Activity> existing = activityRepository.findByProjectId(project.getId());
        Activity raftActivity = null;

        if (existing.isEmpty()) {
            Activity act1 = Activity.builder()
                    .projectId(project.getId())
                    .wbsCode("ACT-WBS-01")
                    .name("Deep Basement Earthwork & Shoring Excavation")
                    .location("Tower A & B Footprint")
                    .unit("CU_M")
                    .plannedQty(BigDecimal.valueOf(10000.00))
                    .weightage(BigDecimal.valueOf(15.00))
                    .plannedStartDate(LocalDate.of(2026, 9, 1))
                    .plannedEndDate(LocalDate.of(2026, 10, 15))
                    .status("COMPLETED")
                    .build();

            Activity act2 = Activity.builder()
                    .projectId(project.getId())
                    .wbsCode("ACT-WBS-02")
                    .name("Raft Footing & Substructure RCC Column Casting")
                    .location("Tower A Substructure")
                    .unit("CU_M")
                    .plannedQty(BigDecimal.valueOf(5000.00))
                    .weightage(BigDecimal.valueOf(25.00))
                    .plannedStartDate(LocalDate.of(2026, 10, 1))
                    .plannedEndDate(LocalDate.of(2026, 12, 15))
                    .status("IN_PROGRESS")
                    .build();

            Activity act3 = Activity.builder()
                    .projectId(project.getId())
                    .wbsCode("ACT-WBS-03")
                    .name("Tower A 1st Floor Slab Reinforcement & Concreting")
                    .location("Tower A Floor 1")
                    .unit("SQFT")
                    .plannedQty(BigDecimal.valueOf(9500.00))
                    .weightage(BigDecimal.valueOf(20.00))
                    .plannedStartDate(LocalDate.of(2026, 11, 1))
                    .plannedEndDate(LocalDate.of(2027, 1, 30))
                    .status("IN_PROGRESS")
                    .build();

            Activity act4 = Activity.builder()
                    .projectId(project.getId())
                    .wbsCode("ACT-WBS-04")
                    .name("Internal Plumbing Riser & Electrical Conduits")
                    .location("Tower A Shafts")
                    .unit("METERS")
                    .plannedQty(BigDecimal.valueOf(1200.00))
                    .weightage(BigDecimal.valueOf(15.00))
                    .plannedStartDate(LocalDate.of(2027, 1, 1))
                    .plannedEndDate(LocalDate.of(2027, 4, 30))
                    .status("PLANNED")
                    .build();

            List<Activity> saved = activityRepository.saveAll(List.of(act1, act2, act3, act4));
            raftActivity = saved.get(1);
            log.info("Initialized 4 WBS activities for project ID: {}", project.getId());

            // Seed CBS Baseline Budgets
            Budget b1 = Budget.builder()
                    .projectId(project.getId())
                    .activityId(act1.getId())
                    .wbsActivityId(act1.getId())
                    .costCode("CIV_EXC_01")
                    .costHead(CostHead.SUBCONTRACTOR)
                    .baselineAmount(BigDecimal.valueOf(2500000.00))
                    .description("Basement mechanical excavation and soil disposal")
                    .build();

            Budget b2 = Budget.builder()
                    .projectId(project.getId())
                    .activityId(act2.getId())
                    .wbsActivityId(act2.getId())
                    .costCode("CIV_RCC_01")
                    .costHead(CostHead.MATERIAL)
                    .baselineAmount(BigDecimal.valueOf(15000000.00))
                    .description("Raft foundation concrete M30 and TMT Fe500 rebar")
                    .build();

            Budget b3 = Budget.builder()
                    .projectId(project.getId())
                    .activityId(act4.getId())
                    .wbsActivityId(act4.getId())
                    .costCode("MEP_PLUMB_01")
                    .costHead(CostHead.SUBCONTRACTOR)
                    .baselineAmount(BigDecimal.valueOf(4500000.00))
                    .description("CPVC/UPVC internal piping and electrical conduit shafts")
                    .build();

            Budget b4 = Budget.builder()
                    .projectId(project.getId())
                    .costCode("LAB_WAGES_01")
                    .costHead(CostHead.LABOUR)
                    .baselineAmount(BigDecimal.valueOf(6000000.00))
                    .description("Site skilled & unskilled labour wage allocation")
                    .build();

            Budget b5 = Budget.builder()
                    .projectId(project.getId())
                    .costCode("EQ_FLEET_01")
                    .costHead(CostHead.EQUIPMENT)
                    .baselineAmount(BigDecimal.valueOf(3500000.00))
                    .description("Tower crane, boom pump, and DG generator diesel & hire charges")
                    .build();

            budgetRepository.saveAll(List.of(b1, b2, b3, b4, b5));
            log.info("Initialized 5 CBS baseline budget heads for project ID: {}", project.getId());
        } else {
            raftActivity = existing.stream().filter(a -> "ACT-WBS-02".equals(a.getWbsCode())).findFirst().orElse(existing.get(0));
        }

        return raftActivity;
    }

    private void seedSuppliersAndMaterials(Project project) {
        if (!supplierRepository.existsBySupplierCode("SUP-ULT-01")) {
            Supplier s1 = Supplier.builder()
                    .supplierCode("SUP-ULT-01")
                    .name("UltraTech Cement Ltd (Chennai Hub)")
                    .contactPerson("Ramesh Sharma")
                    .phone("9840112233")
                    .email("sales@ultratech.com")
                    .gstin("33AAACU1234F1Z1")
                    .address("Chennai, Tamil Nadu")
                    .status("ACTIVE")
                    .build();

            Supplier s2 = Supplier.builder()
                    .supplierCode("SUP-JSW-02")
                    .name("JSW Steel Ltd (Sriperumbudur Depot)")
                    .contactPerson("Vikram Mehta")
                    .phone("9840223344")
                    .email("rebar@jswsteel.in")
                    .gstin("33AAACJ5678G2Z2")
                    .address("Sriperumbudur, Chennai")
                    .status("ACTIVE")
                    .build();

            Supplier s3 = Supplier.builder()
                    .supplierCode("SUP-COR-03")
                    .name("Coromandel Aggregates & M-Sand")
                    .contactPerson("Balaji V")
                    .phone("9840334455")
                    .email("aggregates@coromandel.in")
                    .gstin("33AAACC9012H3Z3")
                    .address("Chengalpattu, Tamil Nadu")
                    .status("ACTIVE")
                    .build();

            supplierRepository.saveAll(List.of(s1, s2, s3));
        }

        if (!materialRepository.existsByMaterialCode("MAT-CEM-01")) {
            Material m1 = Material.builder()
                    .materialCode("MAT-CEM-01")
                    .name("UltraTech OPC 53 Grade Cement")
                    .category("CEMENT")
                    .unit(MaterialUnit.BAGS)
                    .standardRate(BigDecimal.valueOf(380.00))
                    .reorderLevel(BigDecimal.valueOf(500.00))
                    .currentStock(BigDecimal.valueOf(2500.00))
                    .description("High-early strength 53-grade cement for structural concrete")
                    .build();

            Material m2 = Material.builder()
                    .materialCode("MAT-STL-01")
                    .name("JSW Fe500D TMT Rebar 16mm")
                    .category("STEEL")
                    .unit(MaterialUnit.TONNES)
                    .standardRate(BigDecimal.valueOf(62000.00))
                    .reorderLevel(BigDecimal.valueOf(15.00))
                    .currentStock(BigDecimal.valueOf(65.00))
                    .description("Thermo-Mechanically Treated high ductility earthquake-resistant steel")
                    .build();

            Material m3 = Material.builder()
                    .materialCode("MAT-SND-01")
                    .name("Coromandel Manufactured Sand (M-Sand)")
                    .category("AGGREGATES")
                    .unit(MaterialUnit.CU_M)
                    .standardRate(BigDecimal.valueOf(1650.00))
                    .reorderLevel(BigDecimal.valueOf(50.00))
                    .currentStock(BigDecimal.valueOf(320.00))
                    .description("Double-washed zone-II manufactured sand for plastering and concrete")
                    .build();

            Material m4 = Material.builder()
                    .materialCode("MAT-RMC-01")
                    .name("Ready Mix Concrete M25 Design Mix")
                    .category("CONCRETE")
                    .unit(MaterialUnit.CU_M)
                    .standardRate(BigDecimal.valueOf(4800.00))
                    .reorderLevel(BigDecimal.valueOf(30.00))
                    .currentStock(BigDecimal.valueOf(180.00))
                    .description("Batching plant pumpable concrete mix with flyash retarder")
                    .build();

            materialRepository.saveAll(List.of(m1, m2, m3, m4));
            log.info("Initialized core material master items and inventory stocks.");
        }
    }

    private void seedProcurementData(Project project, Site site, Activity actRaft) {
        if (materialRequestRepository.findByProjectId(project.getId()).isEmpty()) {
            Material cement = materialRepository.findByMaterialCode("MAT-CEM-01").orElse(null);
            Material steel = materialRepository.findByMaterialCode("MAT-STL-01").orElse(null);
            Supplier ultraTech = supplierRepository.findBySupplierCode("SUP-ULT-01").orElse(null);
            Supplier jswSteel = supplierRepository.findBySupplierCode("SUP-JSW-02").orElse(null);

            if (cement != null && ultraTech != null) {
                // 1. Material Requests
                MaterialRequest mr1 = MaterialRequest.builder()
                        .projectId(project.getId())
                        .siteId(site != null ? site.getId() : null)
                        .wbsActivityId(actRaft != null ? actRaft.getId() : null)
                        .materialId(cement.getId())
                        .requiredQty(BigDecimal.valueOf(1000.00))
                        .requiredDate(LocalDate.now().plusDays(5))
                        .status("APPROVED")
                        .requestedBy("se_suresh")
                        .approvedBy("pm_karthik")
                        .justification("Raft foundation concrete pouring for Tower A")
                        .remarks("Priority high - batching schedule locked")
                        .build();

                MaterialRequest mr2 = MaterialRequest.builder()
                        .projectId(project.getId())
                        .siteId(site != null ? site.getId() : null)
                        .wbsActivityId(actRaft != null ? actRaft.getId() : null)
                        .materialId(steel != null ? steel.getId() : cement.getId())
                        .requiredQty(BigDecimal.valueOf(25.00))
                        .requiredDate(LocalDate.now().plusDays(10))
                        .status("PENDING")
                        .requestedBy("se_suresh")
                        .justification("Tower A Column starter reinforcement Fe500D rebar")
                        .remarks("Awaiting bend schedule verification")
                        .build();

                materialRequestRepository.saveAll(List.of(mr1, mr2));

                // 2. Purchase Orders
                PurchaseOrder po1 = PurchaseOrder.builder()
                        .poNumber("PO-PADUR-CEM-01")
                        .projectId(project.getId())
                        .supplierId(ultraTech.getId())
                        .materialId(cement.getId())
                        .orderQty(BigDecimal.valueOf(1000.00))
                        .unitRate(BigDecimal.valueOf(380.00))
                        .amount(BigDecimal.valueOf(380000.00))
                        .deliveryDate(LocalDate.now().plusDays(7))
                        .status("PARTIALLY_FULFILLED")
                        .approvedBy("director_rajesh")
                        .remarks("30 days payment credit terms approved")
                        .build();

                PurchaseOrder po2 = PurchaseOrder.builder()
                        .poNumber("PO-PADUR-STL-02")
                        .projectId(project.getId())
                        .supplierId(jswSteel != null ? jswSteel.getId() : ultraTech.getId())
                        .materialId(steel != null ? steel.getId() : cement.getId())
                        .orderQty(BigDecimal.valueOf(20.00))
                        .unitRate(BigDecimal.valueOf(62000.00))
                        .amount(BigDecimal.valueOf(1240000.00))
                        .deliveryDate(LocalDate.now().plusDays(12))
                        .status("ISSUED")
                        .approvedBy("director_rajesh")
                        .remarks("Formal PO dispatched to JSW Depot")
                        .build();

                PurchaseOrder savedPo1 = purchaseOrderRepository.save(po1);
                purchaseOrderRepository.save(po2);

                // 3. Goods Receipt Note (GRN)
                if (grnRepository.findByProjectId(project.getId()).isEmpty()) {
                    Grn grn1 = Grn.builder()
                            .grnNumber("GRN-PADUR-CEM-01")
                            .poId(savedPo1.getId())
                            .projectId(project.getId())
                            .siteId(site != null ? site.getId() : null)
                            .materialId(cement.getId())
                            .dcNumber("DC-ULT-88412")
                            .vehicleNumber("TN-09-CA-5521")
                            .gateEntryNumber("GATE-2026-104")
                            .receivedQty(BigDecimal.valueOf(600.00))
                            .acceptedQty(BigDecimal.valueOf(580.00))
                            .rejectedQty(BigDecimal.valueOf(20.00))
                            .damageReason("20 bags torn/moisture-damaged in heavy transit rain")
                            .rejectionReason("20 bags torn/moisture-damaged in heavy transit rain")
                            .receivedBy("se_suresh")
                            .deliveryEvidenceUrl("https://storage.buildtwin360.com/evidence/padur_cement_dc.jpg")
                            .status("COMPLETED")
                            .build();

                    grnRepository.save(grn1);
                }

                log.info("Initialized baseline Procurement artifacts (MRs, POs, and GRN) for Padur project.");
            }
        }
    }

    private void seedContractorsAndEquipment(Project project, Site siteA) {
        Contractor lnt = contractorRepository.findByContractorCode("CNT-LNT-01").orElseGet(() -> {
            Contractor c = Contractor.builder()
                    .contractorCode("CNT-LNT-01")
                    .name("L&T Construction (Civil Infrastructure)")
                    .companyName("Larsen & Toubro Ltd")
                    .tradeSpecialization(TradeCategory.MASON)
                    .contractorType(ContractorType.MAIN_CONTRACTOR)
                    .contactNumber("9876543210")
                    .email("padur.civil@intecc.com")
                    .address("L&T Construction Campus, Manapakkam, Chennai")
                    .status("ACTIVE")
                    .build();
            return contractorRepository.save(c);
        });

        contractorRepository.findByContractorCode("CNT-VOL-02").orElseGet(() -> {
            Contractor c = Contractor.builder()
                    .contractorCode("CNT-VOL-02")
                    .name("Voltas Electro-Mechanical Solutions")
                    .companyName("Voltas Ltd")
                    .tradeSpecialization(TradeCategory.ELECTRICIAN)
                    .contractorType(ContractorType.SUBCONTRACTOR)
                    .contactNumber("9876543211")
                    .email("padur.mep@voltas.com")
                    .address("Voltas House, Guindy, Chennai")
                    .status("ACTIVE")
                    .build();
            return contractorRepository.save(c);
        });

        if (labourDailyRepository.count() == 0) {
            LabourDaily lb1 = LabourDaily.builder()
                    .projectId(project.getId())
                    .siteId(siteA != null ? siteA.getId() : null)
                    .contractor(lnt)
                    .tradeCategory(TradeCategory.MASON)
                    .headcount(12)
                    .standardHours(BigDecimal.valueOf(8.00))
                    .overtimeHours(BigDecimal.valueOf(2.00))
                    .recordDate(LocalDate.now())
                    .build();

            LabourDaily lb2 = LabourDaily.builder()
                    .projectId(project.getId())
                    .siteId(siteA != null ? siteA.getId() : null)
                    .contractor(lnt)
                    .tradeCategory(TradeCategory.CARPENTER)
                    .headcount(8)
                    .standardHours(BigDecimal.valueOf(8.00))
                    .overtimeHours(BigDecimal.ZERO)
                    .recordDate(LocalDate.now())
                    .build();

            labourDailyRepository.saveAll(List.of(lb1, lb2));
        }

        Equipment crane = equipmentRepository.findByAssetCode("EQ-TCR-01").orElseGet(() -> {
            Equipment eq = Equipment.builder()
                    .assetCode("EQ-TCR-01")
                    .name("Liebherr 85EC Tower Crane (40m Jib)")
                    .type("CRANE")
                    .status("IN_USE")
                    .build();
            return equipmentRepository.save(eq);
        });

        equipmentRepository.findByAssetCode("EQ-PMP-02").orElseGet(() -> {
            Equipment eq = Equipment.builder()
                    .assetCode("EQ-PMP-02")
                    .name("Schwing Stetter 36m Concrete Boom Pump")
                    .type("PUMP")
                    .status("AVAILABLE")
                    .build();
            return equipmentRepository.save(eq);
        });

        if (equipmentUsageRepository.count() == 0) {
            EquipmentUsage use1 = EquipmentUsage.builder()
                    .projectId(project.getId())
                    .equipmentId(crane.getId())
                    .usageDate(LocalDate.now())
                    .hoursUsed(BigDecimal.valueOf(7.50))
                    .downtimeHours(BigDecimal.valueOf(0.50))
                    .build();

            equipmentUsageRepository.save(use1);
        }
    }

    /**
     * Seeds realistic tickets demonstrating all 5 steps of the Issue Lifecycle
     */
    private void seedProjectIssues(Project project, Long raftActId) {
        if (projectIssueRepository.findByProjectId(project.getId()).isEmpty()) {
            // Step 1: OPEN Non-Conformance Report (NCR) - Major Quality Defect
            ProjectIssue iss1 = ProjectIssue.builder()
                    .ticketNumber("ISS-PADUR-NCR-01")
                    .projectId(project.getId())
                    .wbsActivityId(raftActId)
                    .title("Concrete 7-day cube test compressive strength fail (18.2 MPa < 20 MPa limit)")
                    .description("Tower A raft footing batch #4 cube failed 7-day compressive test in third-party NABL lab. Requires core extraction and rebound hammer test.")
                    .issueType(IssueType.NCR)
                    .severity(IssueSeverity.CRITICAL)
                    .status(IssueStatus.OPEN)
                    .reportedBy("Anand (QC Lead)")
                    .reportedRole("ROLE_QUALITY_ENGINEER")
                    .initialPhotoUrl("https://storage.buildtwin360.com/evidence/padur_cube_test_failure.jpg")
                    .targetResolutionDate(LocalDate.now().plusDays(2))
                    .build();

            // Step 2 & 5: ASSIGNED Blocker - SLA Overdue demonstrating Red Alert Scan
            ProjectIssue iss2 = ProjectIssue.builder()
                    .ticketNumber("ISS-PADUR-BLK-02")
                    .projectId(project.getId())
                    .title("Local 33kV high tension transformer feeder shutdown halting Tower Crane")
                    .description("TANGEDCO substation scheduled an unscheduled power shutdown. Crane operations stalled. Emergency DG generator synchronization needed.")
                    .issueType(IssueType.BLOCKER)
                    .severity(IssueSeverity.CRITICAL)
                    .status(IssueStatus.ASSIGNED)
                    .reportedBy("Suresh Kumar (Site Eng)")
                    .reportedRole("ROLE_SITE_ENGINEER")
                    .assignedTo("Karthik Raman (PM)")
                    .assignedToRole("ROLE_PROJECT_MANAGER")
                    .initialPhotoUrl("https://storage.buildtwin360.com/evidence/transformer_trip.jpg")
                    .targetResolutionDate(LocalDate.now().minusDays(1)) // Intentionally overdue for Step 5 Red Alert
                    .build();

            // Step 3: IN_PROGRESS Operational Issue
            ProjectIssue iss3 = ProjectIssue.builder()
                    .ticketNumber("ISS-PADUR-OPS-03")
                    .projectId(project.getId())
                    .title("Tower A concrete boom pump hydraulic hose pressure leakage")
                    .description("Hydraulic pressure dropped to 120 bar during slab casting. Replacement high-pressure hose ordered and fitting underway.")
                    .issueType(IssueType.ISSUE)
                    .severity(IssueSeverity.HIGH)
                    .status(IssueStatus.IN_PROGRESS)
                    .reportedBy("Manoj Supervisor")
                    .reportedRole("ROLE_SITE_SUPERVISOR")
                    .assignedTo("Voltas MEP Solutions")
                    .assignedToRole("CONTRACTOR")
                    .initialPhotoUrl("https://storage.buildtwin360.com/evidence/hydraulic_hose_leak.jpg")
                    .targetResolutionDate(LocalDate.now().plusDays(1))
                    .build();

            // Step 3: RESOLVED Snag awaiting QA Inspection
            ProjectIssue iss4 = ProjectIssue.builder()
                    .ticketNumber("ISS-PADUR-SNG-04")
                    .projectId(project.getId())
                    .title("Hairline shrinkage cracks on Tower A 2nd floor living room ceiling plaster")
                    .description("Fine surface cracks visible after de-shuttering. Contractor applied micro-concrete polymer bonding agent.")
                    .issueType(IssueType.SNAG)
                    .severity(IssueSeverity.MEDIUM)
                    .status(IssueStatus.RESOLVED)
                    .reportedBy("Suresh Kumar (Site Eng)")
                    .reportedRole("ROLE_SITE_ENGINEER")
                    .assignedTo("L&T Construction (Civil Div)")
                    .assignedToRole("CONTRACTOR")
                    .initialPhotoUrl("https://storage.buildtwin360.com/evidence/ceiling_crack_before.jpg")
                    .actualResolutionDate(LocalDate.now())
                    .resolutionRemarks("V-groove chased 5mm deep, non-shrink polymer grout injected, cured with wet hessian cloth for 48 hours.")
                    .resolutionPhotoUrl("https://storage.buildtwin360.com/evidence/ceiling_crack_rectified.jpg")
                    .targetResolutionDate(LocalDate.now().plusDays(3))
                    .build();

            // Step 4: CLOSED Verified Ticket audited by QC Inspector
            ProjectIssue iss5 = ProjectIssue.builder()
                    .ticketNumber("ISS-PADUR-SNG-05")
                    .projectId(project.getId())
                    .title("Flat 101 master bedroom balcony vitrified tile lippage exceeds 1.5mm")
                    .description("Uneven tile joint level on outer balcony edge near drain trap.")
                    .issueType(IssueType.SNAG)
                    .severity(IssueSeverity.LOW)
                    .status(IssueStatus.CLOSED)
                    .reportedBy("Suresh Kumar (Site Eng)")
                    .reportedRole("ROLE_SITE_ENGINEER")
                    .assignedTo("L&T Construction (Civil Div)")
                    .assignedToRole("CONTRACTOR")
                    .initialPhotoUrl("https://storage.buildtwin360.com/evidence/balcony_tiles_before.jpg")
                    .actualResolutionDate(LocalDate.now().minusDays(1))
                    .resolutionRemarks("Defective 4 tiles dismantled, laser leveled, reset with epoxy adhesive tile spacer joints.")
                    .resolutionPhotoUrl("https://storage.buildtwin360.com/evidence/balcony_tiles_relaid.jpg")
                    .verifiedBy("Anand (QC Lead)")
                    .verificationRemarks("Audited and approved. Lippage verified with digital level gauge: 0.2mm within tolerance.")
                    .verifiedAt(LocalDateTime.now().minusHours(4))
                    .targetResolutionDate(LocalDate.now().plusDays(2))
                    .build();

            // Step 5: RISK Monitored
            ProjectIssue iss6 = ProjectIssue.builder()
                    .ticketNumber("ISS-PADUR-RSK-06")
                    .projectId(project.getId())
                    .title("Heavy Northeast monsoon depression forecast - open basement excavation pit flooding hazard")
                    .description("IMD Chennai forecast predicts 150mm rainfall over next 72 hours. High risk of pit waterlogging and soil slope collapse.")
                    .issueType(IssueType.RISK)
                    .severity(IssueSeverity.HIGH)
                    .status(IssueStatus.OPEN)
                    .reportedBy("Karthik Raman (PM)")
                    .reportedRole("ROLE_PROJECT_MANAGER")
                    .initialPhotoUrl("https://storage.buildtwin360.com/evidence/monsoon_excavation_pit.jpg")
                    .targetResolutionDate(LocalDate.now().plusDays(5))
                    .build();

            projectIssueRepository.saveAll(List.of(iss1, iss2, iss3, iss4, iss5, iss6));
            log.info("Initialized 6 realistic BuildTwin 360 issues across all 5 lifecycle steps.");
        }
    }

    private void seedProjectEvmSnapshot(Project project) {
        if (projectEVMRecordRepository.findByProjectId(project.getId()).isEmpty()) {
            ProjectEVMRecord evm = ProjectEVMRecord.builder()
                    .projectId(project.getId())
                    .evaluationDate(LocalDate.now())
                    .plannedValue(BigDecimal.valueOf(18000000.00))
                    .earnedValue(BigDecimal.valueOf(16500000.00))
                    .actualCost(BigDecimal.valueOf(15500000.00))
                    .budgetAtCompletion(BigDecimal.valueOf(45000000.00))
                    .costVariance(BigDecimal.valueOf(1000000.00))
                    .scheduleVariance(BigDecimal.valueOf(-1500000.00))
                    .cpi(BigDecimal.valueOf(1.065))
                    .spi(BigDecimal.valueOf(0.917))
                    .estimateAtCompletion(BigDecimal.valueOf(42253521.00))
                    .costStatus("UNDER_BUDGET")
                    .scheduleStatus("SLIGHT_DELAY")
                    .build();

            projectEVMRecordRepository.save(evm);
            log.info("Initialized baseline EVM historical analytics snapshot for project ID: {}", project.getId());
        }
    }

    private void seedQualityIssues(Project project) {
        if (qualityIssueRepository.findByProjectId(project.getId()).isEmpty()) {
            QualityIssue q1 = QualityIssue.builder()
                    .projectId(project.getId())
                    .title("Substandard rebar reinforcement tensile strength")
                    .issueType("NCR")
                    .ncrNumber("NCR-PADUR-01")
                    .location("Tower A Raft Foundation")
                    .category("STRUCTURAL")
                    .severity("CRITICAL")
                    .status("OPEN")
                    .dueDate(LocalDate.now().plusDays(3))
                    .responsibleParty("L&T Construction (Civil Div)")
                    .description("Tensile test yielded 460 MPa against mandatory 500 MPa specification. Immediate rebar replacement required.")
                    .defectPhotoUrl("https://storage.buildtwin360.com/evidence/rebar_tensile_fail.jpg")
                    .build();

            QualityIssue q2 = QualityIssue.builder()
                    .projectId(project.getId())
                    .title("Concrete honeycombing on basement retaining shear wall")
                    .issueType("SNAG")
                    .location("Basement Retaining Wall Grid B4-B6")
                    .category("CONCRETE")
                    .severity("HIGH")
                    .status("RECTIFICATION_SUBMITTED")
                    .dueDate(LocalDate.now().plusDays(2))
                    .responsibleParty("L&T Construction (Civil Div)")
                    .description("Honeycombed pockets after shutter stripping due to congested rebar and inadequate needle vibration.")
                    .defectPhotoUrl("https://storage.buildtwin360.com/evidence/honeycomb_b4.jpg")
                    .rectificationPhotoUrl("https://storage.buildtwin360.com/evidence/honeycomb_rectified.jpg")
                    .resolutionNotes("Chipped off porous matrix, applied zinc-rich primer and non-shrink high-strength micro-concrete.")
                    .resolvedBy("Murugan (Site Engineer)")
                    .resolvedAt(LocalDateTime.now().minusHours(6))
                    .build();

            qualityIssueRepository.saveAll(List.of(q1, q2));
            log.info("Initialized 2 realistic Quality Snag & NCR records for project ID: {}", project.getId());
        }
    }

    private void seedProjectRisks(Project project) {
        if (projectRiskRepository.findByProjectId(project.getId()).isEmpty()) {
            ProjectRisk r1 = ProjectRisk.builder()
                    .projectId(project.getId())
                    .riskTitle("Deep excavation monsoon water ingress & pit flooding")
                    .probability("HIGH")
                    .impact("HIGH")
                    .category("SAFETY")
                    .mitigation("Deploy 4x 15HP dewatering submersible pumps with dedicated diesel backup generator.")
                    .contingencyPlan("Pre-cast cutoff drainage trenches along site perimeter to divert surface runoff.")
                    .owner("Karthik Raman (PM)")
                    .status("IDENTIFIED")
                    .build();
            r1.computeSeverityAndScore();

            ProjectRisk r2 = ProjectRisk.builder()
                    .projectId(project.getId())
                    .riskTitle("Structural steel & rebar market price volatility")
                    .probability("MEDIUM")
                    .impact("HIGH")
                    .category("COMMERCIAL")
                    .mitigation("Lock in fixed quarterly procurement contract with primary producer (JSW Steel).")
                    .contingencyPlan("Procure advance buffer stock of 50 tonnes at current rate.")
                    .owner("Director (Procurement)")
                    .status("MONITORED")
                    .build();
            r2.computeSeverityAndScore();

            ProjectRisk r3 = ProjectRisk.builder()
                    .projectId(project.getId())
                    .riskTitle("Grid substation power shutdown halting concrete pump & crane")
                    .probability("LOW")
                    .impact("HIGH")
                    .category("TECHNICAL")
                    .mitigation("Synchronize on-site 250 kVA Caterpillar silent DG set with auto-mains failure panel.")
                    .contingencyPlan("Contract standby mobile generator rental provider within 1 hour SLA.")
                    .owner("Plant Engineer Suresh")
                    .status("MITIGATED")
                    .resolvedBy("Plant Engineer Suresh")
                    .resolvedAt(LocalDateTime.now().minusDays(1))
                    .build();
            r3.computeSeverityAndScore();

            projectRiskRepository.saveAll(List.of(r1, r2, r3));
            log.info("Initialized 3 realistic Project Risk Matrix records for project ID: {}", project.getId());
        }
    }

    private void seedIssueBlockers(Project project) {
        if (issueBlockerRepository.findByProjectId(project.getId()).isEmpty()) {
            IssueBlocker b1 = IssueBlocker.builder()
                    .projectId(project.getId())
                    .title("Liebherr Tower Crane main hoist motor electrical brake failure")
                    .issueType("BLOCKER")
                    .priority("CRITICAL")
                    .severity("CRITICAL")
                    .owner("Plant Engineer Suresh")
                    .impactArea("WORKFLOW")
                    .daysDelayed(3)
                    .dueDate(LocalDate.now().plusDays(1))
                    .status("OPEN")
                    .causeCode("EQUIPMENT_BREAKDOWN")
                    .description("Main hoist brake coil burnt during heavy panel lifting. Shuttering & rebar lifting halted on Tower A.")
                    .build();

            IssueBlocker b2 = IssueBlocker.builder()
                    .projectId(project.getId())
                    .title("Delay in CMDA municipal heavy vehicle transit movement permit")
                    .issueType("BLOCKER")
                    .priority("HIGH")
                    .severity("HIGH")
                    .owner("Liaison Officer Ramesh")
                    .impactArea("SCHEDULE")
                    .daysDelayed(2)
                    .dueDate(LocalDate.now().plusDays(2))
                    .status("IN_PROGRESS")
                    .causeCode("MATERIAL_DELAY")
                    .description("Chennai traffic police restricted transit of 36m concrete boom pump during daytime peak hours. Evening transit clearance pending.")
                    .build();

            issueBlockerRepository.saveAll(List.of(b1, b2));
            log.info("Initialized 2 realistic Site Blocker records for project ID: {}", project.getId());
        }
    }
}

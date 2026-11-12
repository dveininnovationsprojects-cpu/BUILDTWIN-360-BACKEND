# BuildTwin-360 Backend Platform

**BuildTwin-360** is an enterprise **Construction Progress Intelligence and Digital Site Control Platform** engineered with **Spring Boot 3.4.1** and **Java 17+ (JDK 21)**. It provides a unified, role-secured REST backend connecting project hierarchy, WBS scheduling, Daily Progress Reports (DPR), Earned Value Management (EVM), contractor/labour musters, immutable stock ledgers, procurement/GRN, QA/QC NCRs, automated delay alerts, audit logs, cross-module integration, and executive analytics.

---

## Project Snapshot & Verified Metrics

| Metric | Value | Source / Verification |
|---|---|---|
| **Framework & Runtime** | Spring Boot `3.4.1`, Java `17` (JDK `21` verified) | [`pom.xml`](pom.xml) |
| **Database** | PostgreSQL (Runtime) / H2 In-Memory (Tests) | [`application.properties`](src/main/resources/application.properties) |
| **Main Java Source Files** | **504** source files | `src/main/java` |
| **REST Controllers** | **38** controllers under `/api/v1/**` | `src/main/java/.../controller` |
| **JPA Repositories** | **51** repository interfaces | Spring Data JPA scan |
| **Security Model** | Stateless JWT (`jjwt 0.12.6`) + **10-Role RBAC** | [`SecurityConfig.java`](src/main/java/com/example/BuildTwin/_0/security/SecurityConfig.java) |
| **Automated Test Suites** | **64** test classes / **657** passing tests (`0` failures, `0` errors) | `.\mvnw.cmd test` |
| **Postman Suites** | **1** Complete Collection + **13** Module Collections | [`postman/`](postman) |
| **CI/CD Pipeline** | Jenkins Pipeline (`Checkout -> Compile -> Test -> Package`) | [`Jenkinsfile`](Jenkinsfile) |

---

## Team Ownership & Module Contributions

### Backend Development Split

| Track | Developer / Focus | Modules & Deliverables |
|---|---|---|
| **Track 1 — Core Platform, Scheduling & Progress Engine** | **Navin** (Java Backend Developer 1) | Authentication & RBAC, User/Role approvals, Project & Spatial Hierarchy (`Project -> Site -> Building -> Floor -> Zone`), Work Packages & WBS tree, CPM Activity Dependencies & Cycle Validation, Master Baselines & Revisions, 7-Day & 14-Day Look-Ahead Scheduling, Schedule Delay Alerts, Daily Progress Reports (DPR), Progress History & Velocity, Cumulative Progress & S-Curve Engine, Portfolio & Project Progress Summaries, Audit Logs (`AuditController`), Cross-Module Integration (`ModuleIntegrationController`) |
| **Track 2 — Ground Operations, Finance, QA & Executive Analytics** | **Selvamani** (Java Backend Developer 2) | Contractor Registry, Daily Labour & Overtime Logs, Equipment & Usage Tracking, Materials Master, Immutable Double-Entry Stock Ledger & Transfers, Supplier Master, Material Requests / PO Approvals / GRN Auto-Inwarding, CBS Budgeting & EVM Analytics, Quality Snags/NCRs, Project Issues/Risks/Blockers, Document Management, Notification Engine & Scheduled Crons, Executive Reporting Engine (`ReportController`), RBAC & Concurrency/Regression Test Suites |
| **DevOps & CI/CD** | **Ahamed Yasik** / Build Team | [`Jenkinsfile`](Jenkinsfile) multi-stage pipeline for JDK 21 + Maven Wrapper + JUnit reporting + JAR archiving |

---

## Repository Structure

```text
BuildTwin-360/
├── src/main/java/com/example/BuildTwin/_0/
│   ├── BuildTwin360Application.java        # Spring Boot entry point
│   ├── config/                             # OpenApiConfig & DataInitializer demo bootstrapper
│   ├── controller/                         # 38 REST Controllers (/api/v1/**)
│   ├── domain/                             # Modular domain packages
│   │   ├── cost/                           # Budgets, CostTransactions, CostActualLedger, ProjectEVMRecord
│   │   ├── documents/                      # ProjectDocument management
│   │   ├── equipment/                      # Equipment & EquipmentUsage tracking
│   │   ├── identity/                       # User, Role, UserProjectRole & identity repos
│   │   ├── issues/                         # IssueBlocker, ProjectIssue, ProjectRisk, IssueEvidence
│   │   ├── labour/                         # Contractor, LabourDaily, LabourAllocation
│   │   ├── materials/                      # Material, Supplier, StockLedger (immutable append-only)
│   │   ├── notifications/                  # Notification, inbox service & NotificationScheduledTasks
│   │   ├── procurement/                    # MaterialRequest, PurchaseOrder & Grn lifecycle
│   │   ├── projects/                       # Project & Site core domain entities & repositories
│   │   ├── quality/                        # QualityIssue (NCRs) & QualityEvidence
│   │   ├── reports/                        # Weekly, Inventory, CostQuality, ProcurementLabour & Analytics
│   │   └── wbs/                            # Activity & ActivityDependency domain models
│   ├── dto/                                # Auth, User, Hierarchy, WBS, DPR, Progress, Audit & Integration DTOs
│   ├── exception/                          # GlobalExceptionHandler & standardized ErrorResponse
│   ├── model/                              # Core entities (Hierarchy, WBS, DPR, Baselines, Revisions, Alerts, AuditLog)
│   ├── repository/                         # Core JPA repositories
│   ├── security/                           # SecurityConfig, JwtAuthenticationFilter, JwtTokenProvider, CustomUserDetailsService
│   └── service/                            # Service interfaces & implementations
├── src/main/resources/
│   ├── application.properties              # PostgreSQL, JPA, Swagger, JWT & dynamic cost/seed settings
│   └── db/seed_buildtwin360_demo.sql       # Optional SQL seed script
├── src/test/                               # 64 JUnit 5 / MockMvc / Security / Integration test suites
│   └── resources/application-test.properties
├── postman/                                # 13 module-specific Postman collections
├── BuildTwin_360_Complete_Postman_Collection.json  # Unified Postman collection
├── SELVAMANI_WEEKS_1_TO_12_SPRINT_DELIVERY_REPORT.md
├── Jenkinsfile                             # CI/CD pipeline
└── pom.xml                                 # Maven build descriptor
```

---

## Complete Module & API Reference (38 Controllers)

All endpoints are served under `/api/v1` and documented live via Swagger UI (`http://localhost:8080/swagger-ui.html`).

| # | Functional Domain | Controllers & Base Paths | Key Features |
|---:|---|---|---|
| **1** | **Identity, Users & RBAC** | [`AuthController`](src/main/java/com/example/BuildTwin/_0/controller/AuthController.java) (`/api/v1/auth`), [`UserController`](src/main/java/com/example/BuildTwin/_0/controller/UserController.java) (`/api/v1/users`), [`RoleController`](src/main/java/com/example/BuildTwin/_0/controller/RoleController.java) (`/api/v1/roles`), [`ProjectRoleController`](src/main/java/com/example/BuildTwin/_0/controller/ProjectRoleController.java) (`/api/v1/project-roles`) | Registration, JWT login & refresh tokens, pending user approvals, 10-role RBAC & project-level role assignment |
| **2** | **Project & Spatial Hierarchy** | [`ProjectController`](src/main/java/com/example/BuildTwin/_0/controller/ProjectController.java) (`/api/v1/projects`), [`SiteController`](src/main/java/com/example/BuildTwin/_0/controller/SiteController.java), [`BuildingController`](src/main/java/com/example/BuildTwin/_0/controller/BuildingController.java), [`FloorController`](src/main/java/com/example/BuildTwin/_0/controller/FloorController.java), [`ZoneController`](src/main/java/com/example/BuildTwin/_0/controller/ZoneController.java), [`HierarchyController`](src/main/java/com/example/BuildTwin/_0/controller/HierarchyController.java) (`/api/v1/hierarchy`) | Full spatial hierarchy (`Project -> Site -> Building -> Floor -> Zone`), tree endpoints (`/tree/{projectId}`), validation & paginated queries |
| **3** | **Work Packages & WBS** | [`WorkPackageController`](src/main/java/com/example/BuildTwin/_0/controller/WorkPackageController.java), [`WbsActivityController`](src/main/java/com/example/BuildTwin/_0/controller/WbsActivityController.java) | Parent-child hierarchical WBS activities, activity reparenting, weightage & percentage progress rollups |
| **4** | **Dependencies, Baselines & Revisions** | [`ActivityDependencyController`](src/main/java/com/example/BuildTwin/_0/controller/ActivityDependencyController.java) (`/api/v1/activity-dependencies`), [`BaselineScheduleController`](src/main/java/com/example/BuildTwin/_0/controller/BaselineScheduleController.java), [`ScheduleRevisionController`](src/main/java/com/example/BuildTwin/_0/controller/ScheduleRevisionController.java) | CPM precedence dependencies (`FS`, `SS`, `FF`, `SF`) + cycle detection, baseline freezing/activation, revision submit/review/approve/reject workflows |
| **5** | **Look-Ahead & Delay Alerts** | [`LookaheadScheduleController`](src/main/java/com/example/BuildTwin/_0/controller/LookaheadScheduleController.java), [`ScheduleDelayAlertController`](src/main/java/com/example/BuildTwin/_0/controller/ScheduleDelayAlertController.java) | 7-day & 14-day look-ahead windows (`/lookahead`, `/lookahead/14-day`), constraints & commitments, overdue activity mitigation, automated delay scan & alerts |
| **6** | **Daily Progress Reports (DPR)** | [`DailyProgressReportController`](src/main/java/com/example/BuildTwin/_0/controller/DailyProgressReportController.java) (`/api/v1/projects/{projectId}/dpr`) | Draft, submit, approve, reject & withdraw DPRs; manpower, equipment, material, photo & site blocker items cascading into WBS progress |
| **7** | **Progress History, Cumulative Engine & Summary** | [`ProgressHistoryController`](src/main/java/com/example/BuildTwin/_0/controller/ProgressHistoryController.java), [`CumulativeProgressController`](src/main/java/com/example/BuildTwin/_0/controller/CumulativeProgressController.java), [`ProjectProgressSummaryController`](src/main/java/com/example/BuildTwin/_0/controller/ProjectProgressSummaryController.java) | Activity/work-package/project progress history & velocity, as-of-date reconstruction, S-curves (`DAILY`, `WEEKLY`, `MONTHLY`), audit/reconcile & portfolio summaries |
| **8** | **Contractor, Labour & Equipment** | [`ContractorController`](src/main/java/com/example/BuildTwin/_0/controller/ContractorController.java) (`/api/v1/contractors`), [`LabourController`](src/main/java/com/example/BuildTwin/_0/controller/LabourController.java) (`/api/v1/labour`), [`EquipmentController`](src/main/java/com/example/BuildTwin/_0/controller/EquipmentController.java) (`/api/v1/equipment`) | Main & sub-contractor registry (GSTIN/PAN/license), daily labour muster logs (8h Std + OT), equipment allocation & utilization |
| **9** | **Materials & Immutable Stock Ledger** | [`MaterialController`](src/main/java/com/example/BuildTwin/_0/controller/MaterialController.java) (`/api/v1/materials`), [`StockLedgerController`](src/main/java/com/example/BuildTwin/_0/controller/StockLedgerController.java) (`/api/v1/stock-ledger`) | Material catalog, reorder alerts, append-only immutable stock ledger (`RECEIPT`, `ISSUE`, `CONSUMPTION`, `TRANSFER`, `WASTAGE`, `RETURN`, `ADJUSTMENT`) & reconciliation |
| **10** | **Procurement & Suppliers** | [`SupplierController`](src/main/java/com/example/BuildTwin/_0/controller/SupplierController.java) (`/api/v1/suppliers`), [`ProcurementController`](src/main/java/com/example/BuildTwin/_0/controller/ProcurementController.java) (`/api/v1/procurement`) | Supplier master, site material indents (`/requests`), PO approval workflow, GRN recording & automatic stock receipt sync |
| **11** | **Cost, Budget & EVM Analytics** | [`CostController`](src/main/java/com/example/BuildTwin/_0/controller/CostController.java) (`/api/v1/costs`, `/api/v1/cost`) | CBS budgets & revisions, cost transactions, automated material/labour ledger sync, PO liability gaps, overrun alerts & EVM metrics (`CPI`, `SPI`, `EAC`, `VAC`) |
| **12** | **Quality, Issues, Risks, Blockers & Documents** | [`QualityController`](src/main/java/com/example/BuildTwin/_0/controller/QualityController.java) (`/api/v1/quality`), [`IssueController`](src/main/java/com/example/BuildTwin/_0/controller/IssueController.java) (`/api/v1/issues`), [`ProjectIssueController`](src/main/java/com/example/BuildTwin/_0/controller/ProjectIssueController.java) (`/api/v1/project-issues`), [`DocumentController`](src/main/java/com/example/BuildTwin/_0/controller/DocumentController.java) (`/api/v1/documents`) | 4-step NCR/snag lifecycle (Capture -> Assign -> Rectify -> Verify), blocker delay tracking, probability x impact risk matrix, blueprint/photo document storage |
| **13** | **Notifications, Reports, Audit & Integration** | [`NotificationController`](src/main/java/com/example/BuildTwin/_0/controller/NotificationController.java) (`/api/v1/notifications`), [`ReportController`](src/main/java/com/example/BuildTwin/_0/controller/ReportController.java) (`/api/v1/reports`), [`AuditController`](src/main/java/com/example/BuildTwin/_0/controller/AuditController.java) (`/api/v1/audit-logs`), [`ModuleIntegrationController`](src/main/java/com/example/BuildTwin/_0/controller/ModuleIntegrationController.java) (`/api/v1/integration`), [`HomeController`](src/main/java/com/example/BuildTwin/_0/controller/HomeController.java) (`/`) | Priority inbox & cron alerts, weekly/inventory/procurement-labour/cost-quality/analytics reports, system audit statistics & cross-module health checks |

---

## Guide for Frontend Developers

### 1. Base URL & Authentication Flow
- **Backend Base URL (Local)**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (OpenAPI JSON at `/v3/api-docs`)
- **Public Endpoints (No JWT required)** — configured in [`SecurityConfig.java`](src/main/java/com/example/BuildTwin/_0/security/SecurityConfig.java):
  - `OPTIONS /**` (CORS preflight)
  - `/api/v1/auth/**` (`/login`, `/register`, `/refresh`)
  - `/`, `/error`, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Authenticated Endpoints**:
  - Pass header `Authorization: Bearer <accessToken>` on every `/api/v1/**` call.
  - Refresh expired tokens via `POST /api/v1/auth/refresh` and inspect active session claims via `GET /api/v1/auth/me`.

### 2. CORS Configuration
Configured in `SecurityConfig.corsConfigurationSource()`:
- **Allowed Origin Patterns**: `http://localhost:[*]`, `http://127.0.0.1:[*]`, `http://10.0.2.2:[*]` (Android Emulator), `https://localhost:[*]`, `*` (supports React `:3000`, Vite `:5173`, Angular `:4200`, etc.)
- **Allowed Methods**: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`, `HEAD`
- **Allowed Headers**: `Authorization`, `Content-Type`, `X-Requested-With`, `Accept`, `Origin`, `Access-Control-Request-Method`, `Access-Control-Request-Headers`, `X-Custom-Header`, `Cache-Control`
- **Exposed Headers** (readable by Axios/Fetch): `Authorization`, `Content-Disposition`, `X-Total-Count`, `Link`
- **Allow Credentials**: `true` (preflight cached `3600s`)

### 3. Standard JSON Response & Error Contracts

**Success Wrapper ([`ApiResponse<T>`](src/main/java/com/example/BuildTwin/_0/dto/ApiResponse.java)):**
```json
{
  "status": 200,
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "timestamp": "2026-11-12T17:00:00.000"
}
```

**Error Wrapper ([`ErrorResponse`](src/main/java/com/example/BuildTwin/_0/exception/ErrorResponse.java)):**
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/v1/projects",
  "timestamp": "2026-11-12T17:00:00.000",
  "validationErrors": {
    "name": "must not be blank"
  },
  "details": []
}
```

---

## Guide for QA Testers & RBAC Verification

### 1. Pre-Seeded Demo Environment (`app.seed.enabled=true`)
On startup, [`DataInitializer`](src/main/java/com/example/BuildTwin/_0/config/DataInitializer.java) provisions the master demo project and 10-role RBAC users:
- **Master Demo Project**: `Ashok Grandeur - Padur, Chennai` (Code: `PADUR-AG-01`, Budget: `₹45,000,000.00`)
- **Demo Accounts**:

| Username | Email | Password | Assigned Role | Primary Test Scope |
|---|---|---|---|---|
| `admin` | `admin@buildtwin360.com` | `Admin@123` | `ROLE_ADMIN` + `ROLE_DIRECTOR` | User approvals, system overrides, full admin access |
| `director_rajesh` | `director@buildtwin360.com` | `Director@123` | `ROLE_DIRECTOR` | Portfolio dashboards, executive approvals, EVM reports |
| `pm_karthik` | `pm.karthik@buildtwin360.com` | `Pm@123` | `ROLE_PROJECT_MANAGER` | Project setup, baselines, DPR/PO/revision approvals |
| `se_suresh` | `se.suresh@buildtwin360.com` | `Engineer@123` | `ROLE_SITE_ENGINEER` | WBS updates, DPR drafts, material indents, issues |
| `sup_manoj` | `sup.manoj@buildtwin360.com` | `Supervisor@123` | `ROLE_SITE_SUPERVISOR` | Daily labour musters, attendance, site gate monitoring |
| `contractor_lnt` | `contractor.lnt@buildtwin360.com` | `Contractor@123` | `ROLE_SITE_SUPERVISOR` | Subcontractor field activity & labour coordination |
| `proc_ramesh` | `proc.ramesh@buildtwin360.com` | `Procurement@123` | `ROLE_PROCUREMENT_STORE` | Stock ledger receipts/issues/transfers, POs, GRNs |
| `cost_priya` | `cost.priya@buildtwin360.com` | `Cost@123` | `ROLE_QUANTITY_COST_COORDINATOR` | CBS budgets, cost revisions, actual cost variance |
| `qc_anand` | `qc.anand@buildtwin360.com` | `Quality@123` | `ROLE_QUALITY_ENGINEER` | NCR logging, QA verification & rejection workflows |
| `analyst_divya` | `analyst.divya@buildtwin360.com` | `Analyst@123` | `ROLE_DATA_ANALYST` | S-curves, EVM forecasting, executive reports & analytics |
| `auditor_vignesh` | `auditor.vignesh@buildtwin360.com` | `Auditor@123` | `ROLE_AUDITOR` | Read-only ledger audit, stock reconciliation & audit logs |

### 2. Postman Collections
- **Complete Collection**: [`BuildTwin_360_Complete_Postman_Collection.json`](BuildTwin_360_Complete_Postman_Collection.json) (25 suite folders, 208 API requests)
- **Modular Collections in [`postman/`](postman)**:
  - `BuildTwin360_7Day_Lookahead_API.postman_collection.json`
  - `BuildTwin360_14Day_Lookahead_API.postman_collection.json`
  - `BuildTwin360_Activity_Dependency_API.postman_collection.json`
  - `BuildTwin360_Audit_Log_API.postman_collection.json`
  - `BuildTwin360_Calculate_Cumulative_Progress_Engine_API.postman_collection.json`
  - `BuildTwin360_Daily_Progress_Report_DPR_API.postman_collection.json`
  - `BuildTwin360_Epic7_Executive_Reports_API.postman_collection.json`
  - `BuildTwin360_Module_Integration_API.postman_collection.json`
  - `BuildTwin360_Overdue_Activity_Identification_API.postman_collection.json`
  - `BuildTwin360_Progress_History_API.postman_collection.json`
  - `BuildTwin360_Progress_Percentage_Tracking_API.postman_collection.json`
  - `BuildTwin360_Project_Progress_Summary_API.postman_collection.json`
  - `BuildTwin360_Schedule_Delay_Alert_API.postman_collection.json`

### 3. Automated Testing & Coverage Scenarios
Run all **64 test suites (657 test cases)** without PostgreSQL (uses H2 in-memory profile `src/test/resources/application-test.properties`):
```powershell
.\mvnw.cmd test
```
Key verification coverage:
- **RBAC Security**: `SecurityRbacIntegrationTest`, `Epic3ProcurementRbacSecurityTest`, `Epic4CostRbacSecurityTest`, `Epic5QualityAndIssueRbacSecurityTest`, `Epic6NotificationRbacSecurityTest`
- **Concurrency & Ledger Integrity**: Pessimistic row locking (`findByIdForUpdate`), immutable `StockLedger` (`@PreUpdate`/`@PreRemove` protection), negative stock blocking (`Week12RegressionPerformanceTest`)
- **Negative API Cases**: Validation (`400`), Unauthenticated (`401`), Forbidden Role (`403`), Missing Entity (`404`), Duplicate Resource (`409`)

---

## Guide for Data & MIS Analysts

### 1. Executive Reporting & Analytics APIs
- **Weekly Executive Briefing**: `GET /api/v1/reports/weekly/{projectId}`
- **Inventory & Stock Valuation**: `GET /api/v1/reports/inventory/{projectId}`
- **Procurement & Labour Expenditure**: `GET /api/v1/reports/procurement-labour/{projectId}`
- **Cost Variance & Quality/Blocker Summary**: `GET /api/v1/reports/cost-quality/{projectId}`
- **Composite Analytics Aggregation (0–100 Health Score)**: `GET /api/v1/reports/analytics/{projectId}`

### 2. Earned Value Management (EVM) & Progress Intelligence
- **EVM Metrics (`/api/v1/costs` & `/progress-summary`)**: Tracks $BAC$ (Budget at Completion), $PV$ (Planned Value), $EV$ (Earned Value), $AC$ (Actual Cost), Schedule Variance ($SV = EV - PV$), Cost Variance ($CV = EV - AC$), $SPI = EV / PV$, $CPI = EV / AC$, Estimate at Completion ($EAC$), $ETC$, and $VAC$.
- **S-Curve & Cumulative Engine (`/api/v1/projects/{projectId}/cumulative-progress`)**: Supports `DAILY`, `WEEKLY`, and `MONTHLY` S-curve aggregation, as-of-date historical reconstruction, and multi-model weighting (Weightage Factor, Budget, Duration, Equal).
- **Schedule Variance & Audit**: `/api/v1/projects/{projectId}/schedule-variance-report`, `/api/v1/audit-logs`, `/api/v1/integration/projects/{projectId}/summary`.

### 3. Configurable Cost & Wage Rates (`application.properties`)
Default automated ledger accrual rates (INR):
- Equipment operational rate: `app.cost.rates.equipment.hourly-default=1200.00`
- Labour hourly wages: `mason=200.00`, `carpenter=220.00`, `electrician=250.00`, `helper=120.00`, `default=180.00` (overtime calculated at $1.5\times$ standard rate in automated labour wage accrual).

---

## Guide for Backend Developers & DevOps

### 1. Local Setup & Running the Server
1. Ensure **PostgreSQL** is running with database `buildtwin` on port `5432`.
2. Verify credentials in [`src/main/resources/application.properties`](src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/buildtwin
   spring.datasource.username=postgres
   spring.datasource.password=<your_postgres_password>
   spring.jpa.hibernate.ddl-auto=update
   server.port=8080
   ```
3. Compile & start cleanly (use `clean` after refactoring/branch merges so no stale `.class` artifacts remain in `target/`):
   ```powershell
   .\mvnw.cmd clean compile
   .\mvnw.cmd spring-boot:run
   ```

### 2. Autonomous Integrations & Scheduled Tasks
- **GRN -> Stock Ledger**: Approved GRNs automatically append `RECEIPT` transactions; voiding reverses stock.
- **Consumption & Labour -> Cost Actual Ledger**: Material consumption and approved daily labour musters accrue automatically into CBS actual costs.
- **Automated Scheduler Crons** ([`NotificationScheduledTasks`](src/main/java/com/example/BuildTwin/_0/domain/notifications/scheduler/NotificationScheduledTasks.java)): Daily background scans for budget overruns (`01:00 AM`), low-stock shortages (`06:00 AM`), and overdue SLAs (`07:00 AM`).

### 3. CI/CD Pipeline ([`Jenkinsfile`](Jenkinsfile))
Runs cross-platform (Linux `./mvnw` / Windows `mvnw.cmd`) using `JDK21`:
1. **Checkout** -> 2. **Environment Info** -> 3. **Compile** (`clean compile`) -> 4. **Test** (`test` + JUnit XML archival) -> 5. **Package** (`package -DskipTests` + JAR artifact fingerprinting).

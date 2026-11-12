# BuildTwin-360 Backend

**BuildTwin-360** is a Construction Progress Intelligence and Digital Site Control Platform built with **Spring Boot 3.4.1** and **Java 17+**. It provides end-to-end backend capabilities covering identity and RBAC, construction spatial hierarchy, Work Breakdown Structure (WBS), CPM scheduling, baseline & revision governance, Daily Progress Reports (DPR), cumulative progress & S-curve analytics, contractor & labour management, materials & immutable stock ledger, procurement & GRN workflows, cost & Earned Value Management (EVM), quality/issues/risks/blockers, automated notifications & delay alerts, audit logging, cross-module integration, and executive reporting.

---

## Tech Stack

- **Framework**: Spring Boot `3.4.1` (Spring Web, Spring Data JPA, Spring Security, Validation)
- **Language**: Java `17` (tested on JDK 21)
- **Database**: PostgreSQL (Production / Local) & H2 In-Memory (Automated Tests)
- **Security**: Stateless JWT Authentication (`jjwt 0.12.6`) + Role-Based Access Control (RBAC)
- **API Documentation**: SpringDoc OpenAPI `2.8.5` (Swagger UI & `/v3/api-docs`)
- **Build & CI/CD**: Maven Wrapper (`mvnw` / `mvnw.cmd`), `Jenkinsfile`
- **Testing**: JUnit 5, Mockito, Spring MockMvc, Spring Security Test

---

## Project Structure

```text
BuildTwin-360/
├── src/main/java/com/example/BuildTwin/_0/
│   ├── BuildTwin360Application.java        # Spring Boot application entry point
│   ├── config/                             # OpenAPI config & demo DataInitializer
│   ├── controller/                         # 38 REST controllers (/api/v1/**)
│   ├── domain/                             # Modular domain packages
│   │   ├── cost/                           # Budgets, Cost Transactions, Cost Ledger & EVM
│   │   ├── documents/                      # Project Documents
│   │   ├── equipment/                      # Equipment & Usage tracking
│   │   ├── identity/                       # Users, Roles & Project Role assignments
│   │   ├── issues/                         # Issues, Blockers, Risks & Evidence
│   │   ├── labour/                         # Contractors & Daily Labour records
│   │   ├── materials/                      # Materials, Suppliers & Stock Ledger
│   │   ├── notifications/                  # Notifications & Scheduled Alerts
│   │   ├── procurement/                    # Material Requests, Purchase Orders & GRN
│   │   ├── projects/                       # Core Project & Site domain entities/repos
│   │   ├── quality/                        # Quality NCRs, Inspections & Verification
│   │   ├── reports/                        # Weekly, Inventory, Cost/Quality & Analytics Reports
│   │   └── wbs/                            # WBS Activities & Dependencies
│   ├── dto/                                # Shared & progress/WBS/DPR/audit/integration DTOs
│   ├── exception/                          # GlobalExceptionHandler & structured error responses
│   ├── model/                              # Core entities (Hierarchy, WBS, DPR, AuditLog, Baselines, Alerts)
│   ├── repository/                         # JPA repositories
│   ├── security/                           # SecurityConfig, JWT filter & provider
│   └── service/                            # Service interfaces & implementations
├── src/main/resources/
│   ├── application.properties              # Datasource, JWT, Swagger & seed/cost rate properties
│   └── db/seed_buildtwin360_demo.sql       # SQL demo seed script
├── src/test/                               # 64 test suites (Unit, Controller, RBAC Security & Integration)
├── postman/                                # Module-wise Postman API collections
├── BuildTwin_360_Complete_Postman_Collection.json
├── Jenkinsfile                             # CI/CD pipeline definition
└── pom.xml                                 # Maven dependencies & build config
```

---

## Integrated Modules & API Endpoints

All APIs are exposed under `/api/v1` and documented interactively in Swagger UI.

| # | Module | Base Endpoints | Key Capabilities |
|---:|---|---|---|
| 1 | **Auth, Users & RBAC** | `/api/v1/auth`, `/api/v1/users`, `/api/v1/roles`, `/api/v1/project-roles` | JWT login/register, user approval workflows, global & per-project role assignments |
| 2 | **Project & Spatial Hierarchy** | `/api/v1/projects`, `/api/v1/projects/{id}/sites`, `/api/v1/sites/{id}/buildings`, `/api/v1/buildings/{id}/floors`, `/api/v1/floors/{id}/zones`, `/api/v1/hierarchy` | Full multi-level hierarchy tree (`Project -> Site -> Building -> Floor -> Zone`), pagination & validation |
| 3 | **WBS & Work Packages** | `/api/v1/projects/{id}/work-packages`, `/api/v1/work-packages/{id}`, `/api/v1/work-packages/{id}/activities`, `/api/v1/wbs-activities` | Hierarchical parent-child WBS activities, reparenting, status & progress percentage rollups |
| 4 | **Dependencies, Baselines & Revisions** | `/api/v1/activity-dependencies`, `/api/v1/projects/{id}/baselines`, `/api/v1/baselines`, `/api/v1/projects/{id}/schedule-revisions`, `/api/v1/schedule-revisions` | CPM precedence links (FS/SS/FF/SF) with cycle detection, baseline freezing/activation, revision approval workflow & schedule variance |
| 5 | **Look-Ahead & Delay Alerts** | `/api/v1/projects/{id}/lookahead`, `/api/v1/projects/{id}/lookahead/14-day`, `/api/v1/projects/{id}/schedule-alerts` | 7-day & 14-day look-ahead planning, constraint readiness, weekly commitments, overdue activity mitigation & automated delay alerts |
| 6 | **Daily Progress Reports (DPR)** | `/api/v1/projects/{id}/dpr` | Draft, submit, approve/reject/withdraw DPRs; physical quantities, manpower, equipment, materials & blocker tracking |
| 7 | **Progress History, Cumulative Engine & Summary** | `/api/v1/activities/{id}/progress-history`, `/api/v1/projects/{id}/cumulative-progress`, `/api/v1/projects/{id}/progress-summary`, `/api/v1/projects/progress-summary` | Historical as-of-date reconstruction, S-curve timelines, multi-model weighting reconciliation & portfolio summaries |
| 8 | **Contractor, Labour & Equipment** | `/api/v1/contractors`, `/api/v1/labour`, `/api/v1/equipment` | Contractor/sub-contractor registry, daily labour logs & equipment utilization tracking |
| 9 | **Materials & Immutable Stock Ledger** | `/api/v1/materials`, `/api/v1/stock-ledger` | Material catalog, receipt/issue/transfer transactions, stock reconciliation & immutable ledger audit trail |
| 10 | **Procurement & Suppliers** | `/api/v1/suppliers`, `/api/v1/procurement` | Supplier management, material requests, purchase order approvals, GRN receipts & PO fulfillment |
| 11 | **Cost, Budget & EVM Analytics** | `/api/v1/costs` (alias `/api/v1/cost`) | Budgets & revisions, cost transactions, actual cost ledgers, budget overrun variance & EVM metrics (CPI, SPI, CV, SV, EAC) |
| 12 | **Quality, Issues, Risks, Blockers & Documents** | `/api/v1/quality`, `/api/v1/issues`, `/api/v1/project-issues`, `/api/v1/documents` | Quality NCRs & inspection verification, site blockers, risk scoring matrix & project documents |
| 13 | **Notifications, Reports, Audit & Integration** | `/api/v1/notifications`, `/api/v1/reports`, `/api/v1/audit-logs`, `/api/v1/integration` | Scheduled/broadcast notifications, weekly/inventory/cost-quality executive reports, system audit statistics & cross-module integration health |

---

## Prerequisites & Local Setup

### Prerequisites
- **Java 17+** (JDK 17 or JDK 21)
- **PostgreSQL 14+** running locally with a database named `buildtwin`

### Configuration
Update `src/main/resources/application.properties` (or override via environment variables):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/buildtwin
spring.datasource.username=postgres
spring.datasource.password=<your_postgres_password>

# Toggle demo data seeder on startup (true/false)
app.seed.enabled=true
```

### Build & Run Locally

```bash
# Clean compile the project
./mvnw clean compile

# Run the Spring Boot application
./mvnw spring-boot:run
```

On Windows PowerShell / CMD:
```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

The server starts on **`http://localhost:8080`**.

---

## Running Tests

The test suite uses an isolated H2 in-memory database (`src/test/resources/application-test.properties`) and requires no external PostgreSQL instance:

```bash
# Run all 64 test suites (657 tests)
./mvnw test
```

On Windows:
```powershell
.\mvnw.cmd test
```

---

## API Documentation & Postman Collections

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`
- **Complete Postman Collection**: [`BuildTwin_360_Complete_Postman_Collection.json`](BuildTwin_360_Complete_Postman_Collection.json)
- **Module Postman Collections**: Located in the [`postman/`](postman) directory:
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

---

## Security & Demo Seed Credentials

When `app.seed.enabled=true`, `DataInitializer` bootstraps the default project, roles, WBS hierarchy, sample cost/procurement/quality data, and default admin credentials configured via `app.seed.admin.*` in `src/main/resources/application.properties`.

# BuildTwin-360 Backend

**BuildTwin-360** is a modern construction management platform designed to streamline project planning, collaboration, and execution. It combines a robust backend with a dynamic frontend to provide a seamless experience for construction professionals.

## ?? Tech Stack

### Backend

- **Framework**: Spring Boot 3.0
- **Language**: Java 17+
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT
- **Tools**: Gradle, Maven

### Frontend

- **Framework**: React.js
- **State Management**: Redux Toolkit
- **Styling**: Tailwind CSS
- **Routing**: React Router
- **UI Library**: Component UI

## ?? Project Structure

```
BuildTwin-360/
+-- src/main/java/com/buildtwin       # Main application entry point
+-- src/main/resources                # Configuration (application.properties)
+-- src/main/webapp                   # React frontend (built in target/classes)
+-- target                            # Build output (generated frontend files)
```

## ?? Running Locally

### Prerequisites

- Java 17 or higher
- PostgreSQL Database running
- Node.js and npm (for frontend development)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd BuildTwin-360
   ```

2. **Configure database**
   Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/buildtwin
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

   The backend will start on `http://localhost:8080`

### Frontend Development

1. **Navigate to the frontend directory**
   ```bash
   cd src/main/webapp
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```

   The frontend will be available at `http://localhost:3000`

## ?? API Documentation

Once the backend is running, you can access the API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8080/v3/api-docs`

## ?? API Endpoints

The API includes the following core modules:

- **1. Identity & Access Management (`/api/v1/auth`)** - User registration, authentication, JWT tokens, RBAC roles
- **2. Project Hierarchy & Spatial Structure (`/api/v1/projects`)** - Projects, sites, buildings, floors, zones
- **3. Work Package & WBS Module (`/api/v1/work-packages`, `/api/v1/activities`)** - WBS breakdown, task management, scheduling & revisions
- **4. Activity Dependencies & CPM** - Predecessors, successors, dependency types (FS, SS, FF, SF) and lead/lag
- **5. Baseline Schedule & Progress Tracking (Schedule Revision Tracking)**:
  - **Create & Freeze Baseline / Revision**: `POST /api/v1/projects/{projectId}/baselines`
  - **List Project Baselines**: `GET /api/v1/projects/{projectId}/baselines`
  - **Get Baseline Details**: `GET /api/v1/baselines/{id}`
  - **Get Baseline Activity Snapshots**: `GET /api/v1/baselines/{id}/snapshots`
  - **Activate Baseline Revision**: `PATCH /api/v1/projects/{projectId}/baselines/{baselineId}/activate`
  - **Approve Baseline Schedule**: `PATCH /api/v1/baselines/{baselineId}/approve`
  - **Reschedule / Revise Activity**: `PATCH /api/v1/activities/{id}/schedule`
  - **Schedule Variance (SV) & Health Report**: `GET /api/v1/projects/{projectId}/schedule-variance-report`
  - **Activity Schedule Variance**: `GET /api/v1/activities/{activityId}/schedule-variance`
  - **Log Daily Site Progress**: `POST /api/v1/activities/{activityId}/daily-progress`
  - **Get Progress History**: `GET /api/v1/activities/{activityId}/daily-progress`
- **6. Schedule Revision Tracking**:
  - **Create Draft Schedule Revision**: POST /api/v1/projects/{projectId}/schedule-revisions
  - **List Project Revisions**: GET /api/v1/projects/{projectId}/schedule-revisions
  - **Get Revision Details by ID**: GET /api/v1/schedule-revisions/{id}
  - **Submit Revision for Review**: PATCH /api/v1/schedule-revisions/{id}/submit
  - **Review Schedule Revision**: PATCH /api/v1/schedule-revisions/{id}/review
  - **Approve and Apply Revision to Live Schedule**: PATCH /api/v1/schedule-revisions/{id}/approve
  - **Reject Schedule Revision**: PATCH /api/v1/schedule-revisions/{id}/reject
- **7. 7-Day Look-Ahead Schedule & Weekly Work Planning**:
  - **Get 7-Day Look-Ahead Schedule & Workload Matrix**: GET /api/v1/projects/{projectId}/lookahead
  - **Get Look-Ahead Metrics & Readiness Score**: GET /api/v1/projects/{projectId}/lookahead/metrics
  - **Get Constraint & Hindrance Backlog**: GET /api/v1/projects/{projectId}/lookahead/constraints
  - **Create Weekly Work Plan Commitment**: POST /api/v1/projects/{projectId}/lookahead/commitments
  - **List Project Commitments**: GET /api/v1/projects/{projectId}/lookahead/commitments
  - **Get Commitment by ID**: GET /api/v1/lookahead/commitments/{id}
  - **Update Commitment & Actual Quantity**: PUT /api/v1/lookahead/commitments/{id}
  - **Delete Weekly Commitment**: DELETE /api/v1/lookahead/commitments/{id}
- **8. Progress History Module (`/api/v1/activities/{id}/progress-history`, `/api/v1/projects/{id}/progress-history`)**:
  - **Activity Progress History**: GET /api/v1/activities/{activityId}/progress-history
  - **Paginated Activity Progress History**: GET /api/v1/activities/{activityId}/progress-history/paginated
  - **Activity Progress Velocity & S-Curve Analytics**: GET /api/v1/activities/{activityId}/progress-history/analytics
  - **Project-Wide Progress History Dashboard**: GET /api/v1/projects/{projectId}/progress-history
  - **Work Package Progress History**: GET /api/v1/work-packages/{workPackageId}/progress-history
  - **Log Site Progress Entry**: POST /api/v1/activities/{activityId}/progress-history
  - **Get Progress Entry by ID**: GET /api/v1/progress-history/{id}
  - **Update Progress Entry**: PUT /api/v1/progress-history/{id}
  - **Delete Progress Entry (Auto-Recalculate)**: DELETE /api/v1/progress-history/{id}
- **9. Calculate Cumulative Progress Engine (`/api/v1/projects/{id}/cumulative-progress`, `/api/v1/work-packages/{id}/cumulative-progress`, `/api/v1/activities/{id}/cumulative-progress`)**:
  - **Project Cumulative Progress Dashboard**: GET /api/v1/projects/{projectId}/cumulative-progress (Weightage Factor, Budget, Duration, Equal)
  - **Work Package Cumulative Progress**: GET /api/v1/work-packages/{workPackageId}/cumulative-progress
  - **Activity Cumulative Progress**: GET /api/v1/activities/{activityId}/cumulative-progress
  - **Project Historical As-of-Date Reconstruction**: GET /api/v1/projects/{projectId}/cumulative-progress/as-of-date
  - **Activity Historical As-of-Date Progress**: GET /api/v1/activities/{activityId}/cumulative-progress/as-of-date
  - **Project Cumulative S-Curve Timeline**: GET /api/v1/projects/{projectId}/cumulative-progress/s-curve (DAILY, WEEKLY, MONTHLY)
  - **Activity Cumulative S-Curve Timeline**: GET /api/v1/activities/{activityId}/cumulative-progress/s-curve
  - **Recalculate Project Cumulative Progress**: POST /api/v1/projects/{projectId}/cumulative-progress/recalculate
  - **Recalculate Work Package Cumulative Progress**: POST /api/v1/work-packages/{workPackageId}/cumulative-progress/recalculate
  - **Recalculate Activity Cumulative Progress**: POST /api/v1/activities/{activityId}/cumulative-progress/recalculate
  - **Audit Cumulative Progress Data Integrity**: GET /api/v1/projects/{projectId}/cumulative-progress/audit
  - **Reconcile & Auto-Heal Cumulative Progress**: POST /api/v1/projects/{projectId}/cumulative-progress/reconcile
- **10. Project Progress Summary Module (`/api/v1/projects/{id}/progress-summary`, `/api/v1/projects/progress-summary`, `/api/v1/work-packages/{id}/progress-summary`)**:
  - **Project Progress Summary**: `GET /api/v1/projects/{projectId}/progress-summary` (Overall %, Health, EVM, activity counts, physical quantities, work package and discipline rollups)
  - **Portfolio-Wide Progress Summary**: `GET /api/v1/projects/progress-summary`
  - **Work Package Progress Summary**: `GET /api/v1/work-packages/{workPackageId}/progress-summary`
- **11. Audit & History Logging** - Entity audits and revision logs

## ? Key Features

- ? Role-based access control (Admin, Director, Project Manager, Site Engineer, Site Supervisor)
- ? Master Baseline Versioning & Schedule Freeze (Plan of Record contract baselines)
- ? Schedule Revision Tracking with reason audit logs
- ? Earned Value Management (EVM) Schedule Variance (SV) & Slippage analysis
- ? Daily site progress tracking with physical quantities, manpower, equipment & site hindrances
- ? Full WBS hierarchical tree and rollups
- ? Secure authentication with JWT

## ?? Configuration

### Environment Variables

The following environment variables can be set for configuration:

```bash
# Database configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/buildtwin
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password

# JWT configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION_MINUTES=60
```

## ?? Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)\n4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## ?? License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ?? Support

For issues or questions, please open an issue on the GitHub repository.

---

**Built with ?? for Construction Excellence**

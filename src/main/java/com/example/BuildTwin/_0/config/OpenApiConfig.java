package com.example.BuildTwin._0.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI buildTwinOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("BuildTwin 360 - Construction Progress Intelligence & Digital Site Control Platform")
                        .description("Enterprise REST API Documentation for Ashok Builders & Developers, Padur, Chennai. Covering WBS scheduling, Daily Progress Reports (DPR), Labour productivity, Material inventory, Procurement, Budget control, Quality snags, Construction issues, Document repository, Module Integration, and Audit History Analytics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BuildTwin 360 Engineering Team")
                                .email("support@buildtwin360.com")
                                .url("https://ashokbuilderschennai.in"))
                        .license(new License()
                                .name("Proprietary - Ashok Builders & Developers")
                                .url("https://buildtwin360.internal/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.buildtwin360.com").description("Production Server")
                ))
                .tags(List.of(
                        new Tag().name("1. Identity & Access Management").description("Authentication, User management, RBAC, and Project-specific role assignments"),
                        new Tag().name("2. Project & Site Master Setup").description("Projects, Sites, Buildings, Floors, Zones, and Physical Lineage Hierarchy"),
                        new Tag().name("3. Work Package & WBS Module").description("Work Breakdown Structure (WBS), Activity management, Quantity tracking, and Trade allocations"),
                        new Tag().name("4. Activity Dependency & CPM Network Module").description("Precedence relationships (FS, SS, FF, SF), Lag times, Critical Path Method (CPM), and Network loop detection"),
                        new Tag().name("5. Baseline Schedule & Progress Tracking Module").description("Baseline freezing, Planned vs Actual dates, S-curves, and Schedule variance metrics"),
                        new Tag().name("6. Schedule Revision Tracking Module").description("Formal schedule change management, Revision numbers, Reason codes, and Impact assessments"),
                        new Tag().name("7. Daily Progress Report (DPR) Module").description("DPR submissions, Approvals, Revisions, Geo-tagged site photos, and Daily field logs"),
                        new Tag().name("8. Progress History Module").description("Historical audit snapshots, Daily cumulative progress timeline, and Trend analytics"),
                        new Tag().name("9. Calculate Cumulative Progress Engine").description("Automated mathematical engine calculating physical & financial cumulative progress across all WBS levels"),
                        new Tag().name("10. Schedule Delay Alert Support Module").description("Early warning engine, Critical path slippage alerts, Delay severity scoring, and Mitigations"),
                        new Tag().name("11. Procurement & Goods Receipt (GRN)").description("Material Requests, Purchase Orders, Delivery Verification & Goods Receipt Notes"),
                        new Tag().name("12. Module Integration & Collaboration").description("Unified Cross-Module Integration APIs: Spatial Hierarchy, WBS & Activities, Schedule Baselines, Lookahead Planning, DPR Field Logging, and Cumulative Progress Engine Integration."),
                        new Tag().name("13. Issue, Blocker & Risk Register").description("Construction site blockers, Escalations, Severity classification, Owner assignment & Resolution SLA"),
                        new Tag().name("14. Equipment & Asset Usage").description("Equipment register, Site mobilization, Operating hours, and Downtime tracking"),
                        new Tag().name("15. Document & Photo Repository").description("Centralized electronic document management (EDMS), Drawings, Version control, and Site photos"),
                        new Tag().name("17. Project Issue Lifecycle (5-Step Master Workflow)").description("End-to-End master workflow: Step 1 (Capture) -> Step 2 (PM Assign) -> Step 3 (Contractor Fix) -> Step 4 (QA Audit) -> Step 5 (Red Alert)"),
                        new Tag().name("18. Audit & History Logging").description("Enterprise System Audit Trails, Compliance Logs, Security Tracking & Operational Activity Analytics"),
                        new Tag().name("19. Notification & Communication Service").description("Real-time in-app alerts, Email notifications, Shortage alarms, and Scheduled background jobs"),
                        new Tag().name("20. Executive Reports & Analytics Backend").description("Executive KPI dashboards, Consolidated weekly reports, Cost-Quality scores, and Export services")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token format: Bearer <token>")));
    }
}

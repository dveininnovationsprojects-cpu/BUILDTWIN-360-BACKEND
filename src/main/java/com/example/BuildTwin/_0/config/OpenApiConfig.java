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
                        .description("Enterprise REST API Documentation for Ashok Builders & Developers, Padur, Chennai. Covering WBS scheduling, Daily Progress Reports (DPR), Labour productivity, Material inventory, Procurement, Budget control, Quality snags, Construction issues, Document repository, and Executive Analytics.")
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
                        new Tag().name("1. Identity & Access Management").description("Authentication, User management, RBAC, Project-specific role assignments"),
                        new Tag().name("2. Project Hierarchy").description("Projects, Sites, Buildings, Floors, and Micro-zones"),
                        new Tag().name("3. WBS & Schedule Management").description("Work Breakdown Structure, Activities, Precedence dependencies, Baseline freezing, Delay tracking"),
                        new Tag().name("4. Daily Progress Reporting (DPR)").description("DPR submissions, Approvals, Multi-stage revisions, Progress snapshots, Geo-tagged site photos"),
                        new Tag().name("5. Labour & Subcontractor Control").description("Contractor masters, Trade categories, Labour attendance, Activity hours, Overtime, Shift allocations"),
                        new Tag().name("6. Materials & Inventory Control").description("Material catalog, Site indents, Stock ledger, Issue notes, Consumption logs, AI Forecasts"),
                        new Tag().name("7. Procurement & Vendor Management").description("Vendors, Purchase Orders, Material Goods Receipt Notes (GRN), Inspections"),
                        new Tag().name("8. Cost & Budget Control").description("Cost breakdown codes, Project budget heads, Budget revisions, Cost transactions"),
                        new Tag().name("9. Quality Assurance & Snags").description("Inspection test plans, Quality checklists, Snag defect logging, Resolution evidence"),
                        new Tag().name("10. Site Issues, Risks & Equipment").description("Site blocker issues, Project risk registers, Machinery & heavy equipment allocation"),
                        new Tag().name("11. Document Management System (EDMS)").description("Project drawings, Version control, BOQ documents, Site approval records"),
                        new Tag().name("12. System Governance & Audit").description("Audit trails, System notifications, Event bus processing, Report export requests")
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

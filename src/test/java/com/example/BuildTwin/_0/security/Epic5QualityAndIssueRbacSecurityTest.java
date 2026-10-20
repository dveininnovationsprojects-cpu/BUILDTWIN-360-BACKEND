package com.example.BuildTwin._0.security;

import com.example.BuildTwin._0.controller.IssueController;
import com.example.BuildTwin._0.controller.QualityController;
import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.service.IssueService;
import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import com.example.BuildTwin._0.domain.quality.service.QualityService;
import com.example.BuildTwin._0.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import com.example.BuildTwin._0.controller.ProjectIssueController;
import com.example.BuildTwin._0.domain.issues.dto.*;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.service.ProjectIssueService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {QualityController.class, IssueController.class, ProjectIssueController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class Epic5QualityAndIssueRbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QualityService qualityService;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private ProjectIssueService projectIssueService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ==========================================
    // 1. Unauthenticated Requests (401 Unauthorized)
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request to Quality Issues endpoint should return 401")
    void testUnauthenticatedQualityAccess() throws Exception {
        mockMvc.perform(get("/api/v1/quality/issues"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Unauthenticated request to Issues & Blockers endpoint should return 401")
    void testUnauthenticatedIssueAccess() throws Exception {
        mockMvc.perform(get("/api/v1/issues"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Unauthenticated request to Project Risks endpoint should return 401")
    void testUnauthenticatedRiskAccess() throws Exception {
        mockMvc.perform(get("/api/v1/issues/risks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Unauthenticated request to 5-Step Project Issues endpoint should return 401")
    void testUnauthenticatedProjectIssueAccess() throws Exception {
        mockMvc.perform(get("/api/v1/project-issues/project/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==========================================
    // 2. All 10 System Roles Read Access (200 OK)
    // ==========================================

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "DIRECTOR",
            "PROJECT_MANAGER",
            "SITE_ENGINEER",
            "SITE_SUPERVISOR",
            "PROCUREMENT_STORE",
            "QUANTITY_COST_COORDINATOR",
            "QUALITY_ENGINEER",
            "DATA_ANALYST",
            "AUDITOR"
    })
    @DisplayName("All 10 System Roles should successfully view Quality Issues and KPI summary")
    void testAllTenRolesCanViewQualityIssues(String role) throws Exception {
        when(qualityService.getAllQualityIssues(any(), any(), any())).thenReturn(List.of(
                QualityIssue.builder().id(1L).projectId(100L).title("Snag 1").status("OPEN").severity("LOW").build()
        ));
        when(qualityService.getQualitySummary(100L)).thenReturn(
                QualitySummaryDto.builder().projectId(100L).totalIssues(5L).openNcrs(1L).build()
        );

        mockMvc.perform(get("/api/v1/quality/issues")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/quality/summary/project/100")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "DIRECTOR",
            "PROJECT_MANAGER",
            "SITE_ENGINEER",
            "SITE_SUPERVISOR",
            "PROCUREMENT_STORE",
            "QUANTITY_COST_COORDINATOR",
            "QUALITY_ENGINEER",
            "DATA_ANALYST",
            "AUDITOR"
    })
    @DisplayName("All 10 System Roles should successfully view Site Blockers & Issues")
    void testAllTenRolesCanViewIssuesAndBlockers(String role) throws Exception {
        when(issueService.getAllIssues(any(), any(), any())).thenReturn(List.of(
                IssueBlocker.builder().id(1L).projectId(100L).title("Blocker 1").status("OPEN").issueType("BLOCKER").build()
        ));
        when(issueService.getIssueSummary(100L)).thenReturn(
                IssueSummaryDto.builder().projectId(100L).activeBlockers(2L).totalDaysDelayed(5).build()
        );

        mockMvc.perform(get("/api/v1/issues")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/issues/summary/project/100")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "DIRECTOR",
            "PROJECT_MANAGER",
            "SITE_ENGINEER",
            "SITE_SUPERVISOR",
            "PROCUREMENT_STORE",
            "QUANTITY_COST_COORDINATOR",
            "QUALITY_ENGINEER",
            "DATA_ANALYST",
            "AUDITOR"
    })
    @DisplayName("All 10 System Roles should successfully view Project Risks & Risk KPI summary")
    void testAllTenRolesCanViewRisks(String role) throws Exception {
        when(issueService.getAllRisks(any(), any())).thenReturn(List.of(
                ProjectRisk.builder().id(1L).projectId(100L).riskTitle("Monsoon Flood").severity("CRITICAL").riskScore(9).build()
        ));
        when(issueService.getRiskSummary(100L)).thenReturn(
                RiskSummaryDto.builder().projectId(100L).totalRisks(3L).criticalRisks(1L).build()
        );

        mockMvc.perform(get("/api/v1/issues/risks")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/issues/risks/summary/project/100")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN", "DIRECTOR", "PROJECT_MANAGER", "SITE_ENGINEER", "SITE_SUPERVISOR",
            "PROCUREMENT_STORE", "QUANTITY_COST_COORDINATOR", "QUALITY_ENGINEER", "DATA_ANALYST", "AUDITOR"
    })
    @DisplayName("All 10 System Roles should successfully view Project Issues and Summary")
    void testAllTenRolesCanViewProjectIssues(String role) throws Exception {
        when(projectIssueService.getIssuesByProject(eq(1L), any(), any(), any())).thenReturn(List.of(
                ProjectIssueResponseDto.builder().id(1L).projectId(1L).ticketNumber("ISS-PRJ1-1").title("Issue 1").status(IssueStatus.OPEN).build()
        ));
        when(projectIssueService.getIssueSummaryByProject(1L)).thenReturn(
                ProjectIssueSummaryDto.builder().projectId(1L).totalIssues(5L).openCount(2L).build()
        );

        mockMvc.perform(get("/api/v1/project-issues/project/1")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/project-issues/summary/project/1")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 3. Write & Workflow Restrictions (200 / 201 vs 403 Forbidden)
    // ==========================================

    @Test
    @WithMockUser(roles = "QUALITY_ENGINEER")
    @DisplayName("QUALITY_ENGINEER should be authorized (201) to create Quality Issues")
    void testQualityEngineerCanCreateQualityIssue() throws Exception {
        QualityIssue input = QualityIssue.builder()
                .projectId(100L).title("Cold Joint on Pier P3").category("CONCRETE").issueType("NCR").severity("HIGH").build();
        when(qualityService.createQualityIssue(any())).thenReturn(
                QualityIssue.builder().id(1L).projectId(100L).title("Cold Joint on Pier P3").category("CONCRETE").ncrNumber("NCR-PRJ100-1234").build()
        );

        mockMvc.perform(post("/api/v1/quality/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    @DisplayName("AUDITOR role should be Forbidden (403) from creating Quality Issues")
    void testAuditorCannotCreateQualityIssue() throws Exception {
        QualityIssue input = QualityIssue.builder()
                .projectId(100L).title("Unauthorized creation").category("CONCRETE").issueType("NCR").severity("HIGH").build();

        mockMvc.perform(post("/api/v1/quality/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "DATA_ANALYST")
    @DisplayName("DATA_ANALYST role should be Forbidden (403) from verifying/closing Quality Issues")
    void testDataAnalystCannotVerifyQualityIssue() throws Exception {
        QualityVerificationDto dto = QualityVerificationDto.builder()
                .approved(true).remarks("Verified by unauthorized analyst").verifiedBy("Analyst").build();

        mockMvc.perform(post("/api/v1/quality/issues/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "QUALITY_ENGINEER")
    @DisplayName("QUALITY_ENGINEER should be authorized (200) to verify and close NCRs")
    void testQualityEngineerCanVerifyQualityIssue() throws Exception {
        QualityVerificationDto dto = QualityVerificationDto.builder()
                .approved(true).remarks("Rectification verified and approved").verifiedBy("Lead QA").build();
        when(qualityService.verifyQualityIssue(eq(1L), any())).thenReturn(
                QualityIssue.builder().id(1L).status("CLOSED").build()
        );

        mockMvc.perform(post("/api/v1/quality/issues/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "SITE_SUPERVISOR")
    @DisplayName("SITE_SUPERVISOR should be authorized (200) to submit rectification on Quality Issues")
    void testSiteSupervisorCanSubmitRectification() throws Exception {
        QualityResolutionDto dto = QualityResolutionDto.builder()
                .resolutionNotes("Honeycomb patched with high strength epoxy grout").resolvedBy("Supervisor").build();
        when(qualityService.resolveQualityIssue(eq(1L), any())).thenReturn(
                QualityIssue.builder().id(1L).status("RECTIFICATION_SUBMITTED").build()
        );

        mockMvc.perform(post("/api/v1/quality/issues/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "PROCUREMENT_STORE")
    @DisplayName("PROCUREMENT_STORE role should be Forbidden (403) from creating Project Risks")
    void testProcurementStoreCannotCreateRisk() throws Exception {
        ProjectRisk risk = ProjectRisk.builder()
                .projectId(100L).riskTitle("Supply cost spike").probability("HIGH").impact("HIGH").build();

        mockMvc.perform(post("/api/v1/issues/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(risk)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("PROJECT_MANAGER should be authorized (200) to update Risk mitigation status")
    void testProjectManagerCanUpdateRiskStatus() throws Exception {
        RiskStatusUpdateDto statusDto = RiskStatusUpdateDto.builder()
                .status("MITIGATED").resolutionNotes("Risk mitigated through buffer stock").resolvedBy("PM").build();
        when(issueService.updateRiskStatus(eq(1L), any())).thenReturn(
                ProjectRisk.builder().id(1L).status("MITIGATED").build()
        );

        mockMvc.perform(patch("/api/v1/issues/risks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    @DisplayName("AUDITOR role should be Forbidden (403) from deleting Quality Issues or Blockers")
    void testAuditorCannotDeleteQualityOrBlocker() throws Exception {
        mockMvc.perform(delete("/api/v1/quality/issues/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(delete("/api/v1/issues/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "SITE_ENGINEER")
    @DisplayName("SITE_ENGINEER should be authorized (201) to log Project Issues in Step 1")
    void testSiteEngineerCanCreateProjectIssue() throws Exception {
        CreateProjectIssueDto dto = CreateProjectIssueDto.builder()
                .projectId(1L).title("Slab rebar offset").description("Offset exceeds tolerance").issueType(IssueType.SNAG).severity(IssueSeverity.MEDIUM).build();
        when(projectIssueService.createIssue(any())).thenReturn(
                ProjectIssueResponseDto.builder().id(1L).ticketNumber("ISS-PRJ1-1").status(IssueStatus.OPEN).build()
        );

        mockMvc.perform(post("/api/v1/project-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    @DisplayName("AUDITOR role should be Forbidden (403) from creating Project Issues in Step 1")
    void testAuditorCannotCreateProjectIssue() throws Exception {
        CreateProjectIssueDto dto = CreateProjectIssueDto.builder()
                .projectId(1L).title("Unauthorized").description("Desc").issueType(IssueType.SNAG).severity(IssueSeverity.LOW).build();

        mockMvc.perform(post("/api/v1/project-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("PROJECT_MANAGER should be authorized (200) to assign Project Issue and SLA in Step 2")
    void testProjectManagerCanAssignProjectIssue() throws Exception {
        AssignProjectIssueDto dto = AssignProjectIssueDto.builder()
                .assignedTo("Karthik").assignedToRole("ENGINEER").targetResolutionDate(LocalDate.now().plusDays(3)).build();
        when(projectIssueService.assignIssue(eq(1L), any())).thenReturn(
                ProjectIssueResponseDto.builder().id(1L).status(IssueStatus.ASSIGNED).build()
        );

        mockMvc.perform(patch("/api/v1/project-issues/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    @DisplayName("AUDITOR role should be Forbidden (403) from assigning Project Issues in Step 2")
    void testAuditorCannotAssignProjectIssue() throws Exception {
        AssignProjectIssueDto dto = AssignProjectIssueDto.builder()
                .assignedTo("Karthik").assignedToRole("ENGINEER").targetResolutionDate(LocalDate.now().plusDays(3)).build();

        mockMvc.perform(patch("/api/v1/project-issues/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "QUALITY_ENGINEER")
    @DisplayName("QUALITY_ENGINEER should be authorized (200) to audit verify and CLOSE Project Issues in Step 4")
    void testQualityEngineerCanVerifyAndCloseProjectIssue() throws Exception {
        VerifyProjectIssueDto dto = VerifyProjectIssueDto.builder()
                .approved(true).verifiedBy("Anand QC").verificationRemarks("Inspected and approved").build();
        when(projectIssueService.verifyIssue(eq(1L), any())).thenReturn(
                ProjectIssueResponseDto.builder().id(1L).status(IssueStatus.CLOSED).build()
        );

        mockMvc.perform(patch("/api/v1/project-issues/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "DATA_ANALYST")
    @DisplayName("DATA_ANALYST role should be Forbidden (403) from audit verifying Project Issues in Step 4")
    void testDataAnalystCannotVerifyProjectIssue() throws Exception {
        VerifyProjectIssueDto dto = VerifyProjectIssueDto.builder()
                .approved(true).verifiedBy("Analyst").verificationRemarks("Remarks").build();

        mockMvc.perform(patch("/api/v1/project-issues/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}

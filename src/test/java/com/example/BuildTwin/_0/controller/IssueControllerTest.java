package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.IssueEvidence;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.service.IssueService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IssueController.class)
@AutoConfigureMockMvc(addFilters = false)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/issues - Record Site Blocker / Issue")
    void testCreateIssue() throws Exception {
        IssueBlocker input = IssueBlocker.builder()
                .projectId(100L)
                .title("Tower Crane Motor Malfunction")
                .issueType("BLOCKER")
                .severity("CRITICAL")
                .owner("Heavy Machinery Lead")
                .impactArea("SCHEDULE")
                .daysDelayed(2)
                .build();

        IssueBlocker saved = IssueBlocker.builder()
                .id(1L)
                .projectId(100L)
                .title("Tower Crane Motor Malfunction")
                .issueType("BLOCKER")
                .severity("CRITICAL")
                .owner("Heavy Machinery Lead")
                .status("OPEN")
                .build();

        when(issueService.createIssue(any(IssueBlocker.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Tower Crane Motor Malfunction"))
                .andExpect(jsonPath("$.data.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.data.owner").value("Heavy Machinery Lead"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/{id} - Get Issue By ID")
    void testGetIssueById() throws Exception {
        IssueBlocker issue = IssueBlocker.builder()
                .id(1L)
                .projectId(100L)
                .title("Tower Crane Motor Malfunction")
                .severity("CRITICAL")
                .owner("Heavy Machinery Lead")
                .status("OPEN")
                .build();

        when(issueService.getIssueById(1L)).thenReturn(issue);

        mockMvc.perform(get("/api/v1/issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Tower Crane Motor Malfunction"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/blockers/project/{projectId} - Get Active Site Blockers")
    void testGetBlockersByProject() throws Exception {
        IssueBlocker blocker = IssueBlocker.builder()
                .id(1L)
                .projectId(100L)
                .title("Tower Crane Motor Malfunction")
                .issueType("BLOCKER")
                .severity("CRITICAL")
                .daysDelayed(3)
                .status("OPEN")
                .build();

        when(issueService.getBlockersByProject(100L)).thenReturn(List.of(blocker));

        mockMvc.perform(get("/api/v1/issues/blockers/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].issueType").value("BLOCKER"))
                .andExpect(jsonPath("$.data[0].daysDelayed").value(3));
    }

    @Test
    @DisplayName("POST /api/v1/issues/{id}/resolve - Resolve Issue with Notes & Owner")
    void testResolveIssue() throws Exception {
        IssueResolutionDto dto = IssueResolutionDto.builder()
                .resolutionNotes("Technician rewound motor coils and reinstalled successfully.")
                .resolvedBy("Er. Selvam")
                .build();

        IssueBlocker resolved = IssueBlocker.builder()
                .id(1L)
                .projectId(100L)
                .title("Tower Crane Motor Malfunction")
                .status("RESOLVED")
                .resolutionNotes("Technician rewound motor coils and reinstalled successfully.")
                .resolvedBy("Er. Selvam")
                .build();

        when(issueService.resolveIssue(eq(1L), any(IssueResolutionDto.class))).thenReturn(resolved);

        mockMvc.perform(post("/api/v1/issues/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedBy").value("Er. Selvam"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/summary/project/{projectId} - Blocker & Issue KPI Summary")
    void testGetIssueSummary() throws Exception {
        IssueSummaryDto summary = IssueSummaryDto.builder()
                .projectId(100L)
                .totalIssues(10)
                .activeBlockers(2)
                .openIssues(5)
                .inProgressCount(1)
                .resolvedCount(4)
                .criticalCount(2)
                .highCount(3)
                .totalDaysDelayed(7)
                .build();

        when(issueService.getIssueSummary(100L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/issues/summary/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIssues").value(10))
                .andExpect(jsonPath("$.data.activeBlockers").value(2))
                .andExpect(jsonPath("$.data.totalDaysDelayed").value(7));
    }

    @Test
    @DisplayName("POST /api/v1/issues/risks - Register Project Risk")
    void testCreateRisk() throws Exception {
        ProjectRisk input = ProjectRisk.builder()
                .projectId(100L)
                .riskTitle("Material price escalation on structural steel")
                .probability("HIGH")
                .impact("HIGH")
                .owner("Commercial Lead")
                .mitigation("Lock rate contracts with steel manufacturers")
                .build();

        ProjectRisk saved = ProjectRisk.builder()
                .id(10L)
                .projectId(100L)
                .riskTitle("Material price escalation on structural steel")
                .probability("HIGH")
                .impact("HIGH")
                .severity("CRITICAL")
                .riskScore(9)
                .owner("Commercial Lead")
                .status("IDENTIFIED")
                .build();

        when(issueService.createRisk(any(ProjectRisk.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/issues/risks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.data.riskScore").value(9));
    }

    @Test
    @DisplayName("PATCH /api/v1/issues/risks/{id}/status - Update Risk Mitigation & Status")
    void testUpdateRiskStatus() throws Exception {
        RiskStatusUpdateDto dto = RiskStatusUpdateDto.builder()
                .status("MITIGATED")
                .mitigation("Long term fixed price MoU executed with Tata Steel.")
                .resolutionNotes("Rate locked at INR 62/kg, insulating budget from fluctuations.")
                .resolvedBy("PM_Arun")
                .build();

        ProjectRisk updated = ProjectRisk.builder()
                .id(10L)
                .projectId(100L)
                .riskTitle("Material price escalation on structural steel")
                .status("MITIGATED")
                .mitigation("Long term fixed price MoU executed with Tata Steel.")
                .resolutionNotes("Rate locked at INR 62/kg, insulating budget from fluctuations.")
                .resolvedBy("PM_Arun")
                .build();

        when(issueService.updateRiskStatus(eq(10L), any(RiskStatusUpdateDto.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/issues/risks/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("MITIGATED"))
                .andExpect(jsonPath("$.data.resolvedBy").value("PM_Arun"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/risks/summary/project/{projectId} - Risk KPI Summary")
    void testGetRiskSummary() throws Exception {
        RiskSummaryDto summary = RiskSummaryDto.builder()
                .projectId(100L)
                .totalRisks(8)
                .identifiedRisks(3)
                .monitoredRisks(2)
                .mitigatedRisks(3)
                .criticalRisks(2)
                .highRisks(3)
                .mediumRisks(2)
                .lowRisks(1)
                .build();

        when(issueService.getRiskSummary(100L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/issues/risks/summary/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRisks").value(8))
                .andExpect(jsonPath("$.data.mitigatedRisks").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/issues - Get All Issues Globally")
    void testGetAllIssues() throws Exception {
        IssueBlocker issue = IssueBlocker.builder()
                .id(1L)
                .projectId(100L)
                .title("Subcontractor labour strike")
                .issueType("BLOCKER")
                .severity("HIGH")
                .status("OPEN")
                .build();

        when(issueService.getAllIssues(any(), any(), any())).thenReturn(List.of(issue));

        mockMvc.perform(get("/api/v1/issues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Subcontractor labour strike"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/blockers - Get All Active Blockers Globally")
    void testGetAllBlockers() throws Exception {
        IssueBlocker blocker = IssueBlocker.builder()
                .id(2L)
                .projectId(100L)
                .title("Tower crane mechanical issue")
                .issueType("BLOCKER")
                .severity("CRITICAL")
                .status("OPEN")
                .build();

        when(issueService.getAllBlockers()).thenReturn(List.of(blocker));

        mockMvc.perform(get("/api/v1/issues/blockers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].issueType").value("BLOCKER"));
    }

    @Test
    @DisplayName("POST /api/v1/issues/evidence - Attach Issue Proof Evidence")
    void testAddEvidence() throws Exception {
        IssueEvidence evidence = IssueEvidence.builder()
                .id(10L)
                .issueId(1L)
                .objectUrl("https://storage.buildtwin.com/issues/proof_01.jpg")
                .stage("BLOCKER_PROOF")
                .caption("Waterlogged excavation pit")
                .uploadedBy("Site Engineer")
                .build();

        when(issueService.addEvidence(any(IssueEvidence.class))).thenReturn(evidence);

        mockMvc.perform(post("/api/v1/issues/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evidence)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.stage").value("BLOCKER_PROOF"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/{id}/evidence - Get Proof Evidence By Issue ID")
    void testGetEvidenceByIssueId() throws Exception {
        IssueEvidence evidence = IssueEvidence.builder()
                .id(10L)
                .issueId(1L)
                .objectUrl("https://storage.buildtwin.com/issues/proof_01.jpg")
                .stage("BLOCKER_PROOF")
                .build();

        when(issueService.getEvidenceByIssueId(1L)).thenReturn(List.of(evidence));

        mockMvc.perform(get("/api/v1/issues/1/evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].objectUrl").value("https://storage.buildtwin.com/issues/proof_01.jpg"));
    }

    @Test
    @DisplayName("GET /api/v1/issues/risks - Get All Project Risks Globally")
    void testGetAllRisks() throws Exception {
        ProjectRisk risk = ProjectRisk.builder()
                .id(5L)
                .projectId(100L)
                .riskTitle("Severe monsoon flooding")
                .severity("CRITICAL")
                .riskScore(9)
                .status("IDENTIFIED")
                .build();

        when(issueService.getAllRisks(any(), any())).thenReturn(List.of(risk));

        mockMvc.perform(get("/api/v1/issues/risks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].riskTitle").value("Severe monsoon flooding"));
    }
}

package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.issues.dto.*;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.service.ProjectIssueService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectIssueController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectIssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectIssueService projectIssueService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProjectIssueResponseDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = ProjectIssueResponseDto.builder()
                .id(1L)
                .ticketNumber("ISS-PRJ1-1776077700000")
                .projectId(1L)
                .wbsActivityId(10L)
                .title("Wall Tile Crack in Lobby")
                .description("Vitrified tile hairline crack")
                .issueType(IssueType.SNAG)
                .severity(IssueSeverity.LOW)
                .status(IssueStatus.OPEN)
                .reportedBy("Site_Engg_Ravi")
                .initialPhotoUrl("https://storage.buildtwin360.com/defects/tile.jpg")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/project-issues - Step 1: Capture Phase (Identification & Logging)")
    void testStep1CreateIssue() throws Exception {
        CreateProjectIssueDto req = CreateProjectIssueDto.builder()
                .projectId(1L)
                .wbsActivityId(10L)
                .title("Wall Tile Crack in Lobby")
                .issueType(IssueType.SNAG)
                .severity(IssueSeverity.LOW)
                .build();

        when(projectIssueService.createIssue(any(CreateProjectIssueDto.class))).thenReturn(sampleDto);

        mockMvc.perform(post("/api/v1/project-issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.ticketNumber").value("ISS-PRJ1-1776077700000"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/project-issues/1/assign - Step 2: Allocation Phase (PM Assigns & Sets SLA)")
    void testStep2AssignIssue() throws Exception {
        AssignProjectIssueDto req = AssignProjectIssueDto.builder()
                .assignedTo("Tile_Contractor_Mohan")
                .assignedToRole("CONTRACTOR")
                .targetResolutionDate(LocalDate.now().plusDays(3))
                .build();

        sampleDto.setStatus(IssueStatus.ASSIGNED);
        sampleDto.setAssignedTo("Tile_Contractor_Mohan");
        sampleDto.setTargetResolutionDate(LocalDate.now().plusDays(3));

        when(projectIssueService.assignIssue(eq(1L), any(AssignProjectIssueDto.class))).thenReturn(sampleDto);

        mockMvc.perform(patch("/api/v1/project-issues/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.assignedTo").value("Tile_Contractor_Mohan"));
    }

    @Test
    @DisplayName("PATCH /api/v1/project-issues/1/start-progress - Step 3A: Fixing Phase (Contractor Starts Work)")
    void testStep3AStartProgress() throws Exception {
        sampleDto.setStatus(IssueStatus.IN_PROGRESS);
        when(projectIssueService.startProgress(1L)).thenReturn(sampleDto);

        mockMvc.perform(patch("/api/v1/project-issues/1/start-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/v1/project-issues/1/resolve - Step 3B: Rectification Phase (Proof Upload & Remarks)")
    void testStep3BResolveIssue() throws Exception {
        ResolveProjectIssueDto req = ResolveProjectIssueDto.builder()
                .resolutionRemarks("Replaced cracked tile and re-grouted joint.")
                .resolutionPhotoUrl("https://storage.buildtwin360.com/defects/tile_fixed.jpg")
                .build();

        sampleDto.setStatus(IssueStatus.RESOLVED);
        sampleDto.setResolutionRemarks("Replaced cracked tile and re-grouted joint.");
        when(projectIssueService.resolveIssue(eq(1L), any(ResolveProjectIssueDto.class))).thenReturn(sampleDto);

        mockMvc.perform(patch("/api/v1/project-issues/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolutionRemarks").value("Replaced cracked tile and re-grouted joint."));
    }

    @Test
    @DisplayName("PATCH /api/v1/project-issues/1/verify - Step 4: Audit Phase (QA Lead Approval -> CLOSED)")
    void testStep4VerifyPass() throws Exception {
        VerifyProjectIssueDto req = VerifyProjectIssueDto.builder()
                .approved(true)
                .verifiedBy("QA_Lead_Suresh")
                .verificationRemarks("Tile replacement meets alignment tolerance.")
                .build();

        sampleDto.setStatus(IssueStatus.CLOSED);
        sampleDto.setVerifiedBy("QA_Lead_Suresh");
        when(projectIssueService.verifyIssue(eq(1L), any(VerifyProjectIssueDto.class))).thenReturn(sampleDto);

        mockMvc.perform(patch("/api/v1/project-issues/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.verifiedBy").value("QA_Lead_Suresh"));
    }

    @Test
    @DisplayName("GET /api/v1/project-issues/1 - Get Issue Details By ID")
    void testGetIssueById() throws Exception {
        when(projectIssueService.getIssueById(1L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/v1/project-issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.ticketNumber").value("ISS-PRJ1-1776077700000"));
    }

    @Test
    @DisplayName("GET /api/v1/project-issues/project/1 - Filter Issues By Project and Type")
    void testGetIssuesByProject() throws Exception {
        when(projectIssueService.getIssuesByProject(1L, null, null, null)).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/project-issues/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].issueType").value("SNAG"));
    }

    @Test
    @DisplayName("GET /api/v1/project-issues/overdue/project/1 - Step 5A: Monitoring Phase (Get Overdue SLA Breaches)")
    void testGetOverdueIssues() throws Exception {
        sampleDto.setOverdue(true);
        when(projectIssueService.getOverdueIssuesByProject(1L)).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/project-issues/overdue/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].overdue").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/project-issues/summary/project/1 - Step 5B: Monitoring Phase (Get KPI Summary)")
    void testGetIssueSummary() throws Exception {
        ProjectIssueSummaryDto summary = ProjectIssueSummaryDto.builder()
                .projectId(1L)
                .totalIssues(10)
                .closedCount(8)
                .openCount(2)
                .snagCount(6)
                .ncrCount(2)
                .blockerCount(2)
                .overdueCount(1)
                .resolutionPercentage(80.0)
                .build();

        when(projectIssueService.getIssueSummaryByProject(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/project-issues/summary/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIssues").value(10))
                .andExpect(jsonPath("$.data.resolutionPercentage").value(80.0))
                .andExpect(jsonPath("$.data.snagCount").value(6));
    }
}

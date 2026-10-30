package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.dto.*;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.model.ProjectIssue;
import com.example.BuildTwin._0.domain.issues.repository.ProjectIssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectIssueServiceTest {

    @Mock
    private ProjectIssueRepository projectIssueRepository;

    @InjectMocks
    private ProjectIssueServiceImpl projectIssueService;

    private ProjectIssue sampleIssue;

    @BeforeEach
    void setUp() {
        sampleIssue = ProjectIssue.builder()
                .id(1L)
                .ticketNumber("ISS-PRJ1-1776000000000")
                .projectId(1L)
                .wbsActivityId(10L)
                .title("Wall Paint Peeling in Flat 101")
                .description("Paint blistering on exterior wall")
                .issueType(IssueType.SNAG)
                .severity(IssueSeverity.LOW)
                .status(IssueStatus.OPEN)
                .reportedBy("Site_Engg_Kumar")
                .reportedRole("SITE_ENGINEER")
                .initialPhotoUrl("https://storage.buildtwin360.com/photos/snag1.jpg")
                .build();
    }

    @Test
    @DisplayName("Step 1 (Capture Phase): Should log a new issue ticket with status OPEN")
    void testStep1CreateIssue() {
        CreateProjectIssueDto dto = CreateProjectIssueDto.builder()
                .projectId(1L)
                .wbsActivityId(10L)
                .title("Wall Paint Peeling in Flat 101")
                .description("Paint blistering on exterior wall")
                .issueType(IssueType.SNAG)
                .severity(IssueSeverity.LOW)
                .reportedBy("Site_Engg_Kumar")
                .reportedRole("SITE_ENGINEER")
                .initialPhotoUrl("https://storage.buildtwin360.com/photos/snag1.jpg")
                .build();

        when(projectIssueRepository.save(any(ProjectIssue.class))).thenAnswer(invocation -> {
            ProjectIssue pi = invocation.getArgument(0);
            pi.setId(1L);
            return pi;
        });

        ProjectIssueResponseDto res = projectIssueService.createIssue(dto);

        assertNotNull(res);
        assertEquals(IssueStatus.OPEN, res.getStatus());
        assertTrue(res.getTicketNumber().startsWith("ISS-PRJ1-"));
        assertEquals("Wall Paint Peeling in Flat 101", res.getTitle());
        verify(projectIssueRepository, times(1)).save(any(ProjectIssue.class));
    }

    @Test
    @DisplayName("Step 2 (Allocation Phase): Should assign issue to contractor with target SLA date")
    void testStep2AssignIssue() {
        AssignProjectIssueDto dto = AssignProjectIssueDto.builder()
                .assignedTo("Alpha_Painting_Contractor")
                .assignedToRole("CONTRACTOR")
                .targetResolutionDate(LocalDate.now().plusDays(2))
                .severity(IssueSeverity.MEDIUM)
                .build();

        when(projectIssueRepository.findById(1L)).thenReturn(Optional.of(sampleIssue));
        when(projectIssueRepository.save(any(ProjectIssue.class))).thenReturn(sampleIssue);

        ProjectIssueResponseDto res = projectIssueService.assignIssue(1L, dto);

        assertNotNull(res);
        assertEquals(IssueStatus.ASSIGNED, res.getStatus());
        assertEquals("Alpha_Painting_Contractor", res.getAssignedTo());
        assertEquals(LocalDate.now().plusDays(2), res.getTargetResolutionDate());
        verify(projectIssueRepository, times(1)).save(sampleIssue);
    }

    @Test
    @DisplayName("Step 3A (Fixing Phase): Should transition issue to IN_PROGRESS when contractor starts work")
    void testStep3AStartProgress() {
        sampleIssue.setStatus(IssueStatus.ASSIGNED);
        when(projectIssueRepository.findById(1L)).thenReturn(Optional.of(sampleIssue));
        when(projectIssueRepository.save(any(ProjectIssue.class))).thenReturn(sampleIssue);

        ProjectIssueResponseDto res = projectIssueService.startProgress(1L);

        assertNotNull(res);
        assertEquals(IssueStatus.IN_PROGRESS, res.getStatus());
    }

    @Test
    @DisplayName("Step 3B (Rectification Phase): Should resolve issue with proof photo and remarks")
    void testStep3BResolveIssue() {
        sampleIssue.setStatus(IssueStatus.IN_PROGRESS);
        ResolveProjectIssueDto dto = ResolveProjectIssueDto.builder()
                .resolutionRemarks("Wall scraped, double primer applied, and re-painted with weather shield.")
                .resolutionPhotoUrl("https://storage.buildtwin360.com/photos/snag1_fixed.jpg")
                .actualResolutionDate(LocalDate.now())
                .build();

        when(projectIssueRepository.findById(1L)).thenReturn(Optional.of(sampleIssue));
        when(projectIssueRepository.save(any(ProjectIssue.class))).thenReturn(sampleIssue);

        ProjectIssueResponseDto res = projectIssueService.resolveIssue(1L, dto);

        assertNotNull(res);
        assertEquals(IssueStatus.RESOLVED, res.getStatus());
        assertEquals("Wall scraped, double primer applied, and re-painted with weather shield.", res.getResolutionRemarks());
        assertEquals("https://storage.buildtwin360.com/photos/snag1_fixed.jpg", res.getResolutionPhotoUrl());
    }

    @Test
    @DisplayName("Step 4 (Audit Phase - PASS): Should verify and CLOSE issue when quality inspection passes")
    void testStep4VerifyPass() {
        sampleIssue.setStatus(IssueStatus.RESOLVED);
        VerifyProjectIssueDto dto = VerifyProjectIssueDto.builder()
                .approved(true)
                .verifiedBy("QA_Inspector_Suresh")
                .verificationRemarks("Inspection complete. Repainting is uniform and meets quality specs.")
                .build();

        when(projectIssueRepository.findById(1L)).thenReturn(Optional.of(sampleIssue));
        when(projectIssueRepository.save(any(ProjectIssue.class))).thenReturn(sampleIssue);

        ProjectIssueResponseDto res = projectIssueService.verifyIssue(1L, dto);

        assertNotNull(res);
        assertEquals(IssueStatus.CLOSED, res.getStatus());
        assertEquals("QA_Inspector_Suresh", res.getVerifiedBy());
    }

    @Test
    @DisplayName("Step 4 (Audit Phase - FAIL): Should REJECT and REVERT status to OPEN when fix is sub-standard")
    void testStep4VerifyFail() {
        sampleIssue.setStatus(IssueStatus.RESOLVED);
        VerifyProjectIssueDto dto = VerifyProjectIssueDto.builder()
                .approved(false)
                .verifiedBy("QA_Inspector_Suresh")
                .verificationRemarks("REJECTED: Primer thickness is uneven, bubbles visible.")
                .build();

        when(projectIssueRepository.findById(1L)).thenReturn(Optional.of(sampleIssue));
        when(projectIssueRepository.save(any(ProjectIssue.class))).thenReturn(sampleIssue);

        ProjectIssueResponseDto res = projectIssueService.verifyIssue(1L, dto);

        assertNotNull(res);
        assertEquals(IssueStatus.OPEN, res.getStatus()); // Reverted cycle to OPEN!
        assertEquals("REJECTED: Primer thickness is uneven, bubbles visible.", res.getVerificationRemarks());
    }

    @Test
    @DisplayName("Step 5 (Monitoring Phase): Should retrieve overdue issues exceeding SLA target date")
    void testStep5OverdueIssues() {
        sampleIssue.setStatus(IssueStatus.ASSIGNED);
        sampleIssue.setTargetResolutionDate(LocalDate.now().minusDays(3)); // 3 days overdue!

        when(projectIssueRepository.findOverdueIssuesByProject(eq(1L), eq(IssueStatus.CLOSED), any(LocalDate.class)))
                .thenReturn(List.of(sampleIssue));

        List<ProjectIssueResponseDto> overdues = projectIssueService.getOverdueIssuesByProject(1L);

        assertNotNull(overdues);
        assertEquals(1, overdues.size());
        assertTrue(overdues.get(0).isOverdue());
    }

    @Test
    @DisplayName("Step 5 (Monitoring Phase): Should compute issue summary and KPI metrics")
    void testStep5Summary() {
        sampleIssue.setStatus(IssueStatus.CLOSED);
        when(projectIssueRepository.findByProjectId(1L)).thenReturn(List.of(sampleIssue));

        ProjectIssueSummaryDto summary = projectIssueService.getIssueSummaryByProject(1L);

        assertNotNull(summary);
        assertEquals(1, summary.getTotalIssues());
        assertEquals(1, summary.getClosedCount());
        assertEquals(1, summary.getSnagCount());
        assertEquals(100.0, summary.getResolutionPercentage());
    }

    @Test
    @DisplayName("Step 5 (Scheduled Monitoring): Should execute daily scan and log red alerts for overdue issues")
    void testStep5RunDailyOverdueScan() {
        sampleIssue.setStatus(IssueStatus.ASSIGNED);
        sampleIssue.setTargetResolutionDate(LocalDate.now().minusDays(2));
        when(projectIssueRepository.findAllOverdueIssues(eq(IssueStatus.CLOSED), any(LocalDate.class)))
                .thenReturn(List.of(sampleIssue));

        assertDoesNotThrow(() -> projectIssueService.runDailyOverdueScan());
    }
}

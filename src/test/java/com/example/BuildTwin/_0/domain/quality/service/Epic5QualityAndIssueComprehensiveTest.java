package com.example.BuildTwin._0.domain.quality.service;

import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.IssueEvidence;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.repository.IssueEvidenceRepository;
import com.example.BuildTwin._0.domain.issues.repository.ProjectRiskRepository;
import com.example.BuildTwin._0.domain.issues.service.IssueServiceImpl;
import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import com.example.BuildTwin._0.domain.quality.repository.QualityEvidenceRepository;
import com.example.BuildTwin._0.domain.quality.repository.QualityIssueRepository;
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
public class Epic5QualityAndIssueComprehensiveTest {

    // Quality Mocks
    @Mock
    private QualityIssueRepository qualityIssueRepository;

    @Mock
    private QualityEvidenceRepository qualityEvidenceRepository;

    @InjectMocks
    private QualityServiceImpl qualityService;

    // Issue / Risk Mocks
    @Mock
    private IssueBlockerRepository issueBlockerRepository;

    @Mock
    private ProjectRiskRepository projectRiskRepository;

    @Mock
    private IssueEvidenceRepository issueEvidenceRepository;

    @InjectMocks
    private IssueServiceImpl issueService;

    // ==========================================
    // 1. Quality / NCR / Snag Issue Tests
    // ==========================================

    @Test
    @DisplayName("Quality: Create NCR auto-generates NCR number when blank")
    void testCreateNcrAutoNumber() {
        QualityIssue ncr = QualityIssue.builder()
                .projectId(101L)
                .title("Substandard rebar reinforcement tensile strength")
                .issueType("NCR")
                .category("STRUCTURAL")
                .severity("CRITICAL")
                .build();

        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        QualityIssue saved = qualityService.createQualityIssue(ncr);

        assertNotNull(saved);
        assertEquals("OPEN", saved.getStatus());
        assertEquals("NCR", saved.getIssueType());
        assertNotNull(saved.getNcrNumber());
        assertTrue(saved.getNcrNumber().startsWith("NCR-PRJ101-"));
    }

    @Test
    @DisplayName("Quality: Resolve issue transitions to RECTIFICATION_SUBMITTED & auto-creates evidence")
    void testResolveQualityIssueWithEvidence() {
        QualityIssue existing = QualityIssue.builder()
                .id(1L)
                .projectId(101L)
                .title("Concrete honeycombing")
                .status("OPEN")
                .build();

        when(qualityIssueRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        QualityResolutionDto dto = QualityResolutionDto.builder()
                .resolutionNotes("Applied high-performance non-shrink grout and cured for 48 hours")
                .resolvedBy("Murugan_SiteEngineer")
                .rectificationPhotoUrl("https://storage.buildtwin.com/proofs/rectified_honeycomb.jpg")
                .build();

        QualityIssue resolved = qualityService.resolveQualityIssue(1L, dto);

        assertEquals("RECTIFICATION_SUBMITTED", resolved.getStatus());
        assertEquals("Murugan_SiteEngineer", resolved.getResolvedBy());
        assertNotNull(resolved.getResolvedAt());
        assertEquals("https://storage.buildtwin.com/proofs/rectified_honeycomb.jpg", resolved.getRectificationPhotoUrl());
        verify(qualityEvidenceRepository).save(any(QualityEvidence.class));
    }

    @Test
    @DisplayName("Quality: Verify approval transitions to CLOSED; rejection reopens issue")
    void testVerifyQualityIssueFlow() {
        QualityIssue issue = QualityIssue.builder()
                .id(2L)
                .projectId(101L)
                .status("RECTIFICATION_SUBMITTED")
                .build();

        when(qualityIssueRepository.findById(2L)).thenReturn(Optional.of(issue));
        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Verify Approval -> CLOSED
        QualityVerificationDto approvedDto = QualityVerificationDto.builder()
                .verifiedBy("QA_Lead_Ramesh")
                .approved(true)
                .remarks("Core rebound hammer test passed.")
                .build();

        QualityIssue closed = qualityService.verifyQualityIssue(2L, approvedDto);
        assertEquals("CLOSED", closed.getStatus());
        assertEquals("QA_Lead_Ramesh", closed.getVerifiedBy());

        // 2. Verify Rejection -> REOPENED
        QualityVerificationDto rejectedDto = QualityVerificationDto.builder()
                .verifiedBy("QA_Lead_Ramesh")
                .approved(false)
                .remarks("Surface finish uneven, needs re-grouting.")
                .build();

        QualityIssue reopened = qualityService.verifyQualityIssue(2L, rejectedDto);
        assertEquals("REOPENED", reopened.getStatus());
        assertTrue(reopened.getDescription().contains("Surface finish uneven"));
    }

    @Test
    @DisplayName("Quality: KPI Summary calculates total, open snags, open NCRs, and critical issues")
    void testQualitySummaryMetrics() {
        QualityIssue snag = QualityIssue.builder().projectId(101L).issueType("SNAG").status("OPEN").severity("LOW").build();
        QualityIssue ncr = QualityIssue.builder().projectId(101L).issueType("NCR").status("OPEN").severity("CRITICAL").build();
        QualityIssue submitted = QualityIssue.builder().projectId(101L).issueType("SNAG").status("RECTIFICATION_SUBMITTED").severity("HIGH").build();
        QualityIssue closed = QualityIssue.builder().projectId(101L).issueType("SNAG").status("CLOSED").severity("MEDIUM").build();

        when(qualityIssueRepository.findByProjectId(101L)).thenReturn(List.of(snag, ncr, submitted, closed));

        QualitySummaryDto summary = qualityService.getQualitySummary(101L);

        assertEquals(4, summary.getTotalIssues());
        assertEquals(2, summary.getOpenSnags()); // OPEN and RECTIFICATION_SUBMITTED
        assertEquals(1, summary.getOpenNcrs());
        assertEquals(1, summary.getPendingVerification());
        assertEquals(1, summary.getClosedCount());
        assertEquals(1, summary.getCriticalCount());
        assertEquals(1, summary.getHighCount());
    }

    // ==========================================
    // 2. Risk Management & Mitigation Plans Tests
    // ==========================================

    @Test
    @DisplayName("Risk: Auto-calculates risk score and severity based on probability and impact")
    void testRiskScoreAndSeverityCalculation() {
        // High * High = 9 -> CRITICAL
        ProjectRisk criticalRisk = ProjectRisk.builder()
                .projectId(101L)
                .riskTitle("Deep excavation monsoon water ingress")
                .probability("HIGH")
                .impact("HIGH")
                .build();

        when(projectRiskRepository.save(any(ProjectRisk.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjectRisk savedCritical = issueService.createRisk(criticalRisk);
        assertEquals(9, savedCritical.getRiskScore());
        assertEquals("CRITICAL", savedCritical.getSeverity());

        // Medium * High = 6 -> CRITICAL
        ProjectRisk highRisk = ProjectRisk.builder()
                .projectId(101L)
                .riskTitle("Cement supplier logistics delay")
                .probability("MEDIUM")
                .impact("HIGH")
                .build();

        ProjectRisk savedHigh = issueService.createRisk(highRisk);
        assertEquals(6, savedHigh.getRiskScore());
        assertEquals("CRITICAL", savedHigh.getSeverity());

        // Low * Low = 1 -> LOW
        ProjectRisk lowRisk = ProjectRisk.builder()
                .projectId(101L)
                .riskTitle("Minor tool wear")
                .probability("LOW")
                .impact("LOW")
                .build();

        ProjectRisk savedLow = issueService.createRisk(lowRisk);
        assertEquals(1, savedLow.getRiskScore());
        assertEquals("LOW", savedLow.getSeverity());
    }

    @Test
    @DisplayName("Risk: Status transition updates mitigation notes and timestamps")
    void testRiskStatusTransition() {
        ProjectRisk risk = ProjectRisk.builder()
                .id(10L)
                .projectId(101L)
                .riskTitle("Price volatility of structural rebar")
                .status("IDENTIFIED")
                .build();

        when(projectRiskRepository.findById(10L)).thenReturn(Optional.of(risk));
        when(projectRiskRepository.save(any(ProjectRisk.class))).thenAnswer(inv -> inv.getArgument(0));

        RiskStatusUpdateDto dto = RiskStatusUpdateDto.builder()
                .status("MITIGATED")
                .mitigation("Signed fixed rate contract with primary producer")
                .resolutionNotes("Rate locked at INR 62/kg for next 6 months")
                .resolvedBy("Director_Finance")
                .build();

        ProjectRisk updated = issueService.updateRiskStatus(10L, dto);

        assertEquals("MITIGATED", updated.getStatus());
        assertEquals("Signed fixed rate contract with primary producer", updated.getMitigation());
        assertNotNull(updated.getResolvedAt());
    }

    @Test
    @DisplayName("Risk: KPI Summary returns correct distribution across severity and mitigation status")
    void testRiskSummaryMetrics() {
        ProjectRisk r1 = ProjectRisk.builder().projectId(101L).status("IDENTIFIED").severity("CRITICAL").build();
        ProjectRisk r2 = ProjectRisk.builder().projectId(101L).status("MONITORED").severity("HIGH").build();
        ProjectRisk r3 = ProjectRisk.builder().projectId(101L).status("MITIGATED").severity("MEDIUM").build();

        when(projectRiskRepository.findByProjectId(101L)).thenReturn(List.of(r1, r2, r3));

        RiskSummaryDto summary = issueService.getRiskSummary(101L);

        assertEquals(3, summary.getTotalRisks());
        assertEquals(1, summary.getIdentifiedRisks());
        assertEquals(1, summary.getMonitoredRisks());
        assertEquals(1, summary.getMitigatedRisks());
        assertEquals(1, summary.getCriticalRisks());
        assertEquals(1, summary.getHighRisks());
        assertEquals(1, summary.getMediumRisks());
    }

    // ==========================================
    // 3. Site Blocker & Evidence Tracking Tests
    // ==========================================

    @Test
    @DisplayName("Blocker: Resolving blocker updates status and auto-saves proof evidence")
    void testResolveBlockerWithEvidence() {
        IssueBlocker blocker = IssueBlocker.builder()
                .id(5L)
                .projectId(101L)
                .title("Tower crane mechanical failure")
                .issueType("BLOCKER")
                .severity("CRITICAL")
                .status("OPEN")
                .daysDelayed(4)
                .build();

        when(issueBlockerRepository.findById(5L)).thenReturn(Optional.of(blocker));
        when(issueBlockerRepository.save(any(IssueBlocker.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueResolutionDto dto = IssueResolutionDto.builder()
                .resolutionNotes("Spare hoist motor procured and commissioned under certified inspection.")
                .resolvedBy("Plant_Engineer_Ganesh")
                .resolutionEvidenceUrl("https://storage.buildtwin.com/proofs/crane_tested.jpg")
                .build();

        IssueBlocker resolved = issueService.resolveIssue(5L, dto);

        assertEquals("RESOLVED", resolved.getStatus());
        assertEquals("Plant_Engineer_Ganesh", resolved.getResolvedBy());
        assertNotNull(resolved.getResolvedAt());
        assertEquals("https://storage.buildtwin.com/proofs/crane_tested.jpg", resolved.getResolutionEvidenceUrl());
        verify(issueEvidenceRepository).save(any(IssueEvidence.class));
    }

    @Test
    @DisplayName("Blocker: Filter active blockers returns only BLOCKER type or CRITICAL severity")
    void testGetActiveBlockers() {
        IssueBlocker b1 = IssueBlocker.builder().projectId(101L).issueType("BLOCKER").severity("HIGH").build();
        IssueBlocker b2 = IssueBlocker.builder().projectId(101L).issueType("SITE_DEFECT").severity("CRITICAL").build();
        IssueBlocker b3 = IssueBlocker.builder().projectId(101L).issueType("ISSUE").severity("LOW").build();

        when(issueBlockerRepository.findByProjectId(101L)).thenReturn(List.of(b1, b2, b3));

        List<IssueBlocker> blockers = issueService.getBlockersByProject(101L);

        assertEquals(2, blockers.size());
        assertTrue(blockers.contains(b1));
        assertTrue(blockers.contains(b2));
        assertFalse(blockers.contains(b3));
    }

    @Test
    @DisplayName("Evidence: Add and retrieve evidence photos for Quality and Issues")
    void testEvidenceManagement() {
        IssueEvidence issueProof = IssueEvidence.builder()
                .issueId(5L)
                .objectUrl("https://storage.buildtwin.com/proofs/issue_01.jpg")
                .stage("BLOCKER_PROOF")
                .build();

        when(issueEvidenceRepository.save(any(IssueEvidence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(issueEvidenceRepository.findByIssueId(5L)).thenReturn(List.of(issueProof));

        IssueEvidence saved = issueService.addEvidence(issueProof);
        assertNotNull(saved);
        assertEquals("BLOCKER_PROOF", saved.getStage());

        List<IssueEvidence> list = issueService.getEvidenceByIssueId(5L);
        assertEquals(1, list.size());
        assertEquals("https://storage.buildtwin.com/proofs/issue_01.jpg", list.get(0).getObjectUrl());
    }
}

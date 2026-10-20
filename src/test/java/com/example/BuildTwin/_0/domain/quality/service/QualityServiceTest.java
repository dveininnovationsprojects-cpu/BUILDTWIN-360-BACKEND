package com.example.BuildTwin._0.domain.quality.service;

import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import com.example.BuildTwin._0.domain.quality.repository.QualityEvidenceRepository;
import com.example.BuildTwin._0.domain.quality.repository.QualityIssueRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    @Mock
    private QualityIssueRepository qualityIssueRepository;

    @Mock
    private QualityEvidenceRepository qualityEvidenceRepository;

    @InjectMocks
    private QualityServiceImpl qualityService;

    private QualityIssue sampleSnag;
    private QualityIssue sampleNcr;

    @BeforeEach
    void setUp() {
        sampleSnag = QualityIssue.builder()
                .id(1L)
                .projectId(100L)
                .title("Minor Honeycombing on Column C2")
                .issueType("SNAG")
                .category("CONCRETE")
                .severity("MEDIUM")
                .status("OPEN")
                .responsibleParty("Civil Contractor ABC")
                .location("Tower 1, 2nd Floor")
                .dueDate(LocalDate.now().plusDays(5))
                .build();

        sampleNcr = QualityIssue.builder()
                .id(2L)
                .projectId(100L)
                .title("Rebar spacing violation at Shear Wall")
                .issueType("NCR")
                .category("STRUCTURAL")
                .severity("CRITICAL")
                .status("OPEN")
                .responsibleParty("Steel Fixer Team")
                .location("Tower 1, Foundation")
                .dueDate(LocalDate.now().plusDays(2))
                .build();
    }

    @Test
    @DisplayName("Create Quality Issue: defaults status to OPEN, severity to MEDIUM, and auto-generates NCR number if NCR")
    void testCreateQualityIssue() {
        QualityIssue inputNcr = QualityIssue.builder()
                .projectId(100L)
                .title("Cube test failed at 28 days")
                .category("CONCRETE")
                .issueType("NCR")
                .responsibleParty("RMC Vendor")
                .build();

        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> {
            QualityIssue saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        QualityIssue created = qualityService.createQualityIssue(inputNcr);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo("OPEN");
        assertThat(created.getSeverity()).isEqualTo("MEDIUM");
        assertThat(created.getNcrNumber()).startsWith("NCR-PRJ100-");
        verify(qualityIssueRepository, times(1)).save(any(QualityIssue.class));
    }

    @Test
    @DisplayName("Submit Rectification Resolution: transitions status to RECTIFICATION_SUBMITTED with notes and resolvedBy")
    void testResolveQualityIssue() {
        when(qualityIssueRepository.findById(1L)).thenReturn(Optional.of(sampleSnag));
        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        QualityResolutionDto dto = QualityResolutionDto.builder()
                .resolutionNotes("Applied Sika polymer repair mortar and curing compound.")
                .resolvedBy("Er. Rajesh")
                .build();

        QualityIssue resolved = qualityService.resolveQualityIssue(1L, dto);

        assertThat(resolved.getStatus()).isEqualTo("RECTIFICATION_SUBMITTED");
        assertThat(resolved.getResolutionNotes()).isEqualTo("Applied Sika polymer repair mortar and curing compound.");
        assertThat(resolved.getResolvedBy()).isEqualTo("Er. Rajesh");
        assertThat(resolved.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Verify Quality Issue: approving marks status CLOSED with verifiedBy and verifiedAt")
    void testVerifyQualityIssueApproved() {
        sampleSnag.setStatus("RECTIFICATION_SUBMITTED");
        when(qualityIssueRepository.findById(1L)).thenReturn(Optional.of(sampleSnag));
        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        QualityVerificationDto dto = QualityVerificationDto.builder()
                .verifiedBy("QA Lead Ramesh")
                .approved(true)
                .remarks("Surface finish and adhesion verified. OK.")
                .build();

        QualityIssue verified = qualityService.verifyQualityIssue(1L, dto);

        assertThat(verified.getStatus()).isEqualTo("CLOSED");
        assertThat(verified.getVerifiedBy()).isEqualTo("QA Lead Ramesh");
        assertThat(verified.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("Verify Quality Issue: rejecting reopens the issue with REOPENED status")
    void testVerifyQualityIssueRejected() {
        sampleSnag.setStatus("RECTIFICATION_SUBMITTED");
        when(qualityIssueRepository.findById(1L)).thenReturn(Optional.of(sampleSnag));
        when(qualityIssueRepository.save(any(QualityIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        QualityVerificationDto dto = QualityVerificationDto.builder()
                .verifiedBy("QA Lead Ramesh")
                .approved(false)
                .remarks("Bonding hollow sound detected. Re-do rectification.")
                .build();

        QualityIssue verified = qualityService.verifyQualityIssue(1L, dto);

        assertThat(verified.getStatus()).isEqualTo("REOPENED");
        assertThat(verified.getDescription()).contains("Bonding hollow sound detected");
    }

    @Test
    @DisplayName("Evidence attachment and retrieval for Quality Defect")
    void testEvidenceHandling() {
        QualityEvidence evidence = QualityEvidence.builder()
                .qualityIssueId(1L)
                .objectUrl("https://s3.amazonaws.com/buildtwin/defects/col_c2.jpg")
                .stage("DEFECT_CAPTURE")
                .caption("Before repair photo")
                .build();

        when(qualityEvidenceRepository.save(evidence)).thenReturn(evidence);
        when(qualityEvidenceRepository.findByQualityIssueId(1L)).thenReturn(List.of(evidence));

        QualityEvidence saved = qualityService.addEvidence(evidence);
        List<QualityEvidence> list = qualityService.getEvidenceByQualityIssueId(1L);

        assertThat(saved).isNotNull();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getObjectUrl()).contains("col_c2.jpg");
    }

    @Test
    @DisplayName("Quality Summary KPIs computation")
    void testGetQualitySummary() {
        QualityIssue closedSnag = QualityIssue.builder()
                .id(3L).projectId(100L).issueType("SNAG").status("CLOSED").severity("LOW").build();
        QualityIssue pendingNcr = QualityIssue.builder()
                .id(4L).projectId(100L).issueType("NCR").status("RECTIFICATION_SUBMITTED").severity("HIGH").build();

        when(qualityIssueRepository.findByProjectId(100L))
                .thenReturn(List.of(sampleSnag, sampleNcr, closedSnag, pendingNcr));

        QualitySummaryDto summary = qualityService.getQualitySummary(100L);

        assertThat(summary.getTotalIssues()).isEqualTo(4);
        assertThat(summary.getOpenSnags()).isEqualTo(1); // sampleSnag is OPEN
        assertThat(summary.getOpenNcrs()).isEqualTo(2);  // sampleNcr is OPEN, pendingNcr is RECTIFICATION_SUBMITTED (not closed)
        assertThat(summary.getPendingVerification()).isEqualTo(1);
        assertThat(summary.getClosedCount()).isEqualTo(1);
        assertThat(summary.getCriticalCount()).isEqualTo(1); // sampleNcr
        assertThat(summary.getHighCount()).isEqualTo(1);     // pendingNcr
    }
}

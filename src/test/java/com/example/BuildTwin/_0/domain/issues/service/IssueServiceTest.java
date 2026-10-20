package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.repository.ProjectRiskRepository;
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
class IssueServiceTest {

    @Mock
    private IssueBlockerRepository issueBlockerRepository;

    @Mock
    private ProjectRiskRepository projectRiskRepository;

    @InjectMocks
    private IssueServiceImpl issueService;

    private IssueBlocker sampleBlocker;
    private IssueBlocker sampleIssue;
    private ProjectRisk sampleRisk;

    @BeforeEach
    void setUp() {
        sampleBlocker = IssueBlocker.builder()
                .id(1L)
                .projectId(100L)
                .title("Excavator Hydraulic Failure")
                .issueType("BLOCKER")
                .severity("CRITICAL")
                .priority("CRITICAL")
                .owner("Machinery Supervisor John")
                .impactArea("SCHEDULE")
                .daysDelayed(3)
                .status("OPEN")
                .causeCode("EQUIPMENT_BREAKDOWN")
                .dueDate(LocalDate.now().plusDays(2))
                .build();

        sampleIssue = IssueBlocker.builder()
                .id(2L)
                .projectId(100L)
                .title("Minor Drawing Clarification on Beam Layout")
                .issueType("ISSUE")
                .severity("LOW")
                .priority("LOW")
                .owner("Architect Lead Anitha")
                .impactArea("DESIGN")
                .daysDelayed(0)
                .status("OPEN")
                .causeCode("DESIGN_RFI")
                .build();

        sampleRisk = ProjectRisk.builder()
                .id(10L)
                .projectId(100L)
                .riskTitle("Monsoon flooding risk in basement excavation")
                .probability("HIGH")
                .impact("HIGH")
                .owner("Safety Officer Peter")
                .mitigation("Install dewatering sump pits and standby diesel pumps")
                .status("IDENTIFIED")
                .build();
    }

    @Test
    @DisplayName("Create Site Blocker: verifies default status OPEN, severity/priority sync, and owner")
    void testCreateIssueBlocker() {
        IssueBlocker newBlocker = IssueBlocker.builder()
                .projectId(100L)
                .title("Ready-mix concrete strike")
                .severity("HIGH")
                .owner("Procurement Team")
                .build();

        when(issueBlockerRepository.save(any(IssueBlocker.class))).thenAnswer(inv -> {
            IssueBlocker saved = inv.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        IssueBlocker created = issueService.createIssue(newBlocker);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo("OPEN");
        assertThat(created.getSeverity()).isEqualTo("HIGH");
        assertThat(created.getPriority()).isEqualTo("HIGH");
        assertThat(created.getIssueType()).isEqualTo("BLOCKER");
        assertThat(created.getOwner()).isEqualTo("Procurement Team");
    }

    @Test
    @DisplayName("Resolve Blocker: records resolution notes, resolvedBy, resolvedAt and marks status RESOLVED")
    void testResolveIssueWithDetails() {
        when(issueBlockerRepository.findById(1L)).thenReturn(Optional.of(sampleBlocker));
        when(issueBlockerRepository.save(any(IssueBlocker.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueResolutionDto dto = IssueResolutionDto.builder()
                .resolutionNotes("Replacement hydraulic pump installed and tested. Excavation resumed.")
                .resolvedBy("Senior Mechanic Karthik")
                .build();

        IssueBlocker resolved = issueService.resolveIssue(1L, dto);

        assertThat(resolved.getStatus()).isEqualTo("RESOLVED");
        assertThat(resolved.getResolutionNotes()).contains("Replacement hydraulic pump installed");
        assertThat(resolved.getResolvedBy()).isEqualTo("Senior Mechanic Karthik");
        assertThat(resolved.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Get Blockers specifically: returns active blockers and critical severity items")
    void testGetBlockersByProject() {
        when(issueBlockerRepository.findByProjectId(100L)).thenReturn(List.of(sampleBlocker, sampleIssue));

        List<IssueBlocker> blockers = issueService.getBlockersByProject(100L);

        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0).getTitle()).isEqualTo("Excavator Hydraulic Failure");
    }

    @Test
    @DisplayName("Issue and Blocker Summary KPIs computation")
    void testGetIssueSummary() {
        IssueBlocker resolvedBlocker = IssueBlocker.builder()
                .id(3L).projectId(100L).issueType("BLOCKER").status("RESOLVED").severity("HIGH").daysDelayed(2).build();

        when(issueBlockerRepository.findByProjectId(100L)).thenReturn(List.of(sampleBlocker, sampleIssue, resolvedBlocker));

        IssueSummaryDto summary = issueService.getIssueSummary(100L);

        assertThat(summary.getTotalIssues()).isEqualTo(3);
        assertThat(summary.getActiveBlockers()).isEqualTo(1); // sampleBlocker is OPEN
        assertThat(summary.getResolvedCount()).isEqualTo(1);   // resolvedBlocker
        assertThat(summary.getCriticalCount()).isEqualTo(1);   // sampleBlocker
        assertThat(summary.getTotalDaysDelayed()).isEqualTo(3); // only active blocker days counted
    }

    @Test
    @DisplayName("Create Project Risk: auto computes riskScore (3*3=9) and severity CRITICAL")
    void testCreateRiskAutoScore() {
        when(projectRiskRepository.save(any(ProjectRisk.class))).thenAnswer(inv -> {
            ProjectRisk r = inv.getArgument(0);
            r.setId(20L);
            return r;
        });

        ProjectRisk created = issueService.createRisk(sampleRisk);

        assertThat(created).isNotNull();
        assertThat(created.getRiskScore()).isEqualTo(9); // HIGH (3) * HIGH (3)
        assertThat(created.getSeverity()).isEqualTo("CRITICAL");
        assertThat(created.getStatus()).isEqualTo("IDENTIFIED");
    }

    @Test
    @DisplayName("Update Risk Status: transitions to MITIGATED with resolution notes and resolvedBy")
    void testUpdateRiskStatus() {
        when(projectRiskRepository.findById(10L)).thenReturn(Optional.of(sampleRisk));
        when(projectRiskRepository.save(any(ProjectRisk.class))).thenAnswer(inv -> inv.getArgument(0));

        RiskStatusUpdateDto dto = RiskStatusUpdateDto.builder()
                .status("MITIGATED")
                .mitigation("3 dewatering pumps installed and perimeter drainage trenches dug.")
                .resolutionNotes("Water handled during recent rainfall without any flooding incident.")
                .resolvedBy("Safety Head Ramesh")
                .build();

        ProjectRisk updated = issueService.updateRiskStatus(10L, dto);

        assertThat(updated.getStatus()).isEqualTo("MITIGATED");
        assertThat(updated.getMitigation()).contains("3 dewatering pumps");
        assertThat(updated.getResolvedBy()).isEqualTo("Safety Head Ramesh");
        assertThat(updated.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Project Risk Summary KPIs computation")
    void testGetRiskSummary() {
        ProjectRisk mediumRisk = ProjectRisk.builder()
                .id(11L).projectId(100L).probability("MEDIUM").impact("LOW").severity("LOW").status("MONITORED").build();
        ProjectRisk mitigatedRisk = ProjectRisk.builder()
                .id(12L).projectId(100L).probability("HIGH").impact("HIGH").severity("CRITICAL").status("MITIGATED").build();

        sampleRisk.setSeverity("CRITICAL");
        when(projectRiskRepository.findByProjectId(100L)).thenReturn(List.of(sampleRisk, mediumRisk, mitigatedRisk));

        RiskSummaryDto summary = issueService.getRiskSummary(100L);

        assertThat(summary.getTotalRisks()).isEqualTo(3);
        assertThat(summary.getIdentifiedRisks()).isEqualTo(1);
        assertThat(summary.getMonitoredRisks()).isEqualTo(1);
        assertThat(summary.getMitigatedRisks()).isEqualTo(1);
        assertThat(summary.getCriticalRisks()).isEqualTo(2);
        assertThat(summary.getLowRisks()).isEqualTo(1);
    }
}

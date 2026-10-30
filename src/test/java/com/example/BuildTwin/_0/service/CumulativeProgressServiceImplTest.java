package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.cumulative.*;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.ActivityProgressLogRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.impl.CumulativeProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CumulativeProgressServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkPackageRepository workPackageRepository;

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private ActivityProgressLogRepository progressLogRepository;

    @Mock
    private WbsActivityService wbsActivityService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CumulativeProgressServiceImpl cumulativeProgressService;

    private Project mockProject;
    private WorkPackage mockWorkPackage;
    private WbsActivity mockActivity1;
    private WbsActivity mockActivity2;

    @BeforeEach
    void setUp() {
        mockProject = Project.builder()
                .id(1L)
                .code("PRJ-SKY-001")
                .name("Skyline Residency")
                .status("ACTIVE")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 12, 31))
                .actualStartDate(LocalDate.of(2026, 9, 5))
                .build();

        mockWorkPackage = WorkPackage.builder()
                .id(2L)
                .code("WP-CIV-01")
                .name("Civil Works")
                .discipline("CIVIL")
                .status("IN_PROGRESS")
                .budgetAmount(new BigDecimal("500000.00"))
                .project(mockProject)
                .build();

        mockActivity1 = WbsActivity.builder()
                .id(10L)
                .code("ACT-CIV-001")
                .name("Excavation Works")
                .discipline("CIVIL")
                .uom("CUM")
                .plannedQuantity(500.0)
                .completedQuantity(250.0)
                .progressPercentage(50.0)
                .weightage(2.0)
                .status("IN_PROGRESS")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 10, 31))
                .actualStartDate(LocalDate.of(2026, 9, 5))
                .project(mockProject)
                .workPackage(mockWorkPackage)
                .build();

        mockActivity2 = WbsActivity.builder()
                .id(11L)
                .code("ACT-CIV-002")
                .name("Piling Works")
                .discipline("CIVIL")
                .uom("NOS")
                .plannedQuantity(100.0)
                .completedQuantity(100.0)
                .progressPercentage(100.0)
                .weightage(3.0)
                .status("COMPLETED")
                .plannedStartDate(LocalDate.of(2026, 9, 1))
                .plannedEndDate(LocalDate.of(2026, 10, 15))
                .actualStartDate(LocalDate.of(2026, 9, 5))
                .actualEndDate(LocalDate.of(2026, 10, 14))
                .project(mockProject)
                .workPackage(mockWorkPackage)
                .build();
    }

    @Test
    @DisplayName("Should calculate project cumulative progress with multi-model weightings and EVM")
    void testGetProjectCumulativeProgress() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));
        when(workPackageRepository.findByProjectId(1L))
                .thenReturn(List.of(mockWorkPackage));

        CumulativeProgressResponse res = cumulativeProgressService.getProjectCumulativeProgress(
                1L, WeightingModel.WEIGHTAGE_FACTOR, null, null);

        assertNotNull(res);
        assertEquals(1L, res.getProjectId());
        assertEquals("PRJ-SKY-001", res.getProjectCode());
        // Weightage factor: (50*2 + 100*3) / (2+3) = 400 / 5 = 80.0%
        assertEquals(80.0, res.getOverallActualProgressPercentage());
        assertEquals(80.0, res.getWeightageFactorProgressPercentage());
        // Equal weighted: (50 + 100) / 2 = 75.0%
        assertEquals(75.0, res.getEqualWeightedProgressPercentage());
        assertNotNull(res.getOverallPlannedProgressPercentage());
        assertNotNull(res.getEarnedValue());
        assertEquals(2, res.getTotalActivities());
        assertEquals(1, res.getCompletedActivities());
        assertEquals(1, res.getInProgressActivities());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void testGetProjectCumulativeProgress_NotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cumulativeProgressService.getProjectCumulativeProgress(99L, WeightingModel.WEIGHTAGE_FACTOR, null, null));
    }

    @Test
    @DisplayName("Should calculate work package cumulative progress")
    void testGetWorkPackageCumulativeProgress() {
        when(workPackageRepository.findById(2L)).thenReturn(Optional.of(mockWorkPackage));
        when(wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(2L))
                .thenReturn(List.of(mockActivity1, mockActivity2));

        WorkPackageCumulativeProgressResponse res = cumulativeProgressService.getWorkPackageCumulativeProgress(
                2L, WeightingModel.WEIGHTAGE_FACTOR);

        assertNotNull(res);
        assertEquals(2L, res.getWorkPackageId());
        assertEquals("WP-CIV-01", res.getWorkPackageCode());
        assertEquals(80.0, res.getCumulativeActualProgressPercentage());
        assertEquals(2, res.getTotalActivities());
    }

    @Test
    @DisplayName("Should calculate activity cumulative progress")
    void testGetActivityCumulativeProgress() {
        when(wbsActivityRepository.findById(10L)).thenReturn(Optional.of(mockActivity1));

        ActivityCumulativeProgressResponse res = cumulativeProgressService.getActivityCumulativeProgress(10L);

        assertNotNull(res);
        assertEquals(10L, res.getActivityId());
        assertEquals("ACT-CIV-001", res.getActivityCode());
        assertEquals(500.0, res.getPlannedQuantity());
        assertEquals(250.0, res.getCompletedQuantity());
        assertEquals(250.0, res.getBalanceQuantity());
        assertEquals(50.0, res.getActualProgressPercentage());
    }

    @Test
    @DisplayName("Should reconstruct historical cumulative progress as-of-date")
    void testGetAsOfDateCumulativeProgress() {
        LocalDate cutoff = LocalDate.of(2026, 9, 30);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));
        when(workPackageRepository.findByProjectId(1L))
                .thenReturn(List.of(mockWorkPackage));
        when(progressLogRepository.sumQuantityByActivityIdAndLogDateLessThanEqual(10L, cutoff))
                .thenReturn(150.0);
        when(progressLogRepository.sumQuantityByActivityIdAndLogDateLessThanEqual(11L, cutoff))
                .thenReturn(60.0);
        when(progressLogRepository.findByProjectIdAndLogDateLessThanEqualOrderByLogDateAsc(1L, cutoff))
                .thenReturn(Collections.emptyList());

        AsOfDateProgressResponse res = cumulativeProgressService.getAsOfDateCumulativeProgress(1L, cutoff);

        assertNotNull(res);
        assertEquals(cutoff, res.getCutoffDate());
        assertEquals(210.0, res.getTotalCompletedQuantityAsOfDate());
        assertEquals(2, res.getActivitySnapshots().size());
    }

    @Test
    @DisplayName("Should generate project cumulative S-Curve")
    void testGetProjectCumulativeSCurve() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));
        when(workPackageRepository.findByProjectId(1L))
                .thenReturn(List.of(mockWorkPackage));

        CumulativeSCurveResponse res = cumulativeProgressService.getProjectCumulativeSCurve(1L, "WEEKLY");

        assertNotNull(res);
        assertEquals("PROJECT", res.getScope());
        assertEquals(1L, res.getEntityId());
        assertFalse(res.getCurveData().isEmpty());
    }

    @Test
    @DisplayName("Should recalculate and sync project cumulative progress")
    void testRecalculateProjectCumulativeProgress() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1, mockActivity2));
        when(workPackageRepository.findByProjectId(1L))
                .thenReturn(List.of(mockWorkPackage));
        when(progressLogRepository.sumQuantityByActivityId(10L)).thenReturn(250.0);
        when(progressLogRepository.sumQuantityByActivityId(11L)).thenReturn(100.0);

        RecalculateProgressRequest req = RecalculateProgressRequest.builder()
                .weightingModel(WeightingModel.WEIGHTAGE_FACTOR)
                .syncActualDates(true)
                .autoCompleteStatus(true)
                .build();

        CumulativeProgressResponse res = cumulativeProgressService.recalculateProjectCumulativeProgress(1L, req, "admin");

        assertNotNull(res);
        verify(wbsActivityRepository, atLeastOnce()).saveAll(any());
        verify(workPackageRepository, atLeastOnce()).saveAll(any());
        verify(auditService, times(1)).logAction(eq("admin"), eq("RECALCULATE_CUMULATIVE_PROGRESS"), anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should audit project cumulative progress and flag discrepancies")
    void testAuditCumulativeProgress_WithDiscrepancy() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1));
        // verified is 280, stored is 250 -> discrepancy
        when(progressLogRepository.sumQuantityByActivityId(10L)).thenReturn(280.0);

        ProgressAuditReportResponse report = cumulativeProgressService.auditCumulativeProgress(1L);

        assertNotNull(report);
        assertEquals(1, report.getTotalActivitiesAudited());
        assertEquals(1, report.getDiscrepantActivitiesCount());
        assertEquals(1, report.getQuantityMismatchesCount());
        assertEquals("QUANTITY_MISMATCH", report.getDiscrepancies().get(0).getDiscrepancyType());
    }

    @Test
    @DisplayName("Should reconcile cumulative progress discrepancies and repair entities")
    void testReconcileCumulativeProgress() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L))
                .thenReturn(List.of(mockActivity1));
        when(progressLogRepository.sumQuantityByActivityId(10L)).thenReturn(280.0);

        ProgressReconciliationResponse res = cumulativeProgressService.reconcileCumulativeProgress(1L, "admin_user");

        assertNotNull(res);
        assertEquals(1, res.getActivitiesRepairedCount());
        assertTrue(res.getRepairedActivityCodes().contains("ACT-CIV-001"));
        assertEquals(280.0, mockActivity1.getCompletedQuantity());
        verify(wbsActivityRepository, times(1)).saveAll(any());
        verify(auditService, times(1)).logAction(eq("admin_user"), eq("RECONCILE_CUMULATIVE_PROGRESS"), anyString(), anyString(), anyString(), any());
    }
}

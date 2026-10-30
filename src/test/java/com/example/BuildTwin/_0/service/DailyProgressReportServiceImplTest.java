package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.dpr.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import org.mockito.ArgumentCaptor;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.impl.DailyProgressReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyProgressReportServiceImplTest {

    @Mock
    private DailyProgressReportRepository dprRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WbsActivityRepository wbsActivityRepository;

    @Mock
    private ActivityProgressLogRepository activityProgressLogRepository;

    @Mock
    private DprActivityItemRepository dprActivityItemRepository;

    @Mock
    private DprLaborItemRepository dprLaborItemRepository;

    @Mock
    private DprEquipmentItemRepository dprEquipmentItemRepository;

    @Mock
    private DprMaterialItemRepository dprMaterialItemRepository;

    @Mock
    private DprPhotoAttachmentRepository dprPhotoAttachmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DailyProgressReportServiceImpl dprService;

    private Project testProject;
    private WbsActivity testActivity;
    private DailyProgressReport testDpr;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .code("PRJ-SKY-001")
                .name("Skyline Towers")
                .build();

        testActivity = WbsActivity.builder()
                .id(101L)
                .project(testProject)
                .code("ACT-CIV-001")
                .name("Slab Concrete Works")
                .discipline("CIVIL")
                .uom("CUM")
                .plannedQuantity(500.0)
                .completedQuantity(100.0)
                .progressPercentage(20.0)
                .status("IN_PROGRESS")
                .build();

        testDpr = DailyProgressReport.builder()
                .id(10L)
                .reportCode("DPR-PRJ-SKY-001-20261010-DAY")
                .project(testProject)
                .reportDate(LocalDate.of(2026, 10, 10))
                .shift(WorkShift.DAY_SHIFT)
                .weatherCondition(WeatherCondition.SUNNY)
                .temperatureCelsius(32.0)
                .status(DprStatus.DRAFT)
                .siteEngineer("Engr. Rajesh")
                .generalNotes("Pour completed")
                .safetyIncidentsCount(0)
                .activityItems(new ArrayList<>())
                .laborItems(new ArrayList<>())
                .equipmentItems(new ArrayList<>())
                .materialItems(new ArrayList<>())
                .photoAttachments(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Create DPR - Success with initial line items")
    void createDpr_Success() {
        CreateDprRequest request = CreateDprRequest.builder()
                .reportDate(LocalDate.of(2026, 10, 10))
                .shift(WorkShift.DAY_SHIFT)
                .weatherCondition(WeatherCondition.SUNNY)
                .temperatureCelsius(32.0)
                .siteEngineer("Engr. Rajesh")
                .generalNotes("Initial site notes")
                .activityItems(List.of(
                        DprActivityItemRequest.builder()
                                .activityId(101L)
                                .quantityToday(50.0)
                                .locationReference("Tower A Floor 3")
                                .remarks("Pumped M35")
                                .build()
                ))
                .laborItems(List.of(
                        DprLaborItemRequest.builder()
                                .tradeCategory("Masons")
                                .headcount(8)
                                .hoursWorked(8.0)
                                .build()
                ))
                .equipmentItems(List.of(
                        DprEquipmentItemRequest.builder()
                                .equipmentName("Transit Mixer")
                                .quantity(2)
                                .operatingHours(6.0)
                                .build()
                ))
                .materialItems(List.of(
                        DprMaterialItemRequest.builder()
                                .materialName("OPC Cement")
                                .unitOfMeasure("BAGS")
                                .quantityReceived(100.0)
                                .quantityConsumed(80.0)
                                .build()
                ))
                .photoAttachments(List.of(
                        DprPhotoAttachmentRequest.builder()
                                .photoUrl("https://storage.example.com/site1.jpg")
                                .caption("Slab pour")
                                .build()
                ))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(dprRepository.existsByProjectIdAndReportDateAndShift(1L, request.getReportDate(), WorkShift.DAY_SHIFT)).thenReturn(false);
        when(dprRepository.findByReportCode(anyString())).thenReturn(Optional.empty());
        when(wbsActivityRepository.findById(101L)).thenReturn(Optional.of(testActivity));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> {
            DailyProgressReport arg = invocation.getArgument(0);
            arg.setId(10L);
            return arg;
        });

        DailyProgressReportResponse response = dprService.createDpr(1L, request, "Engr. Rajesh");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(DprStatus.DRAFT, response.getStatus());
        assertEquals(1, response.getActivityItems().size());
        assertEquals(1, response.getLaborItems().size());
        assertEquals(8, response.getTotalManpowerCount());
        verify(dprRepository).save(any(DailyProgressReport.class));
    }

    @Test
    @DisplayName("Create DPR - Throws DuplicateResourceException if exists")
    void createDpr_DuplicateThrowsException() {
        CreateDprRequest request = CreateDprRequest.builder()
                .reportDate(LocalDate.of(2026, 10, 10))
                .shift(WorkShift.DAY_SHIFT)
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(dprRepository.existsByProjectIdAndReportDateAndShift(1L, request.getReportDate(), WorkShift.DAY_SHIFT)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> dprService.createDpr(1L, request, "Engr. Rajesh"));
    }

    @Test
    @DisplayName("Get DPR By ID - Success")
    void getDprById_Success() {
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        DailyProgressReportResponse response = dprService.getDprById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("DPR-PRJ-SKY-001-20261010-DAY", response.getReportCode());
    }

    @Test
    @DisplayName("Get DPR By ID - Not Found Throws Exception")
    void getDprById_NotFound() {
        when(dprRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dprService.getDprById(999L));
    }

    @Test
    @DisplayName("Get DPRs By Project - Success")
    void getDprsByProject_Success() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(dprRepository.findByProjectIdOrderByReportDateDesc(1L)).thenReturn(List.of(testDpr));

        List<DailyProgressReportResponse> list = dprService.getDprsByProject(1L, null, null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Update DPR - Success for Draft")
    void updateDpr_Success() {
        UpdateDprRequest request = UpdateDprRequest.builder()
                .weatherCondition(WeatherCondition.CLOUDY)
                .temperatureCelsius(29.0)
                .generalNotes("Updated notes after inspection")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.updateDpr(10L, request, "Engr. Rajesh");

        assertNotNull(response);
        assertEquals(WeatherCondition.CLOUDY, response.getWeatherCondition());
        assertEquals(29.0, response.getTemperatureCelsius());
        assertEquals("Updated notes after inspection", response.getGeneralNotes());
    }

    @Test
    @DisplayName("Update DPR - Throws BadRequestException for Approved DPR")
    void updateDpr_ApprovedThrowsException() {
        testDpr.setStatus(DprStatus.APPROVED);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        UpdateDprRequest request = UpdateDprRequest.builder().generalNotes("Edit attempt").build();
        assertThrows(BadRequestException.class, () -> dprService.updateDpr(10L, request, "user"));
    }

    @Test
    @DisplayName("Delete DPR - Success for Draft")
    void deleteDpr_Success() {
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        dprService.deleteDpr(10L);

        verify(dprRepository).delete(testDpr);
    }

    @Test
    @DisplayName("Delete DPR - Throws BadRequestException for Approved DPR")
    void deleteDpr_ApprovedThrowsException() {
        testDpr.setStatus(DprStatus.APPROVED);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.deleteDpr(10L));
    }

    @Test
    @DisplayName("Line Items - Add & Remove Activity Item")
    void addAndRemoveActivityItem_Success() {
        DprActivityItemRequest addReq = DprActivityItemRequest.builder()
                .activityId(101L)
                .quantityToday(30.0)
                .locationReference("Zone A")
                .remarks("Cast column")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(wbsActivityRepository.findById(101L)).thenReturn(Optional.of(testActivity));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.addActivityItem(10L, addReq);
        assertEquals(1, response.getActivityItems().size());

        // Now test remove
        testDpr.getActivityItems().get(0).setId(55L);
        DailyProgressReportResponse afterRemove = dprService.removeActivityItem(10L, 55L);
        assertEquals(0, afterRemove.getActivityItems().size());
    }

    @Test
    @DisplayName("Activity Items - Get All Items in DPR")
    void getActivityItems_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(20.0)
                .build();
        testDpr.getActivityItems().add(item);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        List<DprActivityItemResponse> items = dprService.getActivityItems(10L);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(20.0, items.get(0).getQuantityToday());
    }

    @Test
    @DisplayName("Activity Items - Get Item by ID")
    void getActivityItemById_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(20.0)
                .build();
        testDpr.getActivityItems().add(item);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        DprActivityItemResponse response = dprService.getActivityItemById(10L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(20.0, response.getQuantityToday());
    }

    @Test
    @DisplayName("Activity Items - Update Item in Draft DPR")
    void updateActivityItem_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(20.0)
                .build();
        testDpr.getActivityItems().add(item);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateDprActivityItemRequest updateReq = UpdateDprActivityItemRequest.builder()
                .quantityToday(40.0)
                .locationReference("Level 4")
                .remarks("Updated remarks")
                .build();

        DprActivityItemResponse response = dprService.updateActivityItem(10L, 1L, updateReq);

        assertNotNull(response);
        assertEquals(40.0, response.getQuantityToday());
        assertEquals("Level 4", response.getLocationReference());
        assertEquals("Updated remarks", response.getRemarks());
    }

    @Test
    @DisplayName("Activity Items - Get Activity DPR Progress History")
    void getActivityDprHistory_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(20.0)
                .build();
        when(wbsActivityRepository.existsById(101L)).thenReturn(true);
        when(dprActivityItemRepository.findByActivityId(101L)).thenReturn(List.of(item));

        List<DprActivityItemResponse> history = dprService.getActivityDprHistory(101L);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(20.0, history.get(0).getQuantityToday());
    }

    @Test
    @DisplayName("Line Items - Add & Remove Labor Item")
    void addAndRemoveLaborItem_Success() {
        DprLaborItemRequest laborReq = DprLaborItemRequest.builder()
                .tradeCategory("Carpenters")
                .headcount(6)
                .hoursWorked(8.0)
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.addLaborItem(10L, laborReq);
        assertEquals(1, response.getLaborItems().size());
        assertEquals(6, response.getTotalManpowerCount());

        testDpr.getLaborItems().get(0).setId(66L);
        DailyProgressReportResponse afterRemove = dprService.removeLaborItem(10L, 66L);
        assertEquals(0, afterRemove.getLaborItems().size());
        assertEquals(0, afterRemove.getTotalManpowerCount());
    }

    @Test
    @DisplayName("Line Items - Add & Remove Equipment Item")
    void addAndRemoveEquipmentItem_Success() {
        DprEquipmentItemRequest equipReq = DprEquipmentItemRequest.builder()
                .equipmentName("JCB Excavator")
                .quantity(1)
                .operatingHours(7.5)
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.addEquipmentItem(10L, equipReq);
        assertEquals(1, response.getEquipmentItems().size());

        testDpr.getEquipmentItems().get(0).setId(77L);
        DailyProgressReportResponse afterRemove = dprService.removeEquipmentItem(10L, 77L);
        assertEquals(0, afterRemove.getEquipmentItems().size());
    }

    @Test
    @DisplayName("Line Items - Add & Remove Material Item")
    void addAndRemoveMaterialItem_Success() {
        DprMaterialItemRequest matReq = DprMaterialItemRequest.builder()
                .materialName("Steel TMT 16mm")
                .unitOfMeasure("MT")
                .quantityReceived(10.0)
                .quantityConsumed(5.0)
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.addMaterialItem(10L, matReq);
        assertEquals(1, response.getMaterialItems().size());

        testDpr.getMaterialItems().get(0).setId(88L);
        DailyProgressReportResponse afterRemove = dprService.removeMaterialItem(10L, 88L);
        assertEquals(0, afterRemove.getMaterialItems().size());
    }

    @Test
    @DisplayName("Line Items - Add & Remove Photo Attachment")
    void addAndRemovePhotoAttachment_Success() {
        DprPhotoAttachmentRequest photoReq = DprPhotoAttachmentRequest.builder()
                .photoUrl("https://storage.example.com/site_inspection.jpg")
                .caption("Safety check")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.addPhotoAttachment(10L, photoReq);
        assertEquals(1, response.getPhotoAttachments().size());

        testDpr.getPhotoAttachments().get(0).setId(99L);
        DailyProgressReportResponse afterRemove = dprService.removePhotoAttachment(10L, 99L);
        assertEquals(0, afterRemove.getPhotoAttachments().size());
    }

    // =========================================================================
    // WORKFLOW TRANSITION TESTS (DRAFT, SUBMIT, WITHDRAW, APPROVE, REJECT)
    // =========================================================================

    @Test
    @DisplayName("Submit DPR - Success Transitions to SUBMITTED")
    void submitDpr_Success() {
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.submitDpr(10L, "Engr. Rajesh");

        assertEquals(DprStatus.SUBMITTED, response.getStatus());
        assertEquals("Engr. Rajesh", response.getSubmittedBy());
        assertNotNull(response.getSubmissionDate());
        verify(dprRepository).save(testDpr);
    }

    @Test
    @DisplayName("Submit DPR - With Notes Success and Clears Rejection Status")
    void submitDpr_WithNotes_Success() {
        testDpr.setStatus(DprStatus.REJECTED);
        testDpr.setRejectionReason("Incorrect concrete batch slips");
        testDpr.setRejectedBy("PM Senthil");
        testDpr.setRejectionDate(LocalDateTime.now());

        SubmitDprRequest submitReq = SubmitDprRequest.builder()
                .submissionNotes("Revised batch slips attached")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.submitDpr(10L, submitReq, "Engr. Rajesh");

        assertEquals(DprStatus.SUBMITTED, response.getStatus());
        assertEquals("Engr. Rajesh", response.getSubmittedBy());
        assertEquals("Revised batch slips attached", response.getSubmissionNotes());
        assertNull(response.getRejectionReason());
        assertNull(response.getRejectedBy());
        assertNull(response.getRejectionDate());
    }

    @Test
    @DisplayName("Submit DPR - Already Approved Throws BadRequestException")
    void submitDpr_AlreadyApproved_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.APPROVED);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.submitDpr(10L, "Engr. Rajesh"));
    }

    @Test
    @DisplayName("Submit DPR - Already Submitted Throws BadRequestException")
    void submitDpr_AlreadySubmitted_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.SUBMITTED);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.submitDpr(10L, "Engr. Rajesh"));
    }

    @Test
    @DisplayName("Withdraw DPR - Reverts SUBMITTED DPR back to DRAFT")
    void withdrawDpr_Success() {
        testDpr.setStatus(DprStatus.SUBMITTED);
        testDpr.setSubmittedBy("Engr. Rajesh");
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawDprRequest withdrawReq = WithdrawDprRequest.builder()
                .withdrawalReason("Need to append evening shift concrete cubes")
                .build();

        DailyProgressReportResponse response = dprService.withdrawDpr(10L, withdrawReq, "Engr. Rajesh");

        assertEquals(DprStatus.DRAFT, response.getStatus());
        assertTrue(response.getGeneralNotes().contains("Need to append evening shift concrete cubes"));
        verify(dprRepository).save(testDpr);
    }

    @Test
    @DisplayName("Withdraw DPR - Non-Submitted Status Throws BadRequestException")
    void withdrawDpr_DraftOrApproved_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.DRAFT);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.withdrawDpr(10L, "Engr. Rajesh"));
    }

    @Test
    @DisplayName("Approve DPR - Cascades progress to WBS Activity and saves ActivityProgressLog")
    void approveDpr_CascadesToWbsAndLogsProgress() {
        testDpr.setStatus(DprStatus.SUBMITTED);
        DprActivityItem actItem = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(150.0)
                .locationReference("Level 3")
                .build();
        testDpr.getActivityItems().add(actItem);

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(wbsActivityRepository.save(any(WbsActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(activityProgressLogRepository.save(any(ActivityProgressLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.approveDpr(10L, "PM Senthil");

        assertEquals(DprStatus.APPROVED, response.getStatus());
        assertEquals("PM Senthil", response.getApprovedBy());
        assertNotNull(response.getApprovalDate());

        // Verify WBS activity was updated: 100 + 150 = 250 out of 500 = 50.0%
        assertEquals(250.0, testActivity.getCompletedQuantity());
        assertEquals(50.0, testActivity.getProgressPercentage());
        assertEquals("IN_PROGRESS", testActivity.getStatus());

        verify(wbsActivityRepository).save(testActivity);
        verify(activityProgressLogRepository).save(any(ActivityProgressLog.class));
    }

    @Test
    @DisplayName("Approve DPR - With Approval Notes Success")
    void approveDpr_WithNotes_Success() {
        testDpr.setStatus(DprStatus.SUBMITTED);
        ApproveDprRequest approveReq = ApproveDprRequest.builder()
                .approvalNotes("Verified with QA/QC lab results")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.approveDpr(10L, approveReq, "PM Senthil");

        assertEquals(DprStatus.APPROVED, response.getStatus());
        assertEquals("PM Senthil", response.getApprovedBy());
        assertEquals("Verified with QA/QC lab results", response.getApprovalNotes());
    }

    @Test
    @DisplayName("Approve DPR - Already Approved Throws BadRequestException")
    void approveDpr_AlreadyApproved_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.APPROVED);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.approveDpr(10L, "PM Senthil"));
    }

    @Test
    @DisplayName("Approve DPR - Rejected Without Resubmitting Throws BadRequestException")
    void approveDpr_RejectedWithoutResubmit_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.REJECTED);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.approveDpr(10L, "PM Senthil"));
    }

    @Test
    @DisplayName("Reject DPR - Success with documented reason")
    void rejectDpr_Success() {
        testDpr.setStatus(DprStatus.SUBMITTED);
        RejectDprRequest rejectReq = RejectDprRequest.builder()
                .rejectionReason("Mismatch in concrete batch slips")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyProgressReportResponse response = dprService.rejectDpr(10L, rejectReq, "PM Senthil");

        assertEquals(DprStatus.REJECTED, response.getStatus());
        assertEquals("Mismatch in concrete batch slips", response.getRejectionReason());
        assertEquals("PM Senthil", response.getRejectedBy());
        assertNotNull(response.getRejectionDate());
    }

    @Test
    @DisplayName("Reject DPR - Draft DPR Throws BadRequestException")
    void rejectDpr_Draft_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.DRAFT);
        RejectDprRequest rejectReq = RejectDprRequest.builder()
                .rejectionReason("Incomplete data")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.rejectDpr(10L, rejectReq, "PM Senthil"));
    }

    @Test
    @DisplayName("Reject DPR - Approved DPR Throws BadRequestException")
    void rejectDpr_Approved_ThrowsBadRequest() {
        testDpr.setStatus(DprStatus.APPROVED);
        RejectDprRequest rejectReq = RejectDprRequest.builder()
                .rejectionReason("Change of scope")
                .build();

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        assertThrows(BadRequestException.class, () -> dprService.rejectDpr(10L, rejectReq, "PM Senthil"));
    }

    @Test
    @DisplayName("Get DPR Summary - Success")
    void getDprSummary_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(dprRepository.countByProjectId(1L)).thenReturn(20L);
        when(dprRepository.countByProjectIdAndStatus(1L, DprStatus.DRAFT)).thenReturn(2L);
        when(dprRepository.countByProjectIdAndStatus(1L, DprStatus.SUBMITTED)).thenReturn(3L);
        when(dprRepository.countByProjectIdAndStatus(1L, DprStatus.APPROVED)).thenReturn(14L);
        when(dprRepository.countByProjectIdAndStatus(1L, DprStatus.REJECTED)).thenReturn(1L);
        when(dprRepository.sumManpowerByProjectId(1L)).thenReturn(850L);
        when(dprRepository.sumSafetyIncidentsByProjectId(1L)).thenReturn(0L);
        when(dprRepository.findTopByProjectIdOrderByReportDateDesc(1L)).thenReturn(Optional.of(testDpr));

        DprSummaryResponse summary = dprService.getDprSummary(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getProjectId());
        assertEquals(20L, summary.getTotalReports());
        assertEquals(14L, summary.getApprovedReports());
        assertEquals(850L, summary.getCumulativeManpowerLogged());
    }

    @Test
    @DisplayName("Quantity Tracking - Get DPR Quantity Tracking Summary")
    void getDprQuantityTracking_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(50.0)
                .cumulativeQuantity(100.0)
                .build();
        testDpr.getActivityItems().add(item);
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        DprQuantityTrackingSummaryResponse summary = dprService.getDprQuantityTracking(10L);

        assertNotNull(summary);
        assertEquals(10L, summary.getDprId());
        assertEquals(1, summary.getTotalActivitiesLogged());
        assertEquals(1, summary.getActivityQuantities().size());
        ActivityQuantityTrackingResponse line = summary.getActivityQuantities().get(0);
        assertEquals(500.0, line.getPlannedQuantity());
        assertEquals(50.0, line.getQuantityToday());
        assertEquals(100.0, line.getCumulativeQuantity());
        assertEquals(50.0, line.getPreviousQuantity());
        assertEquals(400.0, line.getBalanceQuantity());
    }

    @Test
    @DisplayName("Quantity Tracking - Get Activity Quantity Tracking Analytics")
    void getActivityQuantityTracking_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(50.0)
                .cumulativeQuantity(100.0)
                .build();
        when(wbsActivityRepository.findById(101L)).thenReturn(Optional.of(testActivity));
        when(dprActivityItemRepository.findByActivityId(101L)).thenReturn(List.of(item));

        ActivityQuantityTrackingResponse response = dprService.getActivityQuantityTracking(101L);

        assertNotNull(response);
        assertEquals(101L, response.getActivityId());
        assertEquals("ACT-CIV-001", response.getActivityCode());
        assertEquals(500.0, response.getPlannedQuantity());
        assertEquals(100.0, response.getCumulativeQuantity());
        assertEquals(400.0, response.getBalanceQuantity());
        assertEquals(1, response.getProgressHistory().size());
    }

    @Test
    @DisplayName("Quantity Tracking - Get Project Wide Quantity Tracking Dashboard")
    void getProjectQuantityTracking_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(testActivity));

        ProjectQuantityTrackingResponse response = dprService.getProjectQuantityTracking(1L, null, null);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals(1, response.getTotalActivities());
        assertEquals(1, response.getActivities().size());
        assertFalse(response.getDisciplineRollups().isEmpty());
    }

    // =========================================================================
    // BLOCKER & REMARKS SUPPORT TESTS
    // =========================================================================

    @Test
    @DisplayName("Blocker & Remarks - Add Activity Item with Blocker and Remarks")
    void addActivityItem_WithBlockerAndRemarks_Success() {
        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(wbsActivityRepository.findById(101L)).thenReturn(Optional.of(testActivity));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DprActivityItemRequest request = DprActivityItemRequest.builder()
                .activityId(101L)
                .quantityToday(15.0)
                .locationReference("Tower A - 3rd Floor")
                .remarks("Concrete pump jammed; waiting for maintenance crew")
                .isBlocker(true)
                .blockerCategory(BlockerCategory.EQUIPMENT_BREAKDOWN)
                .blockerReason("Hydraulic line burst on 36m concrete pump")
                .blockerSeverity(BlockerSeverity.CRITICAL)
                .impactHours(3.5)
                .build();

        DailyProgressReportResponse response = dprService.addActivityItem(10L, request);

        assertNotNull(response);
        assertEquals(1, response.getActivityItems().size());
        assertEquals(1, response.getTotalBlockersCount());
        assertEquals(1, response.getOpenBlockersCount());

        DprActivityItemResponse itemResp = response.getActivityItems().get(0);
        assertTrue(itemResp.getIsBlocker());
        assertEquals(BlockerCategory.EQUIPMENT_BREAKDOWN, itemResp.getBlockerCategory());
        assertEquals("Hydraulic line burst on 36m concrete pump", itemResp.getBlockerReason());
        assertEquals(BlockerSeverity.CRITICAL, itemResp.getBlockerSeverity());
        assertEquals(BlockerStatus.OPEN, itemResp.getBlockerStatus());
        assertEquals(3.5, itemResp.getImpactHours());
        assertEquals("Concrete pump jammed; waiting for maintenance crew", itemResp.getRemarks());
    }

    @Test
    @DisplayName("Blocker & Remarks - Update Activity Blocker and Remarks")
    void updateActivityItem_BlockerAndRemarks_Success() {
        DprActivityItem item = DprActivityItem.builder()
                .id(55L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(10.0)
                .cumulativeQuantity(110.0)
                .isBlocker(true)
                .blockerStatus(BlockerStatus.OPEN)
                .blockerCategory(BlockerCategory.MATERIAL_SHORTAGE)
                .blockerReason("Shortage of rebar 16mm")
                .remarks("Initial delay observed")
                .build();
        testDpr.getActivityItems().add(item);

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateDprActivityItemRequest updateReq = UpdateDprActivityItemRequest.builder()
                .remarks("Partial shipment received; bending started")
                .blockerStatus(BlockerStatus.IN_PROGRESS)
                .blockerSeverity(BlockerSeverity.MEDIUM)
                .impactHours(1.5)
                .build();

        DprActivityItemResponse updated = dprService.updateActivityItem(10L, 55L, updateReq);

        assertNotNull(updated);
        assertEquals(BlockerStatus.IN_PROGRESS, updated.getBlockerStatus());
        assertEquals(BlockerSeverity.MEDIUM, updated.getBlockerSeverity());
        assertEquals(1.5, updated.getImpactHours());
        assertEquals("Partial shipment received; bending started", updated.getRemarks());
    }

    @Test
    @DisplayName("Blocker & Remarks - Get DPR Blockers")
    void getDprBlockers_Success() {
        DprActivityItem normalItem = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(40.0)
                .isBlocker(false)
                .remarks("Normal pour")
                .build();

        DprActivityItem blockerItem = DprActivityItem.builder()
                .id(2L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(5.0)
                .isBlocker(true)
                .blockerCategory(BlockerCategory.SITE_ACCESS)
                .blockerSeverity(BlockerSeverity.HIGH)
                .blockerStatus(BlockerStatus.OPEN)
                .blockerReason("Access road waterlogged after rain")
                .remarks("Dewatering pumps deployed")
                .build();

        testDpr.getActivityItems().add(normalItem);
        testDpr.getActivityItems().add(blockerItem);

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));

        List<DprBlockerItemResponse> blockers = dprService.getDprBlockers(10L);

        assertEquals(1, blockers.size());
        DprBlockerItemResponse blockerResp = blockers.get(0);
        assertEquals(2L, blockerResp.getItemId());
        assertTrue(blockerResp.getIsBlocker());
        assertEquals(BlockerCategory.SITE_ACCESS, blockerResp.getBlockerCategory());
        assertEquals("Access road waterlogged after rain", blockerResp.getBlockerReason());
        assertEquals("Dewatering pumps deployed", blockerResp.getRemarks());
    }

    @Test
    @DisplayName("Blocker & Remarks - Get Project DPR Blockers with Filters")
    void getProjectDprBlockers_Success() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        DprActivityItem blockerItem = DprActivityItem.builder()
                .id(101L)
                .dpr(testDpr)
                .activity(testActivity)
                .isBlocker(true)
                .blockerCategory(BlockerCategory.DESIGN_CLARIFICATION)
                .blockerSeverity(BlockerSeverity.HIGH)
                .blockerStatus(BlockerStatus.OPEN)
                .blockerReason("Structural drawing discrepancy on beam B-12")
                .remarks("RFI submitted to consultant")
                .build();

        when(dprActivityItemRepository.findProjectBlockersWithFilters(
                1L, BlockerStatus.OPEN, BlockerCategory.DESIGN_CLARIFICATION, BlockerSeverity.HIGH, 101L))
                .thenReturn(List.of(blockerItem));

        List<DprBlockerItemResponse> list = dprService.getProjectDprBlockers(
                1L, BlockerStatus.OPEN, BlockerCategory.DESIGN_CLARIFICATION, BlockerSeverity.HIGH, 101L);

        assertEquals(1, list.size());
        assertEquals("Structural drawing discrepancy on beam B-12", list.get(0).getBlockerReason());
        assertEquals("RFI submitted to consultant", list.get(0).getRemarks());
    }

    @Test
    @DisplayName("Blocker & Remarks - Get Project Blocker Summary")
    void getProjectBlockerSummary_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        DprActivityItem b1 = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .isBlocker(true)
                .blockerCategory(BlockerCategory.MATERIAL_SHORTAGE)
                .blockerSeverity(BlockerSeverity.CRITICAL)
                .blockerStatus(BlockerStatus.OPEN)
                .impactHours(4.0)
                .build();

        DprActivityItem b2 = DprActivityItem.builder()
                .id(2L)
                .dpr(testDpr)
                .activity(testActivity)
                .isBlocker(true)
                .blockerCategory(BlockerCategory.EQUIPMENT_BREAKDOWN)
                .blockerSeverity(BlockerSeverity.HIGH)
                .blockerStatus(BlockerStatus.RESOLVED)
                .impactHours(2.5)
                .build();

        when(dprActivityItemRepository.findByDprProjectIdAndIsBlockerTrueOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(b1, b2));

        DprBlockerSummaryResponse summary = dprService.getProjectBlockerSummary(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getProjectId());
        assertEquals(2, summary.getTotalBlockersCount());
        assertEquals(1, summary.getOpenBlockersCount());
        assertEquals(1, summary.getResolvedBlockersCount());
        assertEquals(1, summary.getCriticalBlockersCount());
        assertEquals(6.5, summary.getTotalImpactHours());
        assertEquals(1L, summary.getCategoryBreakdown().get("MATERIAL_SHORTAGE"));
        assertEquals(1L, summary.getCategoryBreakdown().get("EQUIPMENT_BREAKDOWN"));
    }

    @Test
    @DisplayName("Blocker & Remarks - Resolve Blocker")
    void resolveBlocker_Success() {
        DprActivityItem blockerItem = DprActivityItem.builder()
                .id(77L)
                .dpr(testDpr)
                .activity(testActivity)
                .isBlocker(true)
                .blockerCategory(BlockerCategory.PERMIT_APPROVAL)
                .blockerSeverity(BlockerSeverity.HIGH)
                .blockerStatus(BlockerStatus.OPEN)
                .blockerReason("Hot work permit pending clearance from safety officer")
                .remarks("Standby fire extinguisher arranged")
                .build();
        testDpr.getActivityItems().add(blockerItem);

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResolveDprBlockerRequest request = ResolveDprBlockerRequest.builder()
                .resolutionNotes("Safety officer inspected gas torches and signed hot work permit #HW-2026-99")
                .status(BlockerStatus.RESOLVED)
                .remarks("Welding works commenced at 11:30 AM")
                .build();

        DprBlockerItemResponse response = dprService.resolveBlocker(10L, 77L, request, "Safety Lead Arun");

        assertNotNull(response);
        assertEquals(BlockerStatus.RESOLVED, response.getBlockerStatus());
        assertEquals("Safety Lead Arun", response.getBlockerResolvedBy());
        assertNotNull(response.getBlockerResolvedAt());
        assertEquals("Safety officer inspected gas torches and signed hot work permit #HW-2026-99", response.getBlockerResolutionNotes());
        assertEquals("Welding works commenced at 11:30 AM", response.getRemarks());
    }

    @Test
    @DisplayName("Blocker & Remarks - Approval cascades Blocker and Remarks to ActivityProgressLog")
    void approveDpr_CascadesBlockerAndRemarksToActivityProgressLog() {
        testDpr.setStatus(DprStatus.SUBMITTED);
        DprActivityItem actItem = DprActivityItem.builder()
                .id(1L)
                .dpr(testDpr)
                .activity(testActivity)
                .quantityToday(50.0)
                .locationReference("Level 4")
                .remarks("Partial pour completed before lightning alert")
                .isBlocker(true)
                .blockerCategory(BlockerCategory.WEATHER_DELAY)
                .blockerReason("Thunderstorm and lightning alert halted tower crane operations")
                .build();
        testDpr.getActivityItems().add(actItem);

        when(dprRepository.findById(10L)).thenReturn(Optional.of(testDpr));
        when(wbsActivityRepository.save(any(WbsActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(activityProgressLogRepository.save(any(ActivityProgressLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dprRepository.save(any(DailyProgressReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dprService.approveDpr(10L, "PM Senthil");

        ArgumentCaptor<ActivityProgressLog> logCaptor = ArgumentCaptor.forClass(ActivityProgressLog.class);
        verify(activityProgressLogRepository).save(logCaptor.capture());

        ActivityProgressLog savedLog = logCaptor.getValue();
        assertNotNull(savedLog);
        assertTrue(savedLog.getIsBlocker());
        assertEquals("WEATHER_DELAY", savedLog.getBlockerCategory());
        assertEquals("Thunderstorm and lightning alert halted tower crane operations", savedLog.getBlockerReason());
        assertEquals("Partial pour completed before lightning alert", savedLog.getRemarks());
    }
}

package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.dpr.*;
import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import com.example.BuildTwin._0.model.DprStatus;

import java.time.LocalDate;
import java.util.List;

public interface DailyProgressReportService {

    DailyProgressReportResponse createDpr(Long projectId, CreateDprRequest request, String currentUser);

    DailyProgressReportResponse getDprById(Long dprId);

    List<DailyProgressReportResponse> getDprsByProject(
            Long projectId, LocalDate startDate, LocalDate endDate, DprStatus status);

    DailyProgressReportResponse updateDpr(Long dprId, UpdateDprRequest request, String currentUser);

    void deleteDpr(Long dprId);

    // --- DPR Activity Items Management ---
    DailyProgressReportResponse addActivityItem(Long dprId, DprActivityItemRequest request);

    DailyProgressReportResponse removeActivityItem(Long dprId, Long itemId);

    List<DprActivityItemResponse> getActivityItems(Long dprId);

    DprActivityItemResponse getActivityItemById(Long dprId, Long itemId);

    DprActivityItemResponse updateActivityItem(Long dprId, Long itemId, UpdateDprActivityItemRequest request);

    List<DprActivityItemResponse> getActivityDprHistory(Long activityId);

    // --- Blocker & Remarks Support ---
    List<DprBlockerItemResponse> getDprBlockers(Long dprId);

    List<DprBlockerItemResponse> getProjectDprBlockers(
            Long projectId, BlockerStatus status, BlockerCategory category, BlockerSeverity severity, Long activityId);

    DprBlockerSummaryResponse getProjectBlockerSummary(Long projectId);

    DprBlockerItemResponse resolveBlocker(Long dprId, Long itemId, ResolveDprBlockerRequest request, String resolver);

    // --- Activity Quantity Tracking ---
    DprQuantityTrackingSummaryResponse getDprQuantityTracking(Long dprId);

    ActivityQuantityTrackingResponse getActivityQuantityTracking(Long activityId);

    ProjectQuantityTrackingResponse getProjectQuantityTracking(Long projectId, String discipline, String status);

    // --- Other Line Items Management ---
    DailyProgressReportResponse addLaborItem(Long dprId, DprLaborItemRequest request);

    DailyProgressReportResponse removeLaborItem(Long dprId, Long itemId);

    DailyProgressReportResponse addEquipmentItem(Long dprId, DprEquipmentItemRequest request);

    DailyProgressReportResponse removeEquipmentItem(Long dprId, Long itemId);

    DailyProgressReportResponse addMaterialItem(Long dprId, DprMaterialItemRequest request);

    DailyProgressReportResponse removeMaterialItem(Long dprId, Long itemId);

    DailyProgressReportResponse addPhotoAttachment(Long dprId, DprPhotoAttachmentRequest request);

    DailyProgressReportResponse removePhotoAttachment(Long dprId, Long attachmentId);

    // --- Workflow Transitions ---
    DailyProgressReportResponse submitDpr(Long dprId, String submitter);

    DailyProgressReportResponse approveDpr(Long dprId, String approver);

    DailyProgressReportResponse rejectDpr(Long dprId, RejectDprRequest request, String reviewer);

    // --- Executive Summary & KPIs ---
    DprSummaryResponse getDprSummary(Long projectId);
}

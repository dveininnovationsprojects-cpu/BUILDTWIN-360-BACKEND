package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.progress.*;

import java.time.LocalDate;

public interface ProgressHistoryService {

    /**
     * Retrieves full chronological progress history and summary analytics for a specific WBS activity.
     */
    ActivityProgressHistoryResponse getActivityProgressHistory(
            Long activityId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            String sortDir
    );

    /**
     * Retrieves paginated progress history items for a specific WBS activity.
     */
    PageResponse<ProgressHistoryItemResponse> getActivityProgressHistoryPaginated(
            Long activityId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            int page,
            int size,
            String sortDir
    );

    /**
     * Retrieves project-wide progress history register, executive rollups, and paginated audit entries.
     */
    ProjectProgressHistoryResponse getProjectProgressHistory(
            Long projectId,
            Long workPackageId,
            Long activityId,
            String discipline,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    /**
     * Retrieves progress history entries and performance metrics for a specific work package.
     */
    WorkPackageProgressHistoryResponse getWorkPackageProgressHistory(
            Long workPackageId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isBlocker,
            String source,
            int page,
            int size,
            String sortDir
    );

    /**
     * Computes deep velocity analytics, day-by-day S-curve progression, and run rate projections.
     */
    ProgressAnalyticsResponse getActivityProgressAnalytics(Long activityId);

    /**
     * Retrieves a single progress history log entry by ID.
     */
    ProgressHistoryItemResponse getProgressHistoryItemById(Long logId);

    /**
     * Logs physical site progress directly for an activity, cascades completed quantities, and updates WBS rollups.
     */
    ProgressHistoryItemResponse logProgress(Long activityId, LogProgressHistoryRequest request, String recordedBy);

    /**
     * Updates remarks, hindrances, manpower, equipment, or adjusts quantity on an existing progress history record.
     */
    ProgressHistoryItemResponse updateProgressLog(Long logId, UpdateProgressHistoryRequest request, String performedBy);

    /**
     * Deletes a progress history entry, recalculates cumulative quantity and progress % on the activity, and logs audit trail.
     */
    void deleteProgressLog(Long logId, String performedBy);
}

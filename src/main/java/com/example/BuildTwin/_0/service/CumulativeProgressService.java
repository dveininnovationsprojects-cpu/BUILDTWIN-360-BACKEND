package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.cumulative.*;

import java.time.LocalDate;

public interface CumulativeProgressService {

    /**
     * Calculates master project-level cumulative progress with multi-model weightings, EVM metrics, and multi-dimensional rollups.
     */
    CumulativeProgressResponse getProjectCumulativeProgress(
            Long projectId,
            WeightingModel weightingModel,
            String discipline,
            Long workPackageId);

    /**
     * Calculates work package level cumulative progress with weightage breakdown and subtask contributions.
     */
    WorkPackageCumulativeProgressResponse getWorkPackageCumulativeProgress(
            Long workPackageId,
            WeightingModel weightingModel);

    /**
     * Calculates activity-level cumulative progress, planned vs actual delta, and recursive child tree rollup.
     */
    ActivityCumulativeProgressResponse getActivityCumulativeProgress(Long activityId);

    /**
     * Historical Point-in-Time cumulative progress reconstruction as of any historical cutoff date.
     */
    AsOfDateProgressResponse getAsOfDateCumulativeProgress(Long projectId, LocalDate cutoffDate);

    /**
     * Historical Point-in-Time cumulative progress for a single activity as of a cutoff date.
     */
    AsOfDateProgressResponse.ActivityAsOfDateSnapshot getActivityAsOfDateProgress(Long activityId, LocalDate cutoffDate);

    /**
     * Generates cumulative S-Curve time-series data points (Daily, Weekly, Monthly) and Earned Value curves.
     */
    CumulativeSCurveResponse getProjectCumulativeSCurve(Long projectId, String interval);

    /**
     * Generates cumulative S-Curve time-series data points for a single activity.
     */
    CumulativeSCurveResponse getActivityCumulativeSCurve(Long activityId, String interval);

    /**
     * Executes an on-demand bottom-up hierarchical recalculation of cumulative progress across the entire project WBS tree.
     */
    CumulativeProgressResponse recalculateProjectCumulativeProgress(
            Long projectId,
            RecalculateProgressRequest request,
            String performedBy);

    /**
     * Executes an on-demand recalculation of cumulative progress for a work package and its activities.
     */
    WorkPackageCumulativeProgressResponse recalculateWorkPackageCumulativeProgress(
            Long workPackageId,
            RecalculateProgressRequest request,
            String performedBy);

    /**
     * Recalculates cumulative progress for a single activity sub-tree.
     */
    ActivityCumulativeProgressResponse recalculateActivityCumulativeProgress(Long activityId, String performedBy);

    /**
     * Performs a comprehensive integrity audit comparing stored activity cumulative quantities against verified progress logs.
     */
    ProgressAuditReportResponse auditCumulativeProgress(Long projectId);

    /**
     * Reconciles and auto-heals detected cumulative progress discrepancies across the project.
     */
    ProgressReconciliationResponse reconcileCumulativeProgress(Long projectId, String performedBy);
}

package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.lookahead.*;
import com.example.BuildTwin._0.model.CommitmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface LookaheadScheduleService {

    /**
     * Retrieves the comprehensive 14-Day (Two-Week / Sprint) Look-Ahead Schedule for a project.
     * Features dual-phase breakdown:
     * - Week 1 (Days 1 to 7): Immediate Weekly Work Plan commitments and daily execution matrix.
     * - Week 2 (Days 8 to 14): Lookahead screening, material staging, and constraint removal checklist.
     */
    FourteenDayLookaheadResponse getFourteenDayLookaheadSchedule(
            Long projectId,
            LocalDate startDate,
            Long workPackageId,
            Long siteId,
            Long buildingId,
            String discipline,
            String status,
            Boolean includeOverdue
    );

    /**
     * Retrieves 14-Day Lookahead KPIs and metrics comparing Week 1 vs Week 2 readiness and workload.
     */
    FourteenDayMetricsResponse getFourteenDayMetrics(Long projectId, LocalDate startDate);

    /**
     * Retrieves the constraint and hindrance analysis for the full 14-day lookahead window.
     */
    LookaheadConstraintSummaryResponse getFourteenDayConstraints(Long projectId, LocalDate startDate);

    /**
     * Retrieves the Look-Ahead Schedule (default 7 days, or custom window) for a project,
     * including aggregated KPIs, daily schedule matrix, and detailed activity cards with predecessor & constraint statuses.
     */
    LookaheadScheduleResponse getLookaheadSchedule(
            Long projectId,
            LocalDate startDate,
            Integer days,
            Long workPackageId,
            Long siteId,
            Long buildingId,
            String discipline,
            String status,
            Boolean includeOverdue
    );

    /**
     * Retrieves high-level Lookahead KPIs and readiness metrics for a given time window.
     */
    LookaheadMetricsResponse getLookaheadMetrics(Long projectId, LocalDate startDate, Integer days);

    /**
     * Retrieves the constraint and hindrance analysis for activities falling in the lookahead window.
     */
    LookaheadConstraintSummaryResponse getLookaheadConstraints(Long projectId, LocalDate startDate, Integer days);

    /**
     * Commits an activity into the weekly work plan / lookahead schedule with a target quantity and readiness checklist.
     */
    LookaheadCommitmentResponse createCommitment(Long projectId, CreateLookaheadCommitmentRequest request, String performedBy);

    /**
     * Retrieves all weekly commitments for a project with optional filters.
     */
    List<LookaheadCommitmentResponse> getCommitments(Long projectId, LocalDate weekStartDate, CommitmentStatus status);

    /**
     * Retrieves a single commitment by its ID.
     */
    LookaheadCommitmentResponse getCommitmentById(Long commitmentId);

    /**
     * Updates target quantity, actual achievement, readiness checklist, or notes on an existing commitment.
     */
    LookaheadCommitmentResponse updateCommitment(Long commitmentId, UpdateLookaheadCommitmentRequest request, String performedBy);

    /**
     * Deletes a lookahead commitment.
     */
    void deleteCommitment(Long commitmentId, String performedBy);
}

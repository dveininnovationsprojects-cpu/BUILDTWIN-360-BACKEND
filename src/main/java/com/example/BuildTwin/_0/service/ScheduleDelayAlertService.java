package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.alert.*;
import com.example.BuildTwin._0.model.DelayAlertStatus;
import com.example.BuildTwin._0.model.DelayAlertType;
import com.example.BuildTwin._0.model.OverdueAgingBucket;
import com.example.BuildTwin._0.model.OverdueSeverity;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleDelayAlertService {

    /**
     * Executes automated schedule delay detection engine for a project.
     * Evaluates task planned vs actual dates, progress slippage, successor dependencies, and constraint blockages.
     * Generates new delay alerts and updates severity / delay days of existing active alerts.
     */
    ScanDelayAlertsResultResponse scanAndGenerateAlerts(Long projectId, ScanDelayAlertsRequest request, String performedBy);

    /**
     * Retrieves filtered schedule delay alerts for a project.
     */
    List<ScheduleDelayAlertResponse> getAlerts(
            Long projectId,
            DelayAlertStatus status,
            OverdueSeverity severity,
            DelayAlertType alertType,
            OverdueAgingBucket agingBucket,
            String discipline,
            String contractor
    );

    /**
     * Generates executive KPI summary of schedule delay alerts for the project.
     */
    ScheduleDelayAlertSummaryResponse getAlertSummary(Long projectId, LocalDate asOfDate);

    /**
     * Retrieves single delay alert details by ID.
     */
    ScheduleDelayAlertResponse getAlertById(Long alertId);

    /**
     * Acknowledges a delay alert by site supervisor or project manager.
     * Moves status from ACTIVE to ACKNOWLEDGED.
     */
    ScheduleDelayAlertResponse acknowledgeAlert(Long alertId, AcknowledgeAlertRequest request, String performedBy);

    /**
     * Records formal recovery catch-up mitigation plan for a delayed activity.
     * Updates alert status to IN_MITIGATION and syncs with Lookahead commitment / audit trail.
     */
    ScheduleDelayAlertResponse mitigateAlert(Long alertId, MitigateAlertRequest request, String performedBy);

    /**
     * Marks delay alert as RESOLVED once delay is cleared, work caught up, or task completed.
     */
    ScheduleDelayAlertResponse resolveAlert(Long alertId, ResolveAlertRequest request, String performedBy);

    /**
     * Dismisses a delay alert with justification (e.g. false alarm, scope descoped, or weather exception).
     */
    ScheduleDelayAlertResponse dismissAlert(Long alertId, DismissAlertRequest request, String performedBy);

    /**
     * Permanently deletes a delay alert record (Admin / PM authority).
     */
    void deleteAlert(Long alertId, String performedBy);

    /**
     * Identifies activities approaching their scheduled dates that have high risk of slipping (Early Warning preview).
     */
    List<EarlyWarningDelayRiskResponse> getEarlyWarnings(Long projectId, LocalDate asOfDate, Integer horizonDays);
}

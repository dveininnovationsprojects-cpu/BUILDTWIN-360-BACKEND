package com.example.BuildTwin._0.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDelayAlertSummaryResponse {

    private Long projectId;
    private String projectCode;
    private String projectName;
    private LocalDate asOfDate;

    // KPI Counters
    private Integer totalAlerts;
    private Integer activeAlerts;
    private Integer acknowledgedAlerts;
    private Integer inMitigationAlerts;
    private Integer resolvedAlerts;
    private Integer dismissedAlerts;
    private Integer unacknowledgedAlerts;

    // Severity Breakdown
    private Integer criticalAlerts;
    private Integer highAlerts;
    private Integer mediumAlerts;
    private Integer lowAlerts;

    // Averages & Impacts
    private Double averageDelayDays;
    private Long maxDelayDays;
    private Integer totalImpactedSuccessors;

    // Detailed Maps
    private Map<String, Integer> severityBreakdown;
    private Map<String, Integer> statusBreakdown;
    private Map<String, Integer> alertTypeBreakdown;
    private Map<String, Integer> agingBreakdown;
    private Map<String, Integer> disciplineBreakdown;
    private Map<String, Integer> contractorBreakdown;

    // Critical highlights & recommendations
    private List<ScheduleDelayAlertResponse> topCriticalAlerts;
    private List<String> plannerRecommendations;
}

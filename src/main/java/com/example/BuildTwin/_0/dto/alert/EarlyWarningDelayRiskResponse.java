package com.example.BuildTwin._0.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarlyWarningDelayRiskResponse {

    private Long activityId;
    private String activityCode;
    private String activityName;
    private String discipline;
    private String assignedContractor;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private Long daysUntilPlannedStart;
    private Long daysUntilPlannedEnd;
    private String currentStatus;
    private Double currentProgressPercentage;

    private Integer pendingPredecessorCount;
    private List<String> pendingPredecessors;
    private List<String> unclearedConstraints;

    private String riskLevel; // CRITICAL_RISK, HIGH_RISK, MODERATE_RISK, LOW_RISK
    private String riskReason;
    private String recommendedPreventiveAction;
}

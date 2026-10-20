package com.example.BuildTwin._0.dto.dpr;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed physical quantity tracking metrics and burn rate for a WBS Activity")
public class ActivityQuantityTrackingResponse {

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "WBS Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "WBS Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Discipline category", example = "CIVIL")
    private String discipline;

    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @Schema(description = "Total planned baseline quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Cumulative quantity completed prior to this DPR", example = "200.0")
    private Double previousQuantity;

    @Schema(description = "Physical quantity completed on this report / date", example = "45.5")
    private Double quantityToday;

    @Schema(description = "Total cumulative quantity completed to date", example = "245.5")
    private Double cumulativeQuantity;

    @Schema(description = "Remaining balance quantity to be completed", example = "254.5")
    private Double balanceQuantity;

    @Schema(description = "Cumulative progress percentage achieved", example = "49.1")
    private Double progressPercentage;

    @Schema(description = "Flag indicating if completed quantity exceeds planned baseline", example = "false")
    private Boolean isOverExecuted;

    @Schema(description = "Excess quantity executed beyond baseline budget if any", example = "0.0")
    private Double overExecutionQuantity;

    @Schema(description = "Current activity execution status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Performance velocity status: AHEAD, ON_TRACK, BEHIND, OVER_EXECUTED, COMPLETED", example = "ON_TRACK")
    private String executionStatus;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-31")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Actual finish date if completed", example = "null")
    private LocalDate actualEndDate;

    @Schema(description = "Calendar days remaining until planned end date", example = "19")
    private Long daysRemaining;

    @Schema(description = "Required daily production rate to finish on time (balanceQuantity / daysRemaining)", example = "13.39")
    private Double requiredDailyRunRate;

    @Schema(description = "Actual average daily production rate achieved to date", example = "12.28")
    private Double actualDailyAverageRate;

    @Schema(description = "Assigned contractor name", example = "Apex Concrete Works")
    private String assignedContractor;

    @Schema(description = "Location reference or grid marking", example = "Tower A - Level 3")
    private String locationReference;

    @Schema(description = "Chronological list of recent daily progress records for this activity")
    @Builder.Default
    private List<DprActivityItemResponse> progressHistory = new ArrayList<>();
}

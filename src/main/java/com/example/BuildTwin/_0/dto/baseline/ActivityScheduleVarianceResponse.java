package com.example.BuildTwin._0.dto.baseline;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comparison of current activity progress against baseline plan with schedule variance (SV)")
public class ActivityScheduleVarianceResponse {

    @Schema(description = "Activity ID", example = "1")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Site Excavation & Earthwork")
    private String activityName;

    @Schema(description = "Trade Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Current Execution Status", example = "IN_PROGRESS")
    private String currentStatus;

    @Schema(description = "Baseline Planned Start Date", example = "2026-04-01")
    private LocalDate baselineStartDate;

    @Schema(description = "Baseline Planned End Date", example = "2026-04-10")
    private LocalDate baselineEndDate;

    @Schema(description = "Baseline Planned Duration (Days)", example = "9")
    private Integer baselineDurationDays;

    @Schema(description = "Current Planned or Actual Start Date", example = "2026-04-03")
    private LocalDate currentStartDate;

    @Schema(description = "Current Planned or Actual End Date", example = "2026-04-15")
    private LocalDate currentEndDate;

    @Schema(description = "Current Planned Duration (Days)", example = "12")
    private Integer currentDurationDays;

    @Schema(description = "Schedule Start Variance in days (Positive = Delay, Negative = Ahead)", example = "2")
    private Long startVarianceDays;

    @Schema(description = "Schedule Finish Variance in days (Positive = Delay/Slippage, Negative = Ahead)", example = "5")
    private Long finishVarianceDays;

    @Schema(description = "Current Progress Percentage", example = "45.0")
    private Double currentProgressPercentage;

    @Schema(description = "Variance Health Status: ON_TRACK, SLIGHT_DELAY, CRITICAL_DELAY, AHEAD", example = "CRITICAL_DELAY")
    private String healthStatus;
}

package com.example.BuildTwin._0.dto.progress;

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
@Schema(description = "Deep Progress History Analytics, S-Curve Time-Series Progression, Velocity & Run Rates")
public class ProgressAnalyticsResponse {

    @Schema(description = "Activity ID", example = "15")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Unit of Measure (UOM)", example = "CUM")
    private String uom;

    @Schema(description = "Total planned baseline quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Total cumulative quantity completed to date", example = "220.0")
    private Double completedQuantity;

    @Schema(description = "Remaining balance quantity to complete", example = "280.0")
    private Double balanceQuantity;

    @Schema(description = "Current progress percentage achieved", example = "44.0")
    private Double progressPercentage;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-31")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date if completed", example = "null")
    private LocalDate actualEndDate;

    @Schema(description = "Calendar days remaining until planned end date", example = "33")
    private Long daysRemaining;

    @Schema(description = "Required daily production rate to finish on schedule (balance / daysRemaining)", example = "8.48")
    private Double requiredDailyRunRate;

    @Schema(description = "Actual average daily production rate achieved on active days", example = "8.80")
    private Double actualDailyAverageRate;

    @Schema(description = "Performance velocity status: AHEAD, ON_TRACK, BEHIND, OVER_EXECUTED, COMPLETED", example = "ON_TRACK")
    private String trendStatus;

    @Schema(description = "Chronological day-by-day S-curve progression data points")
    @Builder.Default
    private List<ProgressTimelinePointDto> timeline = new ArrayList<>();
}

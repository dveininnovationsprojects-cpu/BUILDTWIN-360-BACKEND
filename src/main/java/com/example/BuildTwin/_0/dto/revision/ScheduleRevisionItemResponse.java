package com.example.BuildTwin._0.dto.revision;

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
@Schema(description = "Response representation of an activity change item inside a schedule revision")
public class ScheduleRevisionItemResponse {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "Schedule Revision ID", example = "1")
    private Long scheduleRevisionId;

    @Schema(description = "Activity ID", example = "1")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Site Excavation & Earthwork")
    private String activityName;

    @Schema(description = "Original planned start date", example = "2026-04-01")
    private LocalDate originalPlannedStartDate;

    @Schema(description = "Original planned end date", example = "2026-04-10")
    private LocalDate originalPlannedEndDate;

    @Schema(description = "Revised planned start date", example = "2026-04-05")
    private LocalDate revisedPlannedStartDate;

    @Schema(description = "Revised planned end date", example = "2026-04-20")
    private LocalDate revisedPlannedEndDate;

    @Schema(description = "Original duration in days", example = "9")
    private Integer originalDurationDays;

    @Schema(description = "Revised duration in days", example = "15")
    private Integer revisedDurationDays;

    @Schema(description = "Variance in days (Positive = Delay, Negative = Earlier)", example = "10")
    private Long varianceDays;

    @Schema(description = "Reason for delay or date change", example = "Delayed due to unseasonal rain")
    private String delayReason;

    @Schema(description = "Mitigation action", example = "Deploy second excavator")
    private String mitigationAction;
}

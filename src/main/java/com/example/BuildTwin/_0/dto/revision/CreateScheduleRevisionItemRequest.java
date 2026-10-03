package com.example.BuildTwin._0.dto.revision;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add or revise an activity within a schedule revision package")
public class CreateScheduleRevisionItemRequest {

    @NotNull(message = "Activity ID is required")
    @Schema(description = "ID of the WBS activity to revise", example = "1")
    private Long activityId;

    @Schema(description = "Revised planned start date", example = "2026-04-05")
    private LocalDate revisedPlannedStartDate;

    @NotNull(message = "Revised planned completion date is required")
    @Schema(description = "Revised planned completion date", example = "2026-04-20")
    private LocalDate revisedPlannedEndDate;

    @Schema(description = "Reason for delay or date change", example = "Delayed due to unseasonal rain and material supply hold")
    private String delayReason;

    @Schema(description = "Mitigation or recovery action", example = "Deploy additional excavator and second shift team")
    private String mitigationAction;
}

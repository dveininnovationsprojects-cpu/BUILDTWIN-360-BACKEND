package com.example.BuildTwin._0.dto.wbs;

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
@Schema(description = "Request payload for updating or rescheduling activity dates")
public class ScheduleActivityRequest {

    @Schema(description = "Revised or planned start date", example = "2026-09-20")
    private LocalDate plannedStartDate;

    @Schema(description = "Revised or planned completion date", example = "2026-10-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date if work has commenced", example = "2026-09-22")
    private LocalDate actualStartDate;

    @Schema(description = "Actual completion date if work has completed", example = "2026-10-28")
    private LocalDate actualEndDate;

    @Schema(description = "Reason or remarks for schedule revision", example = "Delayed due to unseasonal rain and material transit hold")
    private String reasonForRevision;
}

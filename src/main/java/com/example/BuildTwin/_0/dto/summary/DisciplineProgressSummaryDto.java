package com.example.BuildTwin._0.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Discipline-level progress summary breakdown")
public class DisciplineProgressSummaryDto {

    @Schema(description = "Discipline name (e.g., CIVIL, STRUCTURAL, MEP, FINISHING)", example = "CIVIL")
    private String discipline;

    @Schema(description = "Total activities in this discipline", example = "12")
    private Long totalActivities;

    @Schema(description = "Completed activities in this discipline", example = "8")
    private Long completedActivities;

    @Schema(description = "In-progress activities in this discipline", example = "3")
    private Long inProgressActivities;

    @Schema(description = "Actual progress percentage", example = "75.0")
    private Double actualProgressPercentage;

    @Schema(description = "Planned progress percentage to date", example = "80.0")
    private Double plannedProgressPercentage;

    @Schema(description = "Progress variance (Actual % - Planned %)", example = "-5.0")
    private Double progressVariance;
}

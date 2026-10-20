package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Discipline-level rollup of physical quantities and progress")
public class QuantityDisciplineRollup {

    @Schema(description = "Discipline name (e.g. CIVIL, MEP, STRUCTURAL)", example = "CIVIL")
    private String discipline;

    @Schema(description = "Count of activities in this discipline", example = "12")
    private Integer activityCount;

    @Schema(description = "Count of completed activities", example = "4")
    private Integer completedCount;

    @Schema(description = "Count of activities in progress", example = "6")
    private Integer inProgressCount;

    @Schema(description = "Total planned baseline quantity", example = "12500.0")
    private Double totalPlannedQuantity;

    @Schema(description = "Total completed quantity to date", example = "7800.0")
    private Double totalCompletedQuantity;

    @Schema(description = "Total remaining balance quantity", example = "4700.0")
    private Double totalBalanceQuantity;

    @Schema(description = "Weighted average progress percentage", example = "62.4")
    private Double overallProgressPercentage;
}

package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weighting methodology used to calculate cumulative progress rollups across WBS activities and work packages")
public enum WeightingModel {
    @Schema(description = "Uses the custom weightage factor configured on each WBS activity")
    WEIGHTAGE_FACTOR,

    @Schema(description = "Uses financial budget allocations or unit rate values to compute cost-weighted cumulative progress (Earned Value BCWP/BCWS)")
    BUDGET_WEIGHTED,

    @Schema(description = "Uses scheduled duration in days (planned duration) as the weighting factor")
    DURATION_WEIGHTED,

    @Schema(description = "Assigns equal weight to every activity regardless of size or duration")
    EQUAL_WEIGHTED,

    @Schema(description = "Weights activities by normalized physical baseline quantity")
    QUANTITY_WEIGHTED
}

package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional configuration parameters for triggering an on-demand cumulative progress recalculation")
public class RecalculateProgressRequest {

    @Schema(description = "Optional weighting model to use for rollups (defaults to WEIGHTAGE_FACTOR)", example = "WEIGHTAGE_FACTOR")
    private WeightingModel weightingModel;

    @Schema(description = "Whether to auto-sync actual start and completion dates based on logged progress", example = "true")
    @Builder.Default
    private Boolean syncActualDates = true;

    @Schema(description = "Whether to auto-mark status as COMPLETED when cumulative progress reaches 100%", example = "true")
    @Builder.Default
    private Boolean autoCompleteStatus = true;

    @Schema(description = "Reason or remarks for triggering recalculation", example = "Recalculate after contract baseline revision v2 approval")
    private String reason;
}

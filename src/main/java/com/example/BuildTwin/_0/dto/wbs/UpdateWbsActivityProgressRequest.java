package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating progress on a WBS Activity")
public class UpdateWbsActivityProgressRequest {

    @PositiveOrZero(message = "Completed quantity must be zero or positive")
    @Schema(description = "Cumulative or logged completed quantity so far", example = "150.0")
    private Double completedQuantity;

    @DecimalMin(value = "0.0", message = "Progress percentage cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Progress percentage cannot exceed 100")
    @Schema(description = "Optional explicit progress percentage (0 - 100). If omitted, automatically calculated from completedQuantity / plannedQuantity.", example = "33.33")
    private Double progressPercentage;

    @Schema(description = "Actual start date if work commenced", example = "2026-09-18")
    private LocalDate actualStartDate;

    @Schema(description = "Actual completion date if work finished", example = "2026-10-10")
    private LocalDate actualEndDate;

    @Schema(description = "Optional notes or progress remarks", example = "4 columns shuttered and rebar tied up to beam bottom")
    private String remarks;
}

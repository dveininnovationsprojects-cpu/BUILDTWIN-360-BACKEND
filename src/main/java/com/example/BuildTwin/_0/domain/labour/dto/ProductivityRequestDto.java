package com.example.BuildTwin._0.domain.labour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for on-demand custom productivity calculation")
public class ProductivityRequestDto {

    @Schema(description = "WBS Activity ID (optional)", example = "101")
    private Long activityId;

    @Schema(description = "Configured activity unit", example = "sqm")
    private String unit;

    @NotNull(message = "Completed quantity is required")
    @Positive(message = "Completed quantity must be greater than zero")
    @Schema(description = "Completed quantity", example = "500.00")
    private BigDecimal completedQuantity;

    @NotNull(message = "Labour hours is required")
    @Positive(message = "Labour hours must be greater than zero")
    @Schema(description = "Total labour hours spent", example = "250.00")
    private BigDecimal labourHours;
}

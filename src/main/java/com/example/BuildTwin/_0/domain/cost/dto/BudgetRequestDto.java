package com.example.BuildTwin._0.domain.cost.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

import com.example.BuildTwin._0.domain.cost.enums.CostHead;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequestDto {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long activityId;

    @NotBlank(message = "Cost code is required")
    private String costCode;

    private CostHead costHead;

    @NotNull(message = "Baseline amount is required")
    @Min(value = 0, message = "Baseline amount must be non-negative")
    private BigDecimal baselineAmount;

    private BigDecimal revisedAmount;
}

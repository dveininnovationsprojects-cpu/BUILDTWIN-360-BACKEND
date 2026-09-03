package com.example.BuildTwin._0.domain.cost.dto;

import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostTransactionRequestDto {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long activityId;

    @NotBlank(message = "Cost code is required")
    private String costCode;

    private CostHead costHead;

    @NotNull(message = "Source type is required")
    private CostSourceType sourceType;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private String referenceNumber;
}

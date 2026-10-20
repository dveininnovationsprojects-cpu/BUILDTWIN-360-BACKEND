package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Project material actual cost summary derived from Stock Ledger CONSUMPTION transactions")
public class MaterialActualCostSummaryDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Total material actual consumption cost", example = "450000.00")
    private BigDecimal totalMaterialCost;

    @Schema(description = "Total quantity of materials consumed", example = "1250.00")
    private BigDecimal totalQuantityConsumed;

    @Schema(description = "Number of consumption transaction entries", example = "14")
    private int entriesCount;

    @Schema(description = "Itemized consumption lines")
    private List<MaterialCostItemDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaterialCostItemDto {
        private Long materialId;
        private String materialCode;
        private String materialName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalCost;
        private String referenceId;
        private String timestamp;
    }
}

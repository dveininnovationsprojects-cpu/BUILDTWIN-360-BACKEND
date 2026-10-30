package com.example.BuildTwin._0.domain.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Material & Inventory Summary Report")
public class InventoryReportDto {

    private Long projectId;
    private String projectName;
    private LocalDateTime generatedAt;

    private Integer totalMaterialsCataloged;
    private BigDecimal totalInventoryValuation;
    private Integer lowStockItemsCount;
    private BigDecimal totalConsumedQuantity;
    private BigDecimal totalConsumptionCost;

    private List<LowStockItemDto> criticalLowStockItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItemDto {
        private Long materialId;
        private String materialCode;
        private String materialName;
        private String unit;
        private BigDecimal currentStock;
        private BigDecimal reorderLevel;
        private BigDecimal standardRate;
        private BigDecimal stockDeficit;
    }
}

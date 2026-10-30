package com.example.BuildTwin._0.domain.materials.dto;

import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Real-time computed material stock balance, valuation, and safety threshold alerts")
public class MaterialStockBalanceDto {

    @Schema(description = "Material primary ID", example = "1")
    private Long materialId;

    @Schema(description = "Unique SKU material code", example = "MAT-CEM-001")
    private String materialCode;

    @Schema(description = "Material item name", example = "Coromandel OPC Cement 50kg")
    private String name;

    @Schema(description = "Material category", example = "CEMENT")
    private String category;

    @Schema(description = "Measurement unit", example = "BAGS")
    private MaterialUnit unit;

    @Schema(description = "Dynamically computed current stock on hand", example = "150.00")
    private BigDecimal currentStock;

    @Schema(description = "Safety reorder threshold level", example = "50.00")
    private BigDecimal reorderLevel;

    @Schema(description = "Standard rate per unit", example = "380.00")
    private BigDecimal standardRate;

    @Schema(description = "Total inventory valuation (currentStock * standardRate)", example = "57000.00")
    private BigDecimal totalStockValue;

    @Schema(description = "Indicates whether stock is at or below reorder threshold", example = "false")
    private boolean lowStock;

    @Schema(description = "Timestamp of the most recent stock ledger movement", example = "2026-09-28T14:30:00")
    private LocalDateTime lastTransactionTimestamp;
}

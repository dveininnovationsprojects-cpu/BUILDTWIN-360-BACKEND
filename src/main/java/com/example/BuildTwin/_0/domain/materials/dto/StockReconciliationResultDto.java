package com.example.BuildTwin._0.domain.materials.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO representing the outcome of a stock reconciliation audit")
public class StockReconciliationResultDto {

    @Schema(description = "Target material ID", example = "5")
    private Long materialId;

    @Schema(description = "Material SKU code", example = "MAT-BRK-01")
    private String materialCode;

    @Schema(description = "Material name", example = "Country Red Clay Bricks")
    private String materialName;

    @Schema(description = "Material unit", example = "BRICKS")
    private String unit;

    @Schema(description = "System stock before audit (Ledger Stock)", example = "10010.00")
    @JsonProperty("systemStock")
    private BigDecimal systemStock;

    @Schema(description = "Actual physical stock recorded", example = "10000.00")
    private BigDecimal physicalStock;

    @Schema(description = "Variance quantity (Physical - System)", example = "-10.00")
    private BigDecimal variance;

    @Schema(description = "Variance status: MATCHED, SURPLUS, SHORTAGE", example = "SHORTAGE")
    private String status;

    @Schema(description = "Generated stock ledger adjustment transaction ID", example = "94")
    private Long adjustmentTransactionId;

    @Schema(description = "Auditor name", example = "admin")
    private String auditedBy;

    @Schema(description = "Audit remarks", example = "Physical audit found 10 broken bricks")
    private String remarks;

    @Schema(description = "Reconciliation timestamp")
    private LocalDateTime reconciledAt;

    @JsonProperty("auditorName")
    public String getAuditorName() {
        return this.auditedBy;
    }

    @JsonProperty("ledgerStock")
    public BigDecimal getLedgerStock() {
        return this.systemStock;
    }

    @JsonProperty("physicalStockCount")
    public BigDecimal getPhysicalStockCount() {
        return this.physicalStock;
    }
}

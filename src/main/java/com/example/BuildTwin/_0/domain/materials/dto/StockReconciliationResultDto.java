package com.example.BuildTwin._0.domain.materials.dto;

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

    @Schema(description = "Material SKU code", example = "MAT-CEM-001")
    private String materialCode;

    @Schema(description = "Material name", example = "Portland Cement (PPC)")
    private String materialName;

    @Schema(description = "System stock before audit", example = "200.00")
    private BigDecimal systemStock;

    @Schema(description = "Actual physical stock recorded", example = "180.00")
    private BigDecimal physicalStock;

    @Schema(description = "Variance quantity (Physical - System)", example = "-20.00")
    private BigDecimal variance;

    @Schema(description = "Generated stock ledger adjustment transaction ID", example = "94")
    private Long adjustmentTransactionId;

    @Schema(description = "Auditor name", example = "Auditor_Ramesh")
    private String auditedBy;

    @Schema(description = "Reconciliation timestamp")
    private LocalDateTime reconciledAt;
}

package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Individual Purchase Order commitment item details")
public class PoCommitmentItemDto {

    @Schema(description = "Purchase Order ID", example = "50")
    private Long poId;

    @Schema(description = "PO reference number", example = "PO-2026-0089")
    private String poNumber;

    @Schema(description = "Supplier ID", example = "5")
    private Long supplierId;

    @Schema(description = "Material ID", example = "12")
    private Long materialId;

    @Schema(description = "Ordered quantity", example = "500.00")
    private BigDecimal orderQty;

    @Schema(description = "Unit rate in currency", example = "450.00")
    private BigDecimal unitRate;

    @Schema(description = "Total commercial commitment amount", example = "225000.00")
    private BigDecimal amount;

    @Schema(description = "PO lifecycle status (e.g. ISSUED, PARTIALLY_FULFILLED)", example = "ISSUED")
    private String status;

    @Schema(description = "Expected site delivery date", example = "2026-10-25")
    private LocalDate expectedDeliveryDate;
}

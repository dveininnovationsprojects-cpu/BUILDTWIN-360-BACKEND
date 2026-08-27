package com.example.BuildTwin._0.domain.materials.dto;

import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
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
@Schema(description = "DTO for posting an inventory transaction to the immutable stock ledger")
public class StockTransactionDto {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Target project ID", example = "101")
    private Long projectId;

    @Schema(description = "Site ID", example = "201")
    private Long siteId;

    @NotNull(message = "Material ID is required")
    @Schema(description = "Target material ID", example = "5")
    private Long materialId;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Type of inventory movement: RECEIPT, ISSUE, CONSUMPTION, RETURN, ADJUSTMENT", example = "RECEIPT")
    private StockTransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity of material", example = "500.00")
    private BigDecimal quantity;

    @Schema(description = "Unit rate / price at time of transaction", example = "380.00")
    private BigDecimal unitPrice;

    @Schema(description = "Reference document ID (PO, GRN, Issue Note)", example = "GRN-2026-0812")
    private String referenceId;

    @Schema(description = "Transaction remarks or site notes", example = "Received 500 bags cement against PO-1029")
    private String remarks;
}

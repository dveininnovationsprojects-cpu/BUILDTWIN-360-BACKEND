package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representation of a DPR material receipt and consumption entry")
public class DprMaterialItemResponse {

    @Schema(description = "Material entry ID", example = "1")
    private Long id;

    @Schema(description = "Material name", example = "OPC 53 Grade Cement")
    private String materialName;

    @Schema(description = "Unit of measure", example = "BAGS")
    private String unitOfMeasure;

    @Schema(description = "Quantity received today", example = "200.0")
    private Double quantityReceived;

    @Schema(description = "Quantity consumed today", example = "150.0")
    private Double quantityConsumed;

    @Schema(description = "Supplier / Vendor name", example = "UltraTech Cement Ltd")
    private String supplierName;

    @Schema(description = "Challan / Gate Pass / Invoice number", example = "DC-2026-8891")
    private String challanOrInvoiceNumber;

    @Schema(description = "Remarks", example = "Batch samples passed slump test")
    private String remarks;
}

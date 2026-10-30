package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to track bulk material receipt and consumption in DPR")
public class DprMaterialItemRequest {

    @NotBlank(message = "Material name is required")
    @Schema(description = "Material description", example = "OPC 53 Grade Cement")
    private String materialName;

    @NotBlank(message = "Unit of measure is required")
    @Schema(description = "Unit of measurement (BAGS, MT, CUM, etc.)", example = "BAGS")
    private String unitOfMeasure;

    @Schema(description = "Quantity received at site on this shift", example = "200.0")
    @Builder.Default
    private Double quantityReceived = 0.0;

    @Schema(description = "Quantity consumed during activities on this shift", example = "150.0")
    @Builder.Default
    private Double quantityConsumed = 0.0;

    @Schema(description = "Material vendor or batch supplier", example = "UltraTech Cement Ltd")
    private String supplierName;

    @Schema(description = "Delivery challan, gate pass, or invoice number", example = "DC-2026-8891")
    private String challanOrInvoiceNumber;

    @Schema(description = "Storage location or testing notes", example = "Stored in Site Silo 1; Cube test samples cast")
    private String remarks;
}

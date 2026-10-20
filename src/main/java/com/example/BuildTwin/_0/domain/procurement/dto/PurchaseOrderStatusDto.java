package com.example.BuildTwin._0.domain.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for updating Purchase Order status across lifecycle (DRAFT, ISSUED, PARTIALLY_FULFILLED, FULFILLED, CANCELLED)")
public class PurchaseOrderStatusDto {

    @NotBlank(message = "PO Status is required")
    @Schema(description = "Target status: DRAFT, ISSUED, PARTIALLY_FULFILLED, FULFILLED, CANCELLED, APPROVED", example = "ISSUED")
    private String status;

    @Schema(description = "Reason or remarks for status change", example = "PO issued to vendor for material dispatch")
    private String remarks;

    @Schema(description = "User or role performing the status update", example = "Procurement_Lead")
    private String updatedBy;
}

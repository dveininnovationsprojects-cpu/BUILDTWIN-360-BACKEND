package com.example.BuildTwin._0.domain.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO representing purchase order fulfillment and delivery metrics against GRNs")
public class PoFulfillmentDto {

    @Schema(description = "Purchase Order ID", example = "50")
    private Long poId;

    @Schema(description = "Purchase Order Code/Number", example = "PO-2026-0042")
    private String poNumber;

    @Schema(description = "Supplier ID", example = "3")
    private Long supplierId;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Material ID", example = "1")
    private Long materialId;

    @Schema(description = "Ordered Quantity", example = "500.00")
    private BigDecimal orderQty;

    @Schema(description = "Total Received Quantity from all delivery notes", example = "500.00")
    private BigDecimal totalReceivedQty;

    @Schema(description = "Total Accepted Quantity verified into store", example = "480.00")
    private BigDecimal totalAcceptedQty;

    @Schema(description = "Total Rejected / Damaged Quantity in transit", example = "20.00")
    private BigDecimal totalRejectedQty;

    @Schema(description = "Remaining Quantity yet to be delivered", example = "20.00")
    private BigDecimal remainingQty;

    @Schema(description = "Current PO Fulfillment Status", example = "PARTIALLY_DELIVERED")
    private String status;

    @Schema(description = "Scheduled Delivery Date", example = "2026-10-20")
    private LocalDate expectedDeliveryDate;

    @Schema(description = "Fulfillment Percentage based on accepted goods", example = "96.00%")
    private String fulfillmentPercentage;
}

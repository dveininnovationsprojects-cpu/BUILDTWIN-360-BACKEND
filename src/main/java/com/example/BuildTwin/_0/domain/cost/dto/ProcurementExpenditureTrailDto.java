package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Procurement total expenditure trail showing committed POs vs incurred GRN accepted receipts")
public class ProcurementExpenditureTrailDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Committed Cost: Total commercial value of Purchase Orders in ISSUED status", example = "500000.00")
    private BigDecimal committedCost;

    @Schema(description = "Incurred Cost: Total bill value of GRN accepted deliveries on ground", example = "350000.00")
    private BigDecimal incurredCost;

    @Schema(description = "Total fulfilled / completed PO billing", example = "420000.00")
    private BigDecimal totalFulfilledBilling;

    @Schema(description = "Outstanding PO liability pending delivery & inspection", example = "150000.00")
    private BigDecimal outstandingLiability;

    @Schema(description = "Total purchase orders count", example = "8")
    private int totalPoCount;

    @Schema(description = "Total goods receipt notes count", example = "12")
    private int totalGrnCount;

    @Schema(description = "Detailed list of open PO commitments")
    private List<PoCommitmentItemDto> openCommitments;
}

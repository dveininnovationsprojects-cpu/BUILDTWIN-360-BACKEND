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
@Schema(description = "Procurement Commitments summary showing committed PO balances against actual expenditures")
public class ProcurementCommitmentDto {

    @Schema(description = "Target project ID", example = "101")
    private Long projectId;

    @Schema(description = "Total committed amount on open Purchase Orders (ISSUED, PARTIALLY_FULFILLED)", example = "450000.00")
    private BigDecimal totalCommittedAmount;

    @Schema(description = "Total fulfilled / invoiced PO amount", example = "850000.00")
    private BigDecimal totalFulfilledAmount;

    @Schema(description = "Total cancelled PO amount", example = "50000.00")
    private BigDecimal totalCancelledAmount;

    @Schema(description = "Active open commitment balance pending fulfillment", example = "450000.00")
    private BigDecimal activeCommitmentBalance;

    @Schema(description = "Count of active open purchase orders", example = "4")
    private int openOrdersCount;

    @Schema(description = "Detailed list of open PO commitments")
    private List<PoCommitmentItemDto> openOrders;
}

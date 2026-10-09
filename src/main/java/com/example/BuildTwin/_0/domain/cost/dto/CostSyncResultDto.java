package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Result of syncing stock ledger consumption and labour allocations into the central cost transactions ledger")
public class CostSyncResultDto {

    @Schema(description = "Project ID synced", example = "101")
    private Long projectId;

    @Schema(description = "Count of material consumption transactions synced", example = "12")
    private int materialTransactionsSynced;

    @Schema(description = "Count of labour allocation entries synced", example = "8")
    private int labourTransactionsSynced;

    @Schema(description = "Total cost value synchronized into cost ledger", example = "580000.00")
    private BigDecimal totalSyncedAmount;

    @Schema(description = "Status message", example = "Stock & Labour consumptions synchronized into cost ledger successfully")
    private String message;
}

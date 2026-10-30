package com.example.BuildTwin._0.domain.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Procurement & Labour Unified Report")
public class ProcurementLabourReportDto {

    private Long projectId;
    private String projectName;
    private LocalDateTime generatedAt;

    // Procurement Metrics
    private Integer totalPurchaseOrders;
    private Integer openCommitmentOrdersCount;
    private BigDecimal totalCommittedAmount;
    private BigDecimal totalIncurredGrnCost;
    private BigDecimal outstandingLiability;
    private Integer totalGrnReceiptsLogged;

    // Labour Metrics
    private Integer activeContractorsCount;
    private Integer totalHeadcountDeployed;
    private BigDecimal totalStandardHours;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalLabourWageCost;
    private Integer totalDailyAttendanceLogs;
}

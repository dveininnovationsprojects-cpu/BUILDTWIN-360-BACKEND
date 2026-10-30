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
@Schema(description = "Weekly Project Executive Comprehensive Report")
public class WeeklyProjectReportDto {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Report Week Number", example = "10")
    private Integer weekNumber;

    @Schema(description = "Report generation timestamp")
    private LocalDateTime generatedAt;

    // Progress
    private BigDecimal plannedProgressPercentage;
    private BigDecimal actualProgressPercentage;
    private String scheduleStatus; // ON_SCHEDULE, BEHIND_SCHEDULE

    // Labour
    private Integer totalHeadcount;
    private BigDecimal totalStandardHours;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalLabourCost;

    // Material & Inventory
    private Integer materialsTrackedCount;
    private Integer lowStockAlertsCount;
    private BigDecimal totalMaterialConsumptionCost;

    // Procurement
    private Integer totalPurchaseOrdersCount;
    private BigDecimal committedProcurementCost;
    private BigDecimal incurredGrnCost;
    private BigDecimal outstandingLiability;

    // Cost & EVM
    private BigDecimal baselineBudget;
    private BigDecimal actualExpense;
    private BigDecimal costVariance;
    private BigDecimal costPerformanceIndex;
    private BigDecimal schedulePerformanceIndex;
    private BigDecimal estimateAtCompletion;
    private String costHealthStatus; // ON_BUDGET, OVER_BUDGET, CRITICAL

    // Quality & Issues
    private Long openSnagsCount;
    private Long activeNcrsCount;
    private Long criticalBlockersCount;
    private Integer totalSiteDelayDays;

    // Notifications
    private Long unreadAlertsCount;
}

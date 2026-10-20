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
@Schema(description = "Report of WBS activities and Cost Codes exceeding or nearing allocated budget limits")
public class BudgetOverrunReportDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Total number of over-budget or critical cost heads", example = "3")
    private int overrunCount;

    @Schema(description = "Total cost overrun amount", example = "150000.00")
    private BigDecimal totalOverrunAmount;

    @Schema(description = "List of cost heads exceeding or nearing 90% budget threshold")
    private List<BudgetOverrunItemDto> overruns;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BudgetOverrunItemDto {
        private String costCode;
        private Long activityId;
        private String costHead;
        private String description;
        private BigDecimal allocatedBudget;
        private BigDecimal actualExpense;
        private BigDecimal overrunAmount;
        private String utilizationPercentage;
        private String alertSeverity; // CRITICAL_OVERRUN (>100%), WARNING_90_PERCENT (>=90%), ON_TRACK (<90%)
        private String alertMessage;
    }
}

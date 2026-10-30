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
@Schema(description = "Project labour actual expense summary calculated from Labour Daily and Allocation man-hours × trade wage rates")
public class LabourActualCostSummaryDto {

    @Schema(description = "Project ID", example = "101")
    private Long projectId;

    @Schema(description = "Total labour actual cost incurred", example = "320000.00")
    private BigDecimal totalLabourCost;

    @Schema(description = "Total man-hours worked (standard + overtime)", example = "1600.00")
    private BigDecimal totalManHours;

    @Schema(description = "Total recorded headcount across days", example = "140")
    private int totalHeadcount;

    @Schema(description = "Itemized trade and contractor labour costs")
    private List<LabourCostItemDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LabourCostItemDto {
        private String tradeCategory;
        private String contractorName;
        private int headcount;
        private BigDecimal standardHours;
        private BigDecimal overtimeHours;
        private BigDecimal totalHours;
        private BigDecimal hourlyRate;
        private BigDecimal totalCost;
        private String recordDate;
    }
}

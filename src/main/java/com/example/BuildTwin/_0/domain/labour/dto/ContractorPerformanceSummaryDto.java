package com.example.BuildTwin._0.domain.labour.dto;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comprehensive Contractor Performance Summary covering Progress, Quality, Delay, and Productivity")
public class ContractorPerformanceSummaryDto {

    @Schema(description = "Contractor ID", example = "1")
    private Long contractorId;

    @Schema(description = "Contractor Code", example = "CON-101")
    private String contractorCode;

    @Schema(description = "Contractor Name", example = "Ramesh Kumar")
    private String contractorName;

    @Schema(description = "Company Name", example = "Ramesh Electricals")
    private String companyName;

    @Schema(description = "Trade Specialization", example = "ELECTRICIAN")
    private TradeCategory tradeSpecialization;

    @Schema(description = "Contractor Status", example = "ACTIVE")
    private String status;

    // 1. Progress Metrics
    @Schema(description = "Total activities assigned to contractor", example = "10")
    private Integer totalActivitiesAssigned;

    @Schema(description = "Completed activities count", example = "8")
    private Integer completedActivitiesCount;

    @Schema(description = "Overall progress completion percentage", example = "80.00")
    private BigDecimal progressPercentage;

    // 2. Quality Metrics
    @Schema(description = "Total quality inspections performed", example = "15")
    private Integer qualityInspectionsCount;

    @Schema(description = "Passed quality inspections count", example = "14")
    private Integer qualityInspectionsPassed;

    @Schema(description = "Quality pass rate percentage", example = "93.33")
    private BigDecimal qualityPassRatePercentage;

    // 3. Delay Metrics
    @Schema(description = "Number of activities running delayed", example = "1")
    private Integer delayedActivitiesCount;

    @Schema(description = "Total cumulative delay in days", example = "3")
    private Integer totalDelayDays;

    @Schema(description = "Schedule adherence rating", example = "ON_SCHEDULE")
    private String scheduleStatus;

    // 4. Labour Deployment & Productivity Metrics
    @Schema(description = "Total labour headcount deployed to date", example = "150")
    private Integer totalHeadcountDeployed;

    @Schema(description = "Total standard labour hours logged", example = "1200.00")
    private BigDecimal totalStandardHours;

    @Schema(description = "Total overtime labour hours logged", example = "150.00")
    private BigDecimal totalOvertimeHours;

    @Schema(description = "Total labour hours spent", example = "1350.00")
    private BigDecimal totalLabourHoursSpent;

    @Schema(description = "Average output efficiency rating", example = "HIGH_PERFORMER")
    private String overallPerformanceRating;
}

package com.example.BuildTwin._0.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Executive Project Progress Summary Dashboard Response")
public class ProjectProgressSummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Project Status", example = "ACTIVE")
    private String projectStatus;

    @Schema(description = "Project Type", example = "RESIDENTIAL")
    private String projectType;

    @Schema(description = "Client Name", example = "Ashok Leyland Properties")
    private String clientName;

    @Schema(description = "Project Site Location", example = "OMR Road, Padur, Chennai")
    private String location;

    @Schema(description = "Planned Start Date", example = "2026-01-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned End Date", example = "2026-12-31")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual Start Date", example = "2026-01-10")
    private LocalDate actualStartDate;

    @Schema(description = "Actual End Date", example = "null")
    private LocalDate actualEndDate;

    @Schema(description = "Estimated Budget", example = "25000000.00")
    private BigDecimal estimatedBudget;

    @Schema(description = "Currency", example = "INR")
    private String currency;

    // --- Overall Progress Metrics ---
    @Schema(description = "Overall actual progress percentage (weighted)", example = "68.5")
    private Double overallProgressPercentage;

    @Schema(description = "Planned progress percentage to date based on schedule", example = "72.0")
    private Double plannedProgressPercentage;

    @Schema(description = "Progress Variance % (Actual % - Planned %)", example = "-3.5")
    private Double progressVariance;

    @Schema(description = "Schedule Health Status (AHEAD, ON_TRACK, AT_RISK, DELAYED, COMPLETED)", example = "ON_TRACK")
    private String scheduleHealth;

    @Schema(description = "Earned Value (EV = Budget * Actual % / 100)", example = "17125000.00")
    private BigDecimal earnedValue;

    @Schema(description = "Planned Value (PV = Budget * Planned % / 100)", example = "18000000.00")
    private BigDecimal plannedValue;

    @Schema(description = "Schedule Performance Index (SPI = EV / PV)", example = "0.95")
    private Double schedulePerformanceIndex;

    // --- Activity Status Breakdown ---
    @Schema(description = "Total activities across project", example = "45")
    private Long totalActivities;

    @Schema(description = "Completed activities count", example = "22")
    private Long completedActivities;

    @Schema(description = "In-progress activities count", example = "16")
    private Long inProgressActivities;

    @Schema(description = "Planned / Not-started activities count", example = "7")
    private Long plannedActivities;

    @Schema(description = "Delayed activities count", example = "3")
    private Long delayedActivities;

    @Schema(description = "On-hold activities count", example = "0")
    private Long onHoldActivities;

    @Schema(description = "Activity completion rate percentage ((completed / total) * 100)", example = "48.89")
    private Double completionRate;

    // --- Physical Quantities Summary ---
    @Schema(description = "Total planned physical quantity", example = "15000.0")
    private Double totalPlannedQuantity;

    @Schema(description = "Total completed physical quantity", example = "10275.0")
    private Double totalCompletedQuantity;

    @Schema(description = "Total remaining physical quantity", example = "4725.0")
    private Double totalRemainingQuantity;

    @Schema(description = "Quantity completed progress percentage", example = "68.5")
    private Double quantityProgressPercentage;

    // --- Progress Logs & Hindrance Metrics ---
    @Schema(description = "Date of the most recent progress log", example = "2026-10-27")
    private LocalDate lastProgressUpdateDate;

    @Schema(description = "Total logged progress entries count", example = "142")
    private Long totalProgressEntriesCount;

    @Schema(description = "Active hindrances / site blockers count", example = "2")
    private Long activeBlockersCount;

    // --- Work Package & Discipline Rollups ---
    @Schema(description = "Breakdown by Work Package")
    private List<WorkPackageProgressSummaryDto> workPackages;

    @Schema(description = "Breakdown by Engineering Discipline")
    private List<DisciplineProgressSummaryDto> disciplines;
}

package com.example.BuildTwin._0.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Complete Progress History and Production Audit Trail for a single WBS Activity")
public class ActivityProgressHistoryResponse {

    @Schema(description = "Activity ID", example = "15")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Trade Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Work Package ID", example = "3")
    private Long workPackageId;

    @Schema(description = "Work Package Name", example = "Civil Works Package")
    private String workPackageName;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Unit of Measure (UOM)", example = "CUM")
    private String uom;

    @Schema(description = "Total planned baseline quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Total cumulative quantity completed to date", example = "220.0")
    private Double completedQuantity;

    @Schema(description = "Remaining balance quantity to complete", example = "280.0")
    private Double balanceQuantity;

    @Schema(description = "Current cumulative progress percentage achieved", example = "44.0")
    private Double currentProgressPercentage;

    @Schema(description = "Current activity status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Assigned contractor name", example = "Apex Concrete Works")
    private String assignedContractor;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-31")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date", example = "null")
    private LocalDate actualEndDate;

    // Statistical & Performance Metrics
    @Schema(description = "Total number of progress log entries recorded", example = "8")
    private Integer totalLogsCount;

    @Schema(description = "Sum of physical quantity logged across all journal entries", example = "220.0")
    private Double totalQuantityLogged;

    @Schema(description = "Average daily physical output recorded on active days", example = "27.5")
    private Double averageDailyQuantity;

    @Schema(description = "Maximum output logged in a single day", example = "50.0")
    private Double maxDailyQuantity;

    @Schema(description = "First date progress was recorded", example = "2026-09-03")
    private LocalDate firstLogDate;

    @Schema(description = "Latest date progress was recorded", example = "2026-09-28")
    private LocalDate latestLogDate;

    @Schema(description = "Total number of log entries where a blocker was active", example = "1")
    private Integer totalBlockerDays;

    @Schema(description = "Required daily production rate to finish on schedule", example = "8.48")
    private Double requiredDailyRunRate;

    @Schema(description = "Actual average daily production rate achieved", example = "8.80")
    private Double actualDailyAverageRate;

    @Schema(description = "Chronological list of site progress history entries")
    @Builder.Default
    private List<ProgressHistoryItemResponse> history = new ArrayList<>();
}

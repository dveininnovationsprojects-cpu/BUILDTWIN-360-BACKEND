package com.example.BuildTwin._0.dto.lookahead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Executive summary and analytics dashboard of overdue construction activities across the project")
public class OverdueActivitySummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Evaluation reference date", example = "2026-10-05")
    private LocalDate asOfDate;

    @Schema(description = "Total activities defined in the project", example = "45")
    private Integer totalProjectActivities;

    @Schema(description = "Total activities identified as overdue or delayed", example = "6")
    private Integer totalOverdueActivities;

    @Schema(description = "Percentage of project activities that are overdue", example = "13.3")
    private Double overdueRatePercentage;

    @Schema(description = "Count of activities with Overdue Finish (missed end date)", example = "4")
    private Integer overdueFinishCount;

    @Schema(description = "Count of activities with Overdue Start (missed kickoff date)", example = "2")
    private Integer overdueStartCount;

    @Schema(description = "Count of in-progress activities lagging severely behind planned rate", example = "0")
    private Integer behindScheduleCount;

    @Schema(description = "Average delay across all overdue activities in calendar days", example = "11.5")
    private Double averageDelayDays;

    @Schema(description = "Maximum delay recorded on any single activity in days", example = "28")
    private Long maxDelayDays;

    @Schema(description = "Total downstream successor activities impacted / blocked by these overdue tasks", example = "9")
    private Integer totalImpactedSuccessors;

    @Schema(description = "Distribution by delay aging brackets (DAYS_1_TO_7, DAYS_8_TO_14, DAYS_15_TO_30, OVER_30_DAYS)")
    @Builder.Default
    private Map<String, Integer> agingBreakdown = new HashMap<>();

    @Schema(description = "Distribution by severity classification (CRITICAL, HIGH, MEDIUM, LOW)")
    @Builder.Default
    private Map<String, Integer> severityBreakdown = new HashMap<>();

    @Schema(description = "Distribution of overdue tasks by engineering discipline (CIVIL, MEP, FINISHING, etc.)")
    @Builder.Default
    private Map<String, Integer> disciplineBreakdown = new HashMap<>();

    @Schema(description = "Distribution of overdue tasks by assigned contractor")
    @Builder.Default
    private Map<String, Integer> contractorBreakdown = new HashMap<>();

    @Schema(description = "Sum of remaining physical quantities pending across overdue tasks by Unit of Measure (UOM)")
    @Builder.Default
    private Map<String, Double> remainingWorkloadByUom = new HashMap<>();

    @Schema(description = "Top critical overdue bottlenecks requiring urgent field intervention")
    @Builder.Default
    private List<OverdueActivityItemResponse> topCriticalBottlenecks = new ArrayList<>();

    @Schema(description = "High-level strategic recovery recommendations for project management")
    @Builder.Default
    private List<String> plannerRecommendations = new ArrayList<>();
}

package com.example.BuildTwin._0.dto.baseline;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Overall Project Schedule Variance and Health Report against Active Baseline")
public class ProjectScheduleVarianceReport {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Baseline ID used for comparison", example = "1")
    private Long baselineId;

    @Schema(description = "Baseline Name", example = "Baseline 1.0 (Master Contract Schedule)")
    private String baselineName;

    @Schema(description = "Total activities evaluated", example = "25")
    private Integer totalActivities;

    @Schema(description = "Activities on track", example = "18")
    private Integer onTrackActivitiesCount;

    @Schema(description = "Activities with slight delay (1 to 5 days)", example = "4")
    private Integer slightDelayActivitiesCount;

    @Schema(description = "Activities with critical delay (> 5 days)", example = "2")
    private Integer criticalDelayActivitiesCount;

    @Schema(description = "Activities ahead of schedule", example = "1")
    private Integer aheadActivitiesCount;

    @Schema(description = "Average Schedule Slippage in days across delayed tasks", example = "3.5")
    private Double averageSlippageDays;

    @Schema(description = "Overall project health status: HEALTHY, MODERATE_RISK, HIGH_RISK", example = "MODERATE_RISK")
    private String overallProjectHealth;

    @Builder.Default
    @Schema(description = "Detailed activity-level variance breakdown")
    private List<ActivityScheduleVarianceResponse> activityVariances = new ArrayList<>();
}

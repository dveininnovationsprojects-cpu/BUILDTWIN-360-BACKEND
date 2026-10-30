package com.example.BuildTwin._0.domain.issues.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issue & Blocker KPI summary metrics for project")
public class IssueSummaryDto {

    @Schema(description = "Project ID", example = "100")
    private Long projectId;

    @Schema(description = "Total issues recorded", example = "18")
    private long totalIssues;

    @Schema(description = "Active / unresolved blocker count", example = "4")
    private long activeBlockers;

    @Schema(description = "Total general open issues", example = "7")
    private long openIssues;

    @Schema(description = "In progress issues count", example = "3")
    private long inProgressCount;

    @Schema(description = "Resolved / unblocked issues count", example = "8")
    private long resolvedCount;

    @Schema(description = "Critical severity issues count", example = "2")
    private long criticalCount;

    @Schema(description = "High severity issues count", example = "4")
    private long highCount;

    @Schema(description = "Cumulative delay days across active blockers", example = "14")
    private int totalDaysDelayed;

    @Schema(description = "Overdue active blockers count", example = "2")
    private long overdueCount;
}

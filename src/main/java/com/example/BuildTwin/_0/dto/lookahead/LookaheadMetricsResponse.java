package com.example.BuildTwin._0.dto.lookahead;

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
@Schema(description = "Aggregated KPIs and Readiness Metrics for the Look-Ahead window")
public class LookaheadMetricsResponse {

    @Schema(description = "Total distinct activities scheduled/active in the look-ahead window", example = "5")
    private Integer totalActivitiesInWindow;

    @Schema(description = "Activities starting within this window", example = "2")
    private Integer startingCount;

    @Schema(description = "Activities completing within this window", example = "1")
    private Integer finishingCount;

    @Schema(description = "Activities ongoing across the entire window", example = "2")
    private Integer ongoingCount;

    @Schema(description = "Activities overdue / delayed from earlier cycles still incomplete", example = "0")
    private Integer overdueCount;

    @Schema(description = "Activities committed into weekly plan", example = "3")
    private Integer committedCount;

    @Schema(description = "Activities not yet committed", example = "2")
    private Integer uncommittedCount;

    @Schema(description = "Activities flagged with constraints or blockers", example = "1")
    private Integer blockedCount;

    @Schema(description = "Activities completely ready for field execution (no blockers)", example = "4")
    private Integer readyCount;

    @Schema(description = "Overall Schedule Readiness Score Percentage (Ready / Total * 100)", example = "80.0")
    private Double readinessScorePercentage;

    @Schema(description = "Total work-days sum across active activities", example = "21")
    private Integer totalPlannedWorkloadDays;

    @Schema(description = "Distinct engineering disciplines in this window", example = "[\"CIVIL\", \"MEP\"]")
    @Builder.Default
    private List<String> disciplinesRepresented = new ArrayList<>();

    @Schema(description = "Count of work packages involved", example = "2")
    private Integer workPackagesRepresented;
}

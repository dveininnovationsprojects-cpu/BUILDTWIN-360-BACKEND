package com.example.BuildTwin._0.dto.lookahead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary of hindrances, missing constraints, and dependencies blocking look-ahead activities")
public class LookaheadConstraintSummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Lookahead Window Start Date", example = "2026-10-01")
    private LocalDate windowStartDate;

    @Schema(description = "Lookahead Window End Date", example = "2026-10-07")
    private LocalDate windowEndDate;

    @Schema(description = "Total activities with at least one constraint/blocker", example = "2")
    private Integer totalBlockedActivities;

    @Schema(description = "Activities blocked by missing materials", example = "1")
    private Integer materialConstraintCount;

    @Schema(description = "Activities blocked by missing GFC drawings", example = "0")
    private Integer drawingConstraintCount;

    @Schema(description = "Activities blocked by missing equipment/machinery", example = "1")
    private Integer equipmentConstraintCount;

    @Schema(description = "Activities blocked by manpower/gang deficit", example = "0")
    private Integer manpowerConstraintCount;

    @Schema(description = "Activities blocked by unfinished predecessor dependencies", example = "1")
    private Integer predecessorConstraintCount;

    @Schema(description = "Activities blocked by site access issues", example = "0")
    private Integer accessConstraintCount;

    @Schema(description = "List of specific constrained activity details")
    @Builder.Default
    private List<LookaheadActivityItemResponse> constrainedActivities = new ArrayList<>();
}

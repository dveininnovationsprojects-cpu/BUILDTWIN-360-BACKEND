package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Executive Blocker & Hindrance Analytics Summary for a Project")
public class DprBlockerSummaryResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-SKY-001")
    private String projectCode;

    @Schema(description = "Project Name", example = "Skyline Heights Luxury Towers")
    private String projectName;

    @Schema(description = "Total blockers logged to date across all DPRs", example = "15")
    private Integer totalBlockersCount;

    @Schema(description = "Active open blockers requiring immediate action", example = "4")
    private Integer openBlockersCount;

    @Schema(description = "Blockers currently in progress of resolution", example = "2")
    private Integer inProgressBlockersCount;

    @Schema(description = "Resolved blockers", example = "9")
    private Integer resolvedBlockersCount;

    @Schema(description = "Critical severity blockers", example = "2")
    private Integer criticalBlockersCount;

    @Schema(description = "High severity blockers", example = "5")
    private Integer highBlockersCount;

    @Schema(description = "Total cumulative site delay impact hours", example = "28.5")
    private Double totalImpactHours;

    @Schema(description = "Breakdown of blockers by category")
    @Builder.Default
    private Map<String, Long> categoryBreakdown = new HashMap<>();

    @Schema(description = "Breakdown of blockers by severity")
    @Builder.Default
    private Map<String, Long> severityBreakdown = new HashMap<>();

    @Schema(description = "Breakdown of blockers by status")
    @Builder.Default
    private Map<String, Long> statusBreakdown = new HashMap<>();

    @Schema(description = "Most recent active blockers")
    @Builder.Default
    private List<DprBlockerItemResponse> recentBlockers = new ArrayList<>();
}

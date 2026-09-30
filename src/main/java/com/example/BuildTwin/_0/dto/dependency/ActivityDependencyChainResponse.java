package com.example.BuildTwin._0.dto.dependency;

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
@Schema(description = "Comprehensive dependency chain and readiness analysis for an activity")
public class ActivityDependencyChainResponse {

    @Schema(description = "Activity ID", example = "10")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-002")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Column Concreting & Curing")
    private String activityName;

    @Schema(description = "Activity Status", example = "PLANNED")
    private String status;

    @Schema(description = "Planned Start Date", example = "2026-09-12")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned End Date", example = "2026-09-20")
    private LocalDate plannedEndDate;

    @Schema(description = "Progress Percentage", example = "0.0")
    private Double progressPercentage;

    @Schema(description = "Overall readiness flag: True if all Finish-to-Start predecessors are 100% completed", example = "true")
    private Boolean isReadyToStart;

    @Schema(description = "Total number of predecessor activities that must occur prior", example = "2")
    private Integer totalPredecessors;

    @Schema(description = "Total number of successor activities waiting on this activity", example = "3")
    private Integer totalSuccessors;

    @Schema(description = "Number of incomplete predecessor activities blocking this activity", example = "0")
    private Integer blockingPredecessorsCount;

    @Builder.Default
    @Schema(description = "List of incomplete predecessor activities currently blocking execution")
    private List<String> blockingPredecessorNames = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of predecessor dependencies (incoming edges)")
    private List<ActivityDependencyResponse> predecessors = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of successor dependencies (outgoing edges)")
    private List<ActivityDependencyResponse> successors = new ArrayList<>();
}

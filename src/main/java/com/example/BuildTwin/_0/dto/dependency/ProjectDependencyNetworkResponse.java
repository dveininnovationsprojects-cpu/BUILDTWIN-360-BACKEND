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
@Schema(description = "Full project activity precedence network graph payload for Gantt charts and CPM scheduling")
public class ProjectDependencyNetworkResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-BLR-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Total number of activity nodes in project", example = "45")
    private Integer totalActivities;

    @Schema(description = "Total number of precedence dependency links", example = "58")
    private Integer totalDependencies;

    @Schema(description = "Count of detected schedule conflicts in network", example = "0")
    private Integer scheduleConflictsCount;

    @Builder.Default
    @Schema(description = "Activity graph nodes")
    private List<NetworkNode> nodes = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Directed precedence dependency edges")
    private List<NetworkEdge> edges = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Activity node in CPM precedence graph")
    public static class NetworkNode {
        private Long id;
        private String code;
        private String name;
        private String discipline;
        private String status;
        private Long workPackageId;
        private String workPackageName;
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private Double progressPercentage;
        private Integer level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Directed precedence edge in CPM precedence graph")
    public static class NetworkEdge {
        private Long id;
        private Long fromPredecessorId;
        private Long toSuccessorId;
        private String dependencyType;
        private Integer lagDays;
        private String remarks;
        private Boolean hasScheduleConflict;
    }
}

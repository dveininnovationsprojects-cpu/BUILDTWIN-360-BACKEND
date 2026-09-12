package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hierarchical WBS Breakdown Tree (Project -> Work Packages -> Activities)")
public class WbsTreeResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PADUR-AG-01")
    private String projectCode;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Overall progress percentage", example = "35.5")
    private Double overallProgressPercentage;

    @Builder.Default
    @Schema(description = "Work package nodes with child activities")
    private List<WorkPackageNode> workPackages = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkPackageNode {
        private Long id;
        private String code;
        private String name;
        private String discipline;
        private String status;
        private BigDecimal budgetAmount;
        private String assignedContractor;
        private Double progressPercentage;
        private Integer totalActivities;
        private Integer completedActivities;

        @Builder.Default
        private List<ActivityNode> activities = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityNode {
        private Long id;
        private String code;
        private String name;
        private String uom;
        private Double plannedQuantity;
        private Double completedQuantity;
        private Double progressPercentage;
        private String status;
        private String location; // e.g. "Tower A > First Typical Floor > 3BHK Luxury Flat 101"
        private Integer sequenceOrder;
    }
}

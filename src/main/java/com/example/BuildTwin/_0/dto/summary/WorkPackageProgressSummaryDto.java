package com.example.BuildTwin._0.dto.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Work package progress summary breakdown")
public class WorkPackageProgressSummaryDto {

    @Schema(description = "Work package ID", example = "1")
    private Long workPackageId;

    @Schema(description = "Work package code", example = "WP-CIV-01")
    private String code;

    @Schema(description = "Work package name", example = "Substructure & RCC Piling Works")
    private String name;

    @Schema(description = "Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Work package lifecycle status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Overall progress percentage", example = "65.5")
    private Double progressPercentage;

    @Schema(description = "Planned progress percentage to date", example = "70.0")
    private Double plannedProgressPercentage;

    @Schema(description = "Progress variance percentage", example = "-4.5")
    private Double progressVariance;

    @Schema(description = "Total activities under this work package", example = "15")
    private Long totalActivities;

    @Schema(description = "Completed activities count", example = "8")
    private Long completedActivities;

    @Schema(description = "In-progress activities count", example = "5")
    private Long inProgressActivities;

    @Schema(description = "Planned / Not-started activities count", example = "2")
    private Long plannedActivities;

    @Schema(description = "Allocated budget for work package", example = "1200000.00")
    private BigDecimal budgetAmount;
}

package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.DprStatus;
import com.example.BuildTwin._0.model.WorkShift;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary of quantity tracking for all activities recorded in a Daily Progress Report")
public class DprQuantityTrackingSummaryResponse {

    @Schema(description = "DPR Report ID", example = "1")
    private Long dprId;

    @Schema(description = "Unique Report Tracking Code", example = "DPR-PRJ1-20261012-DAY")
    private String reportCode;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Heights Luxury Towers")
    private String projectName;

    @Schema(description = "Report Date", example = "2026-10-12")
    private LocalDate reportDate;

    @Schema(description = "Shift of work", example = "DAY_SHIFT")
    private WorkShift shift;

    @Schema(description = "Report lifecycle status", example = "APPROVED")
    private DprStatus status;

    @Schema(description = "Total number of activities recorded in this DPR", example = "5")
    private Integer totalActivitiesLogged;

    @Schema(description = "Sum of planned quantities across logged activities", example = "2500.0")
    private Double totalPlannedVolume;

    @Schema(description = "Sum of quantities completed on this report shift", example = "175.5")
    private Double totalQuantityToday;

    @Schema(description = "Sum of cumulative quantities completed to date", example = "1320.0")
    private Double totalCumulativeQuantity;

    @Schema(description = "Sum of remaining balance quantities", example = "1180.0")
    private Double totalBalanceQuantity;

    @Schema(description = "Overall progress percentage of activities in this DPR", example = "52.8")
    private Double overallProgressPercentage;

    @Schema(description = "Count of activities with quantity over-execution", example = "0")
    private Integer overExecutedActivitiesCount;

    @Schema(description = "List of detailed quantity tracking line items for this DPR")
    @Builder.Default
    private List<ActivityQuantityTrackingResponse> activityQuantities = new ArrayList<>();
}

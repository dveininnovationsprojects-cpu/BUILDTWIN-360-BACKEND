package com.example.BuildTwin._0.dto.revision;

import com.example.BuildTwin._0.model.ScheduleRevisionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed Impact Analysis for a Schedule Revision")
public class ScheduleRevisionImpactAnalysisResponse {

    @Schema(description = "Revision ID", example = "1")
    private Long revisionId;

    @Schema(description = "Revision Code", example = "REV-01")
    private String revisionCode;

    @Schema(description = "Revision Name", example = "Baseline Revision 1")
    private String revisionName;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Revision Status", example = "DRAFT")
    private ScheduleRevisionStatus status;

    @Schema(description = "Total activities with schedule changes", example = "4")
    private Integer totalActivitiesChanged;

    @Schema(description = "Max delay days observed among affected activities", example = "15")
    private Long maxActivityDelayDays;

    @Schema(description = "Net time extension requested for the project in calendar days", example = "25")
    private Integer netProjectTimeExtensionDays;

    @Schema(description = "Original project planned completion date", example = "2026-12-31")
    private LocalDate originalProjectEndDate;

    @Schema(description = "Proposed new project completion date", example = "2027-02-15")
    private LocalDate proposedProjectEndDate;

    @Schema(description = "Financial cost impact", example = "450000.00")
    private BigDecimal totalCostImpact;

    @Builder.Default
    @Schema(description = "Detailed breakdown of each activity's variance and delay reason")
    private List<ScheduleRevisionItemResponse> items = new ArrayList<>();
}

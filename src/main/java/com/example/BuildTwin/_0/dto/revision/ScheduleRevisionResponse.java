package com.example.BuildTwin._0.dto.revision;

import com.example.BuildTwin._0.model.ScheduleRevisionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a project schedule revision")
public class ScheduleRevisionResponse {

    @Schema(description = "Revision ID", example = "1")
    private Long id;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Revision Code", example = "REV-01")
    private String revisionCode;

    @Schema(description = "Revision Name", example = "Baseline Revision 1 (Monsoon Delay)")
    private String revisionName;

    @Schema(description = "Revision proposal date", example = "2026-09-30")
    private LocalDate revisionDate;

    @Schema(description = "Narrative / reason for revision", example = "Groundwater seepage and monsoon delays")
    private String reasonForRevision;

    @Schema(description = "Current revision workflow status", example = "DRAFT")
    private ScheduleRevisionStatus status;

    @Schema(description = "Target project completion date", example = "2027-02-15")
    private LocalDate targetCompletionDate;

    @Schema(description = "Time extension requested in days", example = "25")
    private Integer timeExtensionDays;

    @Schema(description = "Cost impact", example = "450000.00")
    private BigDecimal costImpact;

    @Schema(description = "Total activities affected/included in this revision", example = "4")
    private Integer totalAffectedActivities;

    @Schema(description = "Max schedule variance days among activities", example = "15")
    private Long maxVarianceDays;

    @Schema(description = "User who prepared this revision", example = "admin")
    private String preparedBy;

    @Schema(description = "User who requested this revision", example = "admin")
    private String requestedBy;

    @Schema(description = "User who reviewed this revision")
    private String reviewedBy;

    @Schema(description = "User who approved this revision")
    private String approvedBy;

    @Schema(description = "Timestamp when approved")
    private LocalDateTime approvedAt;

    @Schema(description = "Rejection narrative if rejected")
    private String rejectionReason;

    @Schema(description = "List of revised activity items")
    @Builder.Default
    private List<ScheduleRevisionItemResponse> items = new ArrayList<>();

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

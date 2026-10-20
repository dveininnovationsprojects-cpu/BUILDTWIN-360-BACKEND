package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representation of a DPR WBS activity progress line item with blocker & remarks support")
public class DprActivityItemResponse {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "WBS Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "WBS Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Discipline category", example = "CIVIL")
    private String discipline;

    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @Schema(description = "Total planned baseline quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Previous cumulative quantity completed prior to this DPR", example = "200.0")
    private Double previousQuantity;

    @Schema(description = "Quantity completed today", example = "45.5")
    private Double quantityToday;

    @Schema(description = "Cumulative quantity completed up to this date", example = "245.5")
    private Double cumulativeQuantity;

    @Schema(description = "Remaining balance quantity to be executed", example = "254.5")
    private Double balanceQuantity;

    @Schema(description = "Incremental percentage progress achieved today", example = "9.1")
    private Double progressPercentageToday;

    @Schema(description = "Cumulative completion percentage", example = "49.1")
    private Double cumulativeProgressPercentage;

    @Schema(description = "Flag indicating if cumulative quantity exceeds planned baseline", example = "false")
    private Boolean isOverExecuted;

    @Schema(description = "Location reference / Grid line", example = "Tower A - Level 3 - Pour B")
    private String locationReference;

    @Schema(description = "Remarks or operational notes", example = "Concrete pour completed smoothly")
    private String remarks;

    // Blocker Details
    @Schema(description = "Flag indicating if this activity experienced an execution blocker", example = "true")
    private Boolean isBlocker;

    @Schema(description = "Category of blocker", example = "MATERIAL_SHORTAGE")
    private BlockerCategory blockerCategory;

    @Schema(description = "Detailed reason for the blocker", example = "Delay in batching plant transit mixer arrival")
    private String blockerReason;

    @Schema(description = "Severity level of blocker", example = "HIGH")
    private BlockerSeverity blockerSeverity;

    @Schema(description = "Blocker lifecycle status", example = "OPEN")
    private BlockerStatus blockerStatus;

    @Schema(description = "Impact in delay hours", example = "2.5")
    private Double impactHours;

    @Schema(description = "Resolution notes", example = "Backup batching plant engaged")
    private String blockerResolutionNotes;

    @Schema(description = "Name/login of user who resolved the blocker", example = "PM Senthil")
    private String blockerResolvedBy;

    @Schema(description = "Timestamp when blocker was resolved", example = "2026-10-12T16:30:00")
    private LocalDateTime blockerResolvedAt;
}

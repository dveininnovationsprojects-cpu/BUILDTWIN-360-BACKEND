package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import com.example.BuildTwin._0.model.DprStatus;
import com.example.BuildTwin._0.model.WorkShift;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dedicated Blocker Register item representation across DPR execution reports")
public class DprBlockerItemResponse {

    @Schema(description = "DPR Activity Item ID", example = "42")
    private Long itemId;

    @Schema(description = "DPR ID", example = "10")
    private Long dprId;

    @Schema(description = "DPR Report Code", example = "DPR-PRJ1-20261012-DAY")
    private String reportCode;

    @Schema(description = "Report Date", example = "2026-10-12")
    private LocalDate reportDate;

    @Schema(description = "Shift of work", example = "DAY_SHIFT")
    private WorkShift shift;

    @Schema(description = "DPR Status", example = "APPROVED")
    private DprStatus dprStatus;

    @Schema(description = "Site Engineer who drafted the DPR", example = "Engr. Rajesh Kumar")
    private String siteEngineer;

    @Schema(description = "WBS Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "WBS Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "WBS Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Discipline category", example = "CIVIL")
    private String discipline;

    @Schema(description = "Location / grid line reference", example = "Tower A - Level 3 - Pour B")
    private String locationReference;

    @Schema(description = "Whether blocker is active", example = "true")
    private Boolean isBlocker;

    @Schema(description = "Blocker Category", example = "MATERIAL_SHORTAGE")
    private BlockerCategory blockerCategory;

    @Schema(description = "Blocker Severity", example = "HIGH")
    private BlockerSeverity blockerSeverity;

    @Schema(description = "Blocker Lifecycle Status", example = "OPEN")
    private BlockerStatus blockerStatus;

    @Schema(description = "Blocker Root Cause Description", example = "RMC concrete supply halted due to local road works")
    private String blockerReason;

    @Schema(description = "Impact delay in hours", example = "3.0")
    private Double impactHours;

    @Schema(description = "Field remarks / operational notes", example = "Waiting for police clearance on alternate delivery corridor")
    private String remarks;

    @Schema(description = "Resolution Notes", example = "Rerouted via Western bypass")
    private String blockerResolutionNotes;

    @Schema(description = "User who resolved the blocker", example = "PM Senthil")
    private String blockerResolvedBy;

    @Schema(description = "Resolution timestamp", example = "2026-10-12T16:30:00")
    private LocalDateTime blockerResolvedAt;

    @Schema(description = "Item creation timestamp", example = "2026-10-12T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Item last update timestamp", example = "2026-10-12T16:30:00")
    private LocalDateTime updatedAt;
}

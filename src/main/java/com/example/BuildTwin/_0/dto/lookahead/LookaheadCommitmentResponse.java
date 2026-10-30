package com.example.BuildTwin._0.dto.lookahead;

import com.example.BuildTwin._0.model.CommitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a Weekly Lookahead Commitment")
public class LookaheadCommitmentResponse {

    @Schema(description = "Commitment ID", example = "1")
    private Long id;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "WBS Activity ID", example = "1")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Floor 1 Column Starter & Rebar Tying")
    private String activityName;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Week Start Date", example = "2026-10-01")
    private LocalDate weekStartDate;

    @Schema(description = "Week End Date", example = "2026-10-07")
    private LocalDate weekEndDate;

    @Schema(description = "Committed Target Quantity", example = "15.0")
    private Double targetQuantity;

    @Schema(description = "Unit of Measure", example = "MT")
    private String uom;

    @Schema(description = "Actual Quantity Achieved", example = "15.0")
    private Double actualQuantityAchieved;

    @Schema(description = "Percent Commitment Achieved (PPC contribution %)", example = "100.0")
    private Double achievementPercentage;

    @Schema(description = "Commitment Status", example = "COMMITTED")
    private CommitmentStatus status;

    @Schema(description = "Crew Size", example = "8")
    private Integer crewSize;

    @Schema(description = "Assigned Contractor", example = "L&T Construction (Civil Div)")
    private String assignedContractor;

    // Constraint Checklist
    @Schema(description = "Materials Ready", example = "true")
    private Boolean materialsReady;

    @Schema(description = "Drawings Ready", example = "true")
    private Boolean drawingsReady;

    @Schema(description = "Equipment Ready", example = "true")
    private Boolean equipmentReady;

    @Schema(description = "Manpower Ready", example = "true")
    private Boolean manpowerReady;

    @Schema(description = "Safety Permit Approved", example = "true")
    private Boolean safetyPermitApproved;

    @Schema(description = "Access Clear", example = "true")
    private Boolean accessClear;

    @Schema(description = "Is Activity 100% Constraint Free", example = "true")
    private Boolean isConstraintFree;

    @Schema(description = "Blocker / Hindrance reason", example = "None")
    private String blockerReason;

    @Schema(description = "Variance reason if target missed")
    private String varianceReason;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "User who committed this task", example = "admin")
    private String committedBy;

    @Schema(description = "Timestamp when created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when updated")
    private LocalDateTime updatedAt;
}

package com.example.BuildTwin._0.dto.lookahead;

import com.example.BuildTwin._0.model.CommitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to commit an activity into the 7-day lookahead weekly work plan")
public class CreateLookaheadCommitmentRequest {

    @NotNull(message = "Activity ID is required")
    @Schema(description = "WBS Activity ID to commit for weekly execution", example = "1")
    private Long activityId;

    @Schema(description = "Lookahead window start date (defaults to current week start / today if omitted)", example = "2026-10-01")
    private LocalDate weekStartDate;

    @Schema(description = "Lookahead window end date (defaults to start date + 6 days if omitted)", example = "2026-10-07")
    private LocalDate weekEndDate;

    @PositiveOrZero(message = "Target quantity cannot be negative")
    @Schema(description = "Committed physical work quantity to execute during this window", example = "15.0")
    private Double targetQuantity;

    @Schema(description = "Unit of measure for target quantity (optional, defaults to activity UOM)", example = "MT")
    private String uom;

    @Schema(description = "Allocated crew size / labor count", example = "8")
    private Integer crewSize;

    @Schema(description = "Contractor name assigned to this weekly commitment", example = "L&T Construction (Civil Div)")
    private String assignedContractor;

    @Schema(description = "Commitment status (COMMITTED, PENDING_READINESS, BLOCKED)", example = "COMMITTED")
    private CommitmentStatus status;

    // Constraint / Readiness Checklist
    @Schema(description = "Materials available and staged on site", example = "true")
    private Boolean materialsReady;

    @Schema(description = "Good-for-Construction (GFC) drawings approved and available", example = "true")
    private Boolean drawingsReady;

    @Schema(description = "Machinery and equipment ready and tested", example = "true")
    private Boolean equipmentReady;

    @Schema(description = "Required trade labor/gang mobilized", example = "true")
    private Boolean manpowerReady;

    @Schema(description = "Permit to Work (PTW) or safety clearances approved", example = "true")
    private Boolean safetyPermitApproved;

    @Schema(description = "Work area access clear of hindrances", example = "true")
    private Boolean accessClear;

    @Schema(description = "Description of blocker or constraint if not ready", example = "Pending concrete pump maintenance")
    private String blockerReason;

    @Schema(description = "Operational notes or execution guidelines", example = "Ensure high-tensile Fe550D rebar inspection before casting")
    private String notes;
}

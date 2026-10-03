package com.example.BuildTwin._0.dto.lookahead;

import com.example.BuildTwin._0.model.CommitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing lookahead commitment, constraint checklist, or weekly progress")
public class UpdateLookaheadCommitmentRequest {

    @PositiveOrZero(message = "Target quantity cannot be negative")
    @Schema(description = "Updated target quantity for this week", example = "20.0")
    private Double targetQuantity;

    @PositiveOrZero(message = "Actual quantity achieved cannot be negative")
    @Schema(description = "Actual physical quantity achieved at the end of the window", example = "18.5")
    private Double actualQuantityAchieved;

    @Schema(description = "Updated crew size / labor gang count", example = "10")
    private Integer crewSize;

    @Schema(description = "Assigned contractor name", example = "L&T Construction (Civil Div)")
    private String assignedContractor;

    @Schema(description = "Updated commitment status", example = "COMPLETED")
    private CommitmentStatus status;

    // Constraint Checklist Updates
    @Schema(description = "Materials available on site", example = "true")
    private Boolean materialsReady;

    @Schema(description = "Drawings approved and issued", example = "true")
    private Boolean drawingsReady;

    @Schema(description = "Equipment and machinery available", example = "true")
    private Boolean equipmentReady;

    @Schema(description = "Manpower mobilized", example = "true")
    private Boolean manpowerReady;

    @Schema(description = "Safety work permit approved", example = "true")
    private Boolean safetyPermitApproved;

    @Schema(description = "Site access clear", example = "true")
    private Boolean accessClear;

    @Schema(description = "Root cause / explanation if work was blocked", example = "Material delivery delayed by 2 days due to road transport strike")
    private String blockerReason;

    @Schema(description = "Variance explanation if actual quantity missed commitment", example = "Heavy rain caused 4 hours stoppage on Day 3")
    private String varianceReason;

    @Schema(description = "Execution notes", example = "Shift hours extended on Friday to recover backlog")
    private String notes;
}

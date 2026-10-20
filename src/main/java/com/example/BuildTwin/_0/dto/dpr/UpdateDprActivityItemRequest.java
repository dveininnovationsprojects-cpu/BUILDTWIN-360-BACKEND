package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import com.example.BuildTwin._0.model.BlockerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to update an existing DPR WBS activity progress line item, including blocker & remarks updates")
public class UpdateDprActivityItemRequest {

    @Positive(message = "Quantity completed today must be greater than zero")
    @Schema(description = "Updated physical quantity completed on this shift", example = "50.0")
    private Double quantityToday;

    @Schema(description = "Updated location reference or grid marking", example = "Tower A - Level 3 - Pour C")
    private String locationReference;

    @Schema(description = "Updated remarks or operational notes", example = "Re-inspected slump 120mm; vibration completed")
    private String remarks;

    @Schema(description = "Updated blocker presence flag", example = "true")
    private Boolean isBlocker;

    @Schema(description = "Updated category of blocker", example = "MATERIAL_SHORTAGE")
    private BlockerCategory blockerCategory;

    @Schema(description = "Updated blocker reason / root cause details", example = "RMC batch delivery resumed after route diversion")
    private String blockerReason;

    @Schema(description = "Updated blocker severity: LOW, MEDIUM, HIGH, CRITICAL", example = "MEDIUM")
    private BlockerSeverity blockerSeverity;

    @Schema(description = "Updated blocker lifecycle status: OPEN, IN_PROGRESS, RESOLVED, WAIVED", example = "RESOLVED")
    private BlockerStatus blockerStatus;

    @Schema(description = "Updated impact delay hours", example = "1.5")
    private Double impactHours;

    @Schema(description = "Resolution notes explaining how the blocker was resolved or mitigated", example = "Backup batching plant engaged; pour finished at 16:30")
    private String blockerResolutionNotes;
}

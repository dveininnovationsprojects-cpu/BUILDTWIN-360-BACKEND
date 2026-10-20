package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.BlockerCategory;
import com.example.BuildTwin._0.model.BlockerSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to record progress against a WBS activity in DPR with blocker and remarks support")
public class DprActivityItemRequest {

    @NotNull(message = "Activity ID is required")
    @Schema(description = "ID of the WBS activity", example = "101")
    private Long activityId;

    @NotNull(message = "Quantity completed today is required")
    @Positive(message = "Quantity completed today must be greater than zero")
    @Schema(description = "Physical quantity completed on this shift", example = "45.5")
    private Double quantityToday;

    @Schema(description = "Location reference or grid marking", example = "Tower A - Level 3 - Pour B")
    private String locationReference;

    @Schema(description = "Remarks or operational notes", example = "Concrete grade M35 pump pour completed smoothly")
    private String remarks;

    @Schema(description = "Whether this activity experienced an execution blocker / hindrance today", example = "true")
    @Builder.Default
    private Boolean isBlocker = false;

    @Schema(description = "Category of blocker: MATERIAL_SHORTAGE, DESIGN_CLARIFICATION, DRAWING_REVISION, MANPOWER_SHORTAGE, EQUIPMENT_BREAKDOWN, WEATHER_DELAY, SITE_ACCESS, SAFETY_HALT, PERMIT_APPROVAL, SUBCONTRACTOR_DELAY, CLIENT_DECISION, PREDECESSOR_INCOMPLETE, OTHER", example = "MATERIAL_SHORTAGE")
    private BlockerCategory blockerCategory;

    @Schema(description = "Detailed reason and description of the blocker", example = "Delay in batching plant transit mixer arrival due to regional traffic restriction")
    private String blockerReason;

    @Schema(description = "Severity level of the blocker: LOW, MEDIUM, HIGH, CRITICAL", example = "HIGH")
    private BlockerSeverity blockerSeverity;

    @Schema(description = "Impact in terms of delayed work hours", example = "2.5")
    private Double impactHours;
}

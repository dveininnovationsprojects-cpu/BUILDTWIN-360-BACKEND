package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to record progress against a WBS activity in DPR")
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
}

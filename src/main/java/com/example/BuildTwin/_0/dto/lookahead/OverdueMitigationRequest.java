package com.example.BuildTwin._0.dto.lookahead;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to log root-cause delay reason and catch-up recovery action plan for an overdue activity")
public class OverdueMitigationRequest {

    @NotBlank(message = "Delay root cause category is required")
    @Schema(description = "Category of delay (e.g., MATERIAL_SHORTAGE, DESIGN_REVISION, LABOR_SHORTAGE, WEATHER_DELAY, SITE_ACCESS, EQUIPMENT_BREAKDOWN, CONTRACTOR_PERFORMANCE, OTHER)", example = "MATERIAL_SHORTAGE")
    private String delayReasonCategory;

    @Schema(description = "Detailed root cause explanation", example = "Supplier delay in shipping Fe550D rebar bundles due to transport strike")
    private String delayReasonDetails;

    @NotBlank(message = "Recovery action plan is required")
    @Schema(description = "Concrete catch-up recovery plan and fast-tracking strategy", example = "Local procurement of 15 MT authorized rebar + 2-hour daily overtime shift for steel fixers")
    private String recoveryPlanAction;

    @Schema(description = "Revised targeted completion date following mitigation", example = "2026-10-15")
    private LocalDate revisedExpectedCompletionDate;

    @Schema(description = "Additional manpower / crew count allocated for catch-up", example = "4")
    private Integer additionalLaborAllocated;

    @Schema(description = "Additional remarks or meeting references", example = "Approved in weekly progress review meeting with Project Director")
    private String notes;
}

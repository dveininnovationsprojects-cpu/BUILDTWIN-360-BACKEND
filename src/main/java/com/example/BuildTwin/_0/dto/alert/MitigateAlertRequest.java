package com.example.BuildTwin._0.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MitigateAlertRequest {

    @NotBlank(message = "Delay reason category is required")
    @Schema(description = "Category of root cause delay", example = "MATERIAL_SHORTAGE",
            allowableValues = {"MATERIAL_SHORTAGE", "LABOR_ABSENCE", "DRAWING_REVISION", "EQUIPMENT_BREAKDOWN", "WEATHER_DELAY", "PERMIT_PENDING", "PREDECESSOR_DELAY", "SUBCONTRACTOR_DEFAULT", "OTHER"})
    private String delayReasonCategory;

    @Schema(description = "Detailed engineering narrative explaining root cause of delay", example = "Supplier delayed dispatch of ready-mix concrete due to plant maintenance.")
    private String delayReasonDetails;

    @NotBlank(message = "Recovery plan action is required")
    @Schema(description = "Concrete catch-up recovery actions committed to mitigate the delay", example = "Deploy second batching supplier, mobilize overtime shift with 6 extra laborers to recover 4 days.")
    private String recoveryPlanAction;

    @Schema(description = "Revised target completion date aimed by recovery plan", example = "2026-10-30")
    private LocalDate revisedExpectedCompletionDate;

    @Schema(description = "Additional gang or workforce deployed to accelerate progress", example = "6")
    private Integer additionalLaborAllocated;

    @Schema(description = "Additional operational remarks", example = "Recovery schedule approved by Project Manager.")
    private String notes;
}

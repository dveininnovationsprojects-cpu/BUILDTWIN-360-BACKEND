package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to record heavy machinery and equipment usage in DPR")
public class DprEquipmentItemRequest {

    @NotBlank(message = "Equipment name is required")
    @Schema(description = "Name of the plant or equipment", example = "Tower Crane #2")
    private String equipmentName;

    @Schema(description = "Equipment asset tag or registration code", example = "EQ-TC-02")
    private String equipmentCode;

    @NotNull(message = "Quantity of equipment units is required")
    @Positive(message = "Quantity must be at least 1")
    @Schema(description = "Number of active units", example = "1")
    @Builder.Default
    private Integer quantity = 1;

    @Schema(description = "Active operating hours during shift", example = "6.5")
    @Builder.Default
    private Double operatingHours = 0.0;

    @Schema(description = "Idle hours due to standby or sequencing", example = "1.5")
    @Builder.Default
    private Double idleHours = 0.0;

    @Schema(description = "Down time / breakdown hours", example = "0.0")
    @Builder.Default
    private Double breakdownHours = 0.0;

    @Schema(description = "Diesel / electricity fuel consumed in liters or kWh", example = "45.0")
    private Double fuelConsumedLiters;

    @Schema(description = "Remarks or maintenance status", example = "Routine daily maintenance check cleared")
    private String remarks;
}

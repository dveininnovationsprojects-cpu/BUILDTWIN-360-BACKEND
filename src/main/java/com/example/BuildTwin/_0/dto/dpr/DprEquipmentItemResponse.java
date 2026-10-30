package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representation of a DPR equipment deployment entry")
public class DprEquipmentItemResponse {

    @Schema(description = "Equipment entry ID", example = "1")
    private Long id;

    @Schema(description = "Equipment description", example = "Tower Crane #2")
    private String equipmentName;

    @Schema(description = "Equipment code / Asset tag", example = "EQ-TC-02")
    private String equipmentCode;

    @Schema(description = "Quantity of active units", example = "1")
    private Integer quantity;

    @Schema(description = "Operating hours", example = "6.5")
    private Double operatingHours;

    @Schema(description = "Idle hours", example = "1.5")
    private Double idleHours;

    @Schema(description = "Breakdown hours", example = "0.0")
    private Double breakdownHours;

    @Schema(description = "Fuel consumed (Liters)", example = "45.0")
    private Double fuelConsumedLiters;

    @Schema(description = "Remarks", example = "Operating optimally")
    private String remarks;
}

package com.example.BuildTwin._0.dto.zone;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a zone")
public class UpdateZoneRequest {

    @NotBlank(message = "Zone code is required")
    @Schema(description = "Unique zone code", example = "ZN-FL1-A")
    private String code;

    @NotBlank(message = "Zone name is required")
    @Schema(description = "Zone / Flat / Room name", example = "Zone A (Flats 101-104)")
    private String name;

    @Schema(description = "Zone type: RESIDENTIAL_UNIT, COMMON_AREA, CORRIDOR, ELECTRICAL_ROOM, DUCT_SHAFT, STAIRCASE", example = "RESIDENTIAL_UNIT")
    private String zoneType;

    @Schema(description = "Area in Sq.Ft", example = "4500.0")
    private Double areaSqFt;

    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|COMPLETED)?$",
            message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, COMPLETED"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED", example = "IN_PROGRESS")
    private String status;
}

package com.example.BuildTwin._0.dto.building;

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
@Schema(description = "Request payload for updating a building")
public class UpdateBuildingRequest {

    @NotBlank(message = "Building code is required")
    @Schema(description = "Unique building code", example = "BLD-TWR-A")
    private String code;

    @NotBlank(message = "Building name is required")
    @Schema(description = "Building display name", example = "Tower A - Residential")
    private String name;

    @Schema(description = "Building type", example = "RESIDENTIAL_TOWER")
    private String buildingType;

    @Schema(description = "Total number of floors", example = "18")
    private Integer totalFloors;

    @Schema(description = "Total built up area in Sq.Ft", example = "180000.0")
    private Double totalBuiltUpAreaSqFt;

    @Pattern(
            regexp = "(?i)^(PLANNED|UNDER_CONSTRUCTION|COMPLETED|ON_HOLD)?$",
            message = "Invalid status. Allowed: PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD"
    )
    @Schema(description = "Status: PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD", example = "UNDER_CONSTRUCTION")
    private String status;

    @Schema(description = "Description or notes")
    private String description;
}

package com.example.BuildTwin._0.dto.building;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a construction building or tower")
public class BuildingResponse {

    @Schema(description = "Building unique ID", example = "1")
    private Long id;

    @Schema(description = "Parent Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Parent Site name", example = "Tower A Site")
    private String siteName;

    @Schema(description = "Parent Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Parent Project name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Building code", example = "BLD-TWR-A")
    private String code;

    @Schema(description = "Building name", example = "Tower A - 18 Floors")
    private String name;

    @Schema(description = "Building type", example = "RESIDENTIAL_TOWER")
    private String buildingType;

    @Schema(description = "Total number of floors/levels", example = "18")
    private Integer totalFloors;

    @Schema(description = "Total built-up area in Sq.Ft", example = "180000.0")
    private Double totalBuiltUpAreaSqFt;

    @Schema(description = "Construction status (PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD)", example = "UNDER_CONSTRUCTION")
    private String status;

    @Schema(description = "Building description")
    private String description;

    @Schema(description = "Total registered floors count", example = "19")
    private int floorsCount;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}

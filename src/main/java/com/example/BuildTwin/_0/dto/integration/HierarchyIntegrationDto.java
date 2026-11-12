package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Spatial Hierarchy Module Integration Status & Rollups")
public class HierarchyIntegrationDto {

    @Schema(description = "Total sites linked to the project", example = "2")
    private Long siteCount;

    @Schema(description = "Total buildings registered across all sites", example = "4")
    private Long buildingCount;

    @Schema(description = "Total floors registered", example = "28")
    private Long floorCount;

    @Schema(description = "Total zones configured for spatial tracking", example = "84")
    private Long zoneCount;

    @Schema(description = "Module integration status", example = "CONFIGURED")
    private String status;
}

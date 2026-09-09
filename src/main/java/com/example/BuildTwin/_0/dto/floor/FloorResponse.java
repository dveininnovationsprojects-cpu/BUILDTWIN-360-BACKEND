package com.example.BuildTwin._0.dto.floor;

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
@Schema(description = "Response representation of a building floor or level")
public class FloorResponse {

    @Schema(description = "Floor unique ID", example = "1")
    private Long id;

    @Schema(description = "Parent Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Parent Building name", example = "Tower A")
    private String buildingName;

    @Schema(description = "Parent Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Parent Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Floor number (e.g. -1 for B1, 0 for Stilt, 1 for 1st Floor)", example = "1")
    private Integer floorNumber;

    @Schema(description = "Floor name", example = "First Floor")
    private String floorName;

    @Schema(description = "Floor type (BASEMENT, STILT, PODIUM, TYPICAL, REFUGE, TERRACE)", example = "TYPICAL")
    private String floorType;

    @Schema(description = "Built up area in Sq.Ft", example = "10000.0")
    private Double builtUpAreaSqFt;

    @Schema(description = "Construction status (PLANNED, IN_PROGRESS, COMPLETED)", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Total registered zones or flats count on this floor", example = "4")
    private int zonesCount;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}

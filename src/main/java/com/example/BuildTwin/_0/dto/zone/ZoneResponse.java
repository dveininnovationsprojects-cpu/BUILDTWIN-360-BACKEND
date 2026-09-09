package com.example.BuildTwin._0.dto.zone;

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
@Schema(description = "Response representation of a floor zone, unit, or flat")
public class ZoneResponse {

    @Schema(description = "Zone unique ID", example = "1")
    private Long id;

    @Schema(description = "Parent Floor ID", example = "1")
    private Long floorId;

    @Schema(description = "Parent Floor name", example = "First Floor")
    private String floorName;

    @Schema(description = "Parent Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Parent Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Parent Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Zone code", example = "ZN-FL1-A")
    private String code;

    @Schema(description = "Zone / Flat / Room name", example = "Zone A (Flats 101-104)")
    private String name;

    @Schema(description = "Zone type (RESIDENTIAL_UNIT, COMMON_AREA, CORRIDOR, ELECTRICAL_ROOM, DUCT_SHAFT, STAIRCASE)", example = "RESIDENTIAL_UNIT")
    private String zoneType;

    @Schema(description = "Area in Sq.Ft", example = "4500.0")
    private Double areaSqFt;

    @Schema(description = "Construction status (PLANNED, IN_PROGRESS, COMPLETED)", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}

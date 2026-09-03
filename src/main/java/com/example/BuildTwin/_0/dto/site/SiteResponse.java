package com.example.BuildTwin._0.dto.site;

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
@Schema(description = "Response representation of a construction site or zone")
public class SiteResponse {

    @Schema(description = "Site unique database ID", example = "1")
    private Long id;

    @Schema(description = "Parent project ID", example = "1")
    private Long projectId;

    @Schema(description = "Parent project name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Site code", example = "PADUR-TWR-A")
    private String code;

    @Schema(description = "Site name", example = "Tower A (Stilt + 18 Floors)")
    private String name;

    @Schema(description = "Site type", example = "BUILDING_TOWER")
    private String siteType;

    @Schema(description = "Physical address or site coordinates description", example = "North Zone, Padur Site")
    private String location;

    @Schema(description = "Site active status", example = "ACTIVE")
    private String status;

    @Schema(description = "GPS Latitude", example = "12.7932")
    private Double latitude;

    @Schema(description = "GPS Longitude", example = "80.2241")
    private Double longitude;

    @Schema(description = "Built-up or plot area in Sq.Ft", example = "85000.0")
    private Double areaSqFt;

    @Schema(description = "Site incharge / Lead Site Engineer", example = "Suresh Kumar")
    private String siteIncharge;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

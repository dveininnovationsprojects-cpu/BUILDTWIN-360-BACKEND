package com.example.BuildTwin._0.dto.site;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a site")
public class UpdateSiteRequest {

    @NotBlank(message = "Site code is required")
    @Schema(description = "Unique code for the site under the project", example = "PADUR-TWR-A")
    private String code;

    @NotBlank(message = "Site name is required")
    @Schema(description = "Display name of the site or zone", example = "Tower A (Stilt + 18 Floors)")
    private String name;

    @Schema(description = "Site type", example = "BUILDING_TOWER")
    private String siteType;

    @Schema(description = "Site location or zone description", example = "North Zone, Padur Campus")
    private String location;

    @Schema(description = "Site status (ACTIVE, INACTIVE, COMPLETED)", example = "ACTIVE")
    private String status;

    @Schema(description = "GPS Latitude", example = "12.7932")
    private Double latitude;

    @Schema(description = "GPS Longitude", example = "80.2241")
    private Double longitude;

    @Schema(description = "Built-up or plot area in Sq.Ft", example = "85000.0")
    private Double areaSqFt;

    @Schema(description = "Designated Site Engineer / Incharge name", example = "Suresh Kumar")
    private String siteIncharge;
}

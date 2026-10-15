package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating or relocating an activity within physical construction hierarchy")
public class RelocateActivityRequest {

    @Schema(description = "Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Floor ID", example = "2")
    private Long floorId;

    @Schema(description = "Zone ID", example = "1")
    private Long zoneId;
}

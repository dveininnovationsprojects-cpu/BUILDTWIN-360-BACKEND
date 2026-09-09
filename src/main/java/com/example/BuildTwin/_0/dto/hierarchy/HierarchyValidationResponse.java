package com.example.BuildTwin._0.dto.hierarchy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Verification response for Project-Site-Building-Floor-Zone physical relationship hierarchy")
public class HierarchyValidationResponse {

    @Schema(description = "Whether the specified path relationship is strictly valid", example = "true")
    private boolean valid;

    @Schema(description = "Detailed status message", example = "Physical hierarchy path is valid and intact")
    private String message;

    @Schema(description = "Verified project ID")
    private Long projectId;

    @Schema(description = "Verified project name")
    private String projectName;

    @Schema(description = "Verified site ID")
    private Long siteId;

    @Schema(description = "Verified site name")
    private String siteName;

    @Schema(description = "Verified building ID")
    private Long buildingId;

    @Schema(description = "Verified building name")
    private String buildingName;

    @Schema(description = "Verified floor ID")
    private Long floorId;

    @Schema(description = "Verified floor name")
    private String floorName;

    @Schema(description = "Verified zone ID")
    private Long zoneId;

    @Schema(description = "Verified zone name")
    private String zoneName;

    @Schema(description = "List of validation error reasons if invalid")
    private List<String> errors;
}

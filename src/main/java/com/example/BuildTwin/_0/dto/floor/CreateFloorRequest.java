package com.example.BuildTwin._0.dto.floor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a floor / level under a building")
public class CreateFloorRequest {

    @NotNull(message = "Floor number is required")
    @Schema(description = "Floor number (-2, -1, 0, 1, 2, ...)", example = "1")
    private Integer floorNumber;

    @NotBlank(message = "Floor name is required")
    @Schema(description = "Floor name", example = "First Typical Floor")
    private String floorName;

    @Schema(description = "Floor type: BASEMENT, STILT, PODIUM, TYPICAL, REFUGE, TERRACE", example = "TYPICAL")
    private String floorType;

    @Schema(description = "Built up area in Sq.Ft", example = "10000.0")
    private Double builtUpAreaSqFt;

    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|COMPLETED)?$",
            message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, COMPLETED"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED", example = "IN_PROGRESS")
    private String status;
}

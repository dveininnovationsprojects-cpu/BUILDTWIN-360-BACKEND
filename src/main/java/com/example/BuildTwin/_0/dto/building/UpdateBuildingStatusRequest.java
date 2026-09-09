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
@Schema(description = "Request payload for updating building status")
public class UpdateBuildingStatusRequest {

    @NotBlank(message = "Status cannot be empty")
    @Pattern(
            regexp = "(?i)^(PLANNED|UNDER_CONSTRUCTION|COMPLETED|ON_HOLD)$",
            message = "Invalid status. Allowed values are: PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD"
    )
    @Schema(description = "Status: PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD", example = "UNDER_CONSTRUCTION")
    private String status;
}

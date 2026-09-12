package com.example.BuildTwin._0.dto.wbs;

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
@Schema(description = "Request payload for updating WBS Activity execution status")
public class UpdateWbsActivityStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|COMPLETED|DELAYED|ON_HOLD)$",
            message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "IN_PROGRESS")
    private String status;
}

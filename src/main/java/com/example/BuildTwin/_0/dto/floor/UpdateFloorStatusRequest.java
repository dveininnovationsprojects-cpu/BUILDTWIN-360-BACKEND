package com.example.BuildTwin._0.dto.floor;

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
@Schema(description = "Request payload for updating floor status")
public class UpdateFloorStatusRequest {

    @NotBlank(message = "Status cannot be empty")
    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|COMPLETED)$",
            message = "Invalid status. Allowed values are: PLANNED, IN_PROGRESS, COMPLETED"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED", example = "COMPLETED")
    private String status;
}

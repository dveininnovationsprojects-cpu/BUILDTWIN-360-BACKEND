package com.example.BuildTwin._0.dto.project;

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
@Schema(description = "Request payload for updating project lifecycle status")
public class UpdateProjectStatusRequest {

    @NotBlank(message = "Status cannot be empty")
    @Pattern(
            regexp = "(?i)^(PLANNED|ACTIVE|INACTIVE|ON_HOLD|COMPLETED|ARCHIVED)$",
            message = "Invalid status. Allowed values are: PLANNED, ACTIVE, INACTIVE, ON_HOLD, COMPLETED, ARCHIVED"
    )
    @Schema(description = "Lifecycle status: PLANNED, ACTIVE, INACTIVE, ON_HOLD, COMPLETED, ARCHIVED", example = "ACTIVE")
    private String status;
}

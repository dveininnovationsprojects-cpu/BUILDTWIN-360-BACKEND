package com.example.BuildTwin._0.dto.site;

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
@Schema(description = "Request payload for updating site status")
public class UpdateSiteStatusRequest {

    @NotBlank(message = "Status cannot be empty")
    @Pattern(
            regexp = "(?i)^(PLANNED|ACTIVE|INACTIVE|COMPLETED)$",
            message = "Invalid status. Allowed values are: PLANNED, ACTIVE, INACTIVE, COMPLETED"
    )
    @Schema(description = "Site status: PLANNED, ACTIVE, INACTIVE, COMPLETED", example = "ACTIVE")
    private String status;
}

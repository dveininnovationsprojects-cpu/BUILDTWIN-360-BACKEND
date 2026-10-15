package com.example.BuildTwin._0.dto.baseline;

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
@Schema(description = "Request to freeze and capture a Project Baseline schedule")
public class CreateProjectBaselineRequest {

    @NotBlank(message = "Baseline name is required")
    @Schema(description = "Descriptive name for this baseline", example = "Baseline 1.0 (Master Contract Schedule)")
    private String name;

    @Schema(description = "Justification or change management notes for freezing this baseline", example = "Official contractual baseline agreed with client and general contractor.")
    private String description;

    @Builder.Default
    @Schema(description = "Whether to immediately set this baseline as the active reference baseline", example = "true")
    private Boolean setActive = true;
}

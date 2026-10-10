package com.example.BuildTwin._0.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcknowledgeAlertRequest {

    @NotBlank(message = "Acknowledgment notes or initial remarks are required")
    @Schema(description = "Notes recorded by engineer upon reviewing the delay alert", example = "Reviewed in site coordination meeting. Issue caused by delayed structural steel delivery.")
    private String notes;

    @Schema(description = "Estimated target date by which the site team expects the delay to be resolved or caught up", example = "2026-10-25")
    private LocalDate targetResolutionDate;
}

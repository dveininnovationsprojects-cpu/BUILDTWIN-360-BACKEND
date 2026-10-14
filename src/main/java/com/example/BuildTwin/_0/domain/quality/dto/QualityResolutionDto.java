package com.example.BuildTwin._0.domain.quality.dto;

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
@Schema(description = "Payload to submit rectification / resolution proof for a Quality Defect or NCR")
public class QualityResolutionDto {

    @NotBlank(message = "Resolution notes are required")
    @Schema(description = "Description of the corrective rectification completed", example = "Chipped off unsound concrete, applied epoxy bonding agent and non-shrink structural grout.")
    private String resolutionNotes;

    @Schema(description = "Name or ID of engineer/contractor submitting resolution", example = "Site Engineer Murugan")
    private String resolvedBy;

    @Schema(description = "Optional photo URL proving completion of rectification work", example = "https://storage.buildtwin.com/quality/rectified_slab_01.jpg")
    private String rectificationPhotoUrl;
}

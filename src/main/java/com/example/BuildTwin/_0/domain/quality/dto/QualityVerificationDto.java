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
@Schema(description = "Payload for QA Engineer / Auditor to verify and close or reopen a Quality Defect/NCR")
public class QualityVerificationDto {

    @NotBlank(message = "Verifier identifier is required")
    @Schema(description = "Name or ID of QA/QC Engineer performing physical verification", example = "QA Lead Ramesh")
    private String verifiedBy;

    @Schema(description = "True to approve and close; False to reject and reopen for re-rectification", example = "true")
    @Builder.Default
    private Boolean approved = true;

    @Schema(description = "Remarks or inspection test results", example = "Compressive core test strength verified at 35 MPa. Approved.")
    private String remarks;
}

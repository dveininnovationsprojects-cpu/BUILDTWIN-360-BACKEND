package com.example.BuildTwin._0.domain.issues.dto;

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
@Schema(description = "Payload to update Project Risk mitigation status and resolution notes")
public class RiskStatusUpdateDto {

    @NotBlank(message = "Status is required")
    @Schema(description = "Risk status: IDENTIFIED, MONITORED, MITIGATED, CLOSED", example = "MITIGATED")
    private String status;

    @Schema(description = "Updated mitigation steps taken", example = "Pre-ordered 50 tons steel from alternate vendor to hedge price hike.")
    private String mitigation;

    @Schema(description = "Resolution / closure remarks", example = "Risk materialized and handled successfully within contingency buffer.")
    private String resolutionNotes;

    @Schema(description = "Owner / Approver submitting update", example = "PM_Suresh")
    private String resolvedBy;
}

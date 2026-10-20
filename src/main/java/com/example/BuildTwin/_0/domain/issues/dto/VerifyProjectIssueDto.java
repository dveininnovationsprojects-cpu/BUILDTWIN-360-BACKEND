package com.example.BuildTwin._0.domain.issues.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Step 4: Quality verification & sign-off payload by Quality Inspector / PM")
public class VerifyProjectIssueDto {

    @Schema(description = "True if rectification passed audit (moves to CLOSED); False if rejected (reverts to OPEN)", example = "true")
    private boolean approved;

    @NotBlank(message = "Verifier name is required")
    @Schema(description = "Inspector or PM who conducted audit", example = "QA_Lead_Ramesh")
    private String verifiedBy;

    @Schema(description = "Verification observations or rejection remarks", example = "Inspected on site. Honeycombing grouted satisfactorily as per IS:456.")
    private String verificationRemarks;
}

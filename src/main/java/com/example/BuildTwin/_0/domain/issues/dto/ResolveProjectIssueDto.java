package com.example.BuildTwin._0.domain.issues.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Step 3: Action & Rectification submission by contractor")
public class ResolveProjectIssueDto {

    @NotBlank(message = "Resolution remarks are required")
    @Schema(description = "Explanation of work done to fix defect", example = "Crack filled with epoxy grout and reinforced with micro-concrete.")
    private String resolutionRemarks;

    @Schema(description = "Rectification proof photo URL", example = "https://storage.buildtwin360.com/defects/b4_resolved_proof.jpg")
    private String resolutionPhotoUrl;

    @Schema(description = "Actual date when work was completed", example = "2026-10-15")
    private LocalDate actualResolutionDate;
}

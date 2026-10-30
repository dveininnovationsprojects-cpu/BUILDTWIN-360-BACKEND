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
@Schema(description = "Payload to resolve an Issue or Blocker with corrective action notes and owner/resolver")
public class IssueResolutionDto {

    @NotBlank(message = "Resolution notes are required")
    @Schema(description = "Description of how the issue or blocker was unblocked/resolved", example = "Dewatering pumps deployed and standby DG generator connected. Water cleared.")
    private String resolutionNotes;

    @Schema(description = "Name or ID of engineer/party resolving the blocker", example = "Site Engineer Murugan")
    private String resolvedBy;

    @Schema(description = "Proof evidence photo URL demonstrating blocker resolution", example = "https://storage.buildtwin.com/issues/dewatering_complete.jpg")
    private String resolutionEvidenceUrl;
}

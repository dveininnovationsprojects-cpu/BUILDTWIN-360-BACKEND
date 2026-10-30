package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.BlockerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to resolve or update the status of a blocker on a DPR activity item")
public class ResolveDprBlockerRequest {

    @NotBlank(message = "Resolution notes are required")
    @Schema(description = "Explanation of how the blocker was resolved, action taken, or reason for waiving", example = "Backup transit mixer sourced from ancillary RMC plant; pour completed successfully.")
    private String resolutionNotes;

    @Schema(description = "Target status: RESOLVED, IN_PROGRESS, WAIVED", example = "RESOLVED")
    @Builder.Default
    private BlockerStatus status = BlockerStatus.RESOLVED;

    @Schema(description = "Optional updated operational remarks", example = "Normal pour operations resumed with 1.5 hr net delay.")
    private String remarks;
}

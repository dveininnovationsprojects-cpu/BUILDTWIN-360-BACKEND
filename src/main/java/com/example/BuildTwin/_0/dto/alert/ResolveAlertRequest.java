package com.example.BuildTwin._0.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveAlertRequest {

    @NotBlank(message = "Resolution notes are required")
    @Schema(description = "Explanation of how the delay was cleared, task completed, or schedule realigned", example = "Overtime shift successfully caught up 5 days backlog. Task finished at 100% quantity on site.")
    private String resolutionNotes;
}

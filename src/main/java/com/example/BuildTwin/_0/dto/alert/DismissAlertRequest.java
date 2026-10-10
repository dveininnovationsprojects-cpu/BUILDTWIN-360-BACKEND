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
public class DismissAlertRequest {

    @NotBlank(message = "Dismissal reason is required")
    @Schema(description = "Justification for dismissing or suppressing this delay alert", example = "Task was descoped under Change Order CO-04 and replaced by precast methodology.")
    private String dismissalReason;
}

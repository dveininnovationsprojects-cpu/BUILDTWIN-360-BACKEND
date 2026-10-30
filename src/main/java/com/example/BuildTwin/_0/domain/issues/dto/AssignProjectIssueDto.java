package com.example.BuildTwin._0.domain.issues.dto;

import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Step 2: Assignment & SLA setting payload by Project Manager")
public class AssignProjectIssueDto {

    @NotBlank(message = "Assigned person / contractor name is required")
    @Schema(description = "Contractor or Engineer assigned to rectify the issue", example = "Shree_Constructions_Contractor")
    private String assignedTo;

    @Schema(description = "Assigned person's role", example = "CONTRACTOR")
    private String assignedToRole;

    @NotNull(message = "Target resolution date (SLA) is required")
    @Schema(description = "SLA Target Date for fixing the problem", example = "2026-10-16")
    private LocalDate targetResolutionDate;

    @Schema(description = "Optional severity adjustment by PM", example = "HIGH")
    private IssueSeverity severity;
}

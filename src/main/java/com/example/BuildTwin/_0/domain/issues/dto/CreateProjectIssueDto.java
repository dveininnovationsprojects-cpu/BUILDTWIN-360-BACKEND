package com.example.BuildTwin._0.domain.issues.dto;

import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Step 1: Request payload to identify & log a site issue / defect ticket")
public class CreateProjectIssueDto {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Associated WBS Activity ID", example = "10")
    private Long wbsActivityId;

    @NotBlank(message = "Issue title is required")
    @Schema(description = "Issue title", example = "Basement Column B4 honeycombing detected")
    private String title;

    @Schema(description = "Detailed issue description", example = "Vibration was insufficient during concreting, leading to aggregate pockets.")
    private String description;

    @NotNull(message = "Issue type is required")
    @Schema(description = "Category: SNAG, NCR, ISSUE, RISK, BLOCKER", example = "NCR")
    private IssueType issueType;

    @NotNull(message = "Severity is required")
    @Schema(description = "Severity: LOW, MEDIUM, HIGH, CRITICAL", example = "CRITICAL")
    private IssueSeverity severity;

    @Schema(description = "Mobile/Web camera capture photo URL", example = "https://storage.buildtwin360.com/defects/b4_defect.jpg")
    private String initialPhotoUrl;

    @Schema(description = "Person who spotted the defect", example = "Site_Engineer_Karthik")
    private String reportedBy;

    @Schema(description = "Role of reporter", example = "SITE_ENGINEER")
    private String reportedRole;
}

package com.example.BuildTwin._0.domain.issues.dto;

import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed Project Issue Response DTO")
public class ProjectIssueResponseDto {

    private Long id;
    private String ticketNumber;
    private Long projectId;
    private Long wbsActivityId;
    private String title;
    private String description;
    private IssueType issueType;
    private IssueSeverity severity;
    private IssueStatus status;
    private String reportedBy;
    private String reportedRole;
    private String assignedTo;
    private String assignedToRole;
    private String initialPhotoUrl;
    private LocalDate targetResolutionDate;
    private LocalDate actualResolutionDate;
    private String resolutionRemarks;
    private String resolutionPhotoUrl;
    private String verifiedBy;
    private String verificationRemarks;
    private LocalDateTime verifiedAt;
    private boolean isOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.example.BuildTwin._0.domain.issues.model;

import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_issues", indexes = {
        @Index(name = "idx_proj_issue_project_status", columnList = "project_id, status"),
        @Index(name = "idx_proj_issue_type", columnList = "project_id, issue_type"),
        @Index(name = "idx_proj_issue_ticket", columnList = "ticket_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 60)
    private String ticketNumber; // Auto-generated e.g. ISS-PRJ1-1776077700000

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "wbs_activity_id")
    private Long wbsActivityId;

    @NotBlank(message = "Issue title is required")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "Issue type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 30)
    private IssueType issueType; // SNAG, NCR, ISSUE, RISK, BLOCKER

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private IssueSeverity severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IssueStatus status = IssueStatus.OPEN; // OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "reported_role", length = 50)
    private String reportedRole;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "assigned_to_role", length = 50)
    private String assignedToRole;

    @Column(name = "initial_photo_url", length = 500)
    private String initialPhotoUrl; // Photo evidence captured during Step 1

    @Column(name = "target_resolution_date")
    private LocalDate targetResolutionDate; // SLA Target Date set during Step 2

    @Column(name = "actual_resolution_date")
    private LocalDate actualResolutionDate; // Date rectified by contractor

    @Column(name = "resolution_remarks", length = 1000)
    private String resolutionRemarks; // Entered during Step 3 (e.g., "Crack filled with epoxy grout")

    @Column(name = "resolution_photo_url", length = 500)
    private String resolutionPhotoUrl; // Rectification proof photo uploaded during Step 3

    @Column(name = "verified_by", length = 100)
    private String verifiedBy; // QA Inspector / PM who audited the fix in Step 4

    @Column(name = "verification_remarks", length = 1000)
    private String verificationRemarks; // Approval notes or rejection reasons

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public boolean isOverdue() {
        return this.status != IssueStatus.CLOSED
                && this.targetResolutionDate != null
                && this.targetResolutionDate.isBefore(LocalDate.now());
    }
}

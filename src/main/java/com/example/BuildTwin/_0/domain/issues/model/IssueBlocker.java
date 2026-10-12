package com.example.BuildTwin._0.domain.issues.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Site Issue, Work Blocker or Obstacle tracking entity")
public class IssueBlocker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "title", length = 150)
    private String title;

    @Builder.Default
    @Column(name = "issue_type", length = 30)
    private String issueType = "BLOCKER"; // BLOCKER, ISSUE, SITE_DEFECT, DELAY

    @Builder.Default
    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Builder.Default
    @Column(name = "severity", length = 20)
    private String severity = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "owner", length = 100)
    private String owner; // Assigned person / contractor responsible for resolution

    @Column(name = "impact_area", length = 50)
    private String impactArea; // SCHEDULE, COST, SAFETY, WORKFLOW, LABOUR, MATERIAL, DESIGN

    @Builder.Default
    @Column(name = "days_delayed")
    private Integer daysDelayed = 0; // Number of days blocked

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "cause_code", length = 50)
    private String causeCode; // LABOUR_SHORTAGE, MATERIAL_DELAY, DESIGN_RFI, WEATHER, EQUIPMENT_BREAKDOWN

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Synchronize priority and severity
    public void setSeverity(String severity) {
        this.severity = severity;
        if (this.priority == null || this.priority.isBlank()) {
            this.priority = severity;
        }
    }

    public void setPriority(String priority) {
        this.priority = priority;
        if (this.severity == null || this.severity.isBlank()) {
            this.severity = priority;
        }
    }
}

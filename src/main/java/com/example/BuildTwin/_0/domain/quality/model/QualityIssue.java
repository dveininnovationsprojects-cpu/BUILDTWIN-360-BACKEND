package com.example.BuildTwin._0.domain.quality.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Quality Inspection Defect, Snag or Non-Conformance Report (NCR)")
public class QualityIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "title", length = 150)
    private String title;

    @Builder.Default
    @Column(name = "issue_type", length = 30)
    private String issueType = "SNAG"; // SNAG, NCR, DEFECT

    @Column(name = "ncr_number", length = 50)
    private String ncrNumber;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "activity_id")
    private Long activityId;

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Builder.Default
    @Column(name = "severity", nullable = false, length = 20)
    private String severity = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN"; // OPEN, ASSIGNED, RECTIFICATION_SUBMITTED, VERIFIED, CLOSED, REOPENED

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "responsible_party", length = 100)
    private String responsibleParty; // Owner / Contractor responsible for rectification

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "rectification_plan", columnDefinition = "TEXT")
    private String rectificationPlan;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "defect_photo_url", length = 500)
    private String defectPhotoUrl;

    @Column(name = "rectification_photo_url", length = 500)
    private String rectificationPhotoUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

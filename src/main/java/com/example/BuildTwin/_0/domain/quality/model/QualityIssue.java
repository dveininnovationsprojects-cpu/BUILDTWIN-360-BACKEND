package com.example.BuildTwin._0.domain.quality.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

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
    private String status = "OPEN"; // OPEN, ASSIGNED, RECTIFICATION_SUBMITTED, VERIFIED, CLOSED

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "responsible_party", length = 100)
    private String responsibleParty;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

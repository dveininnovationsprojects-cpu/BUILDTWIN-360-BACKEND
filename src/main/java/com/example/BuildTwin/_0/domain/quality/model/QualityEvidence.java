package com.example.BuildTwin._0.domain.quality.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quality_evidence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Quality Issue ID is required")
    @Column(name = "quality_issue_id", nullable = false)
    private Long qualityIssueId;

    @NotBlank(message = "Object URL is required")
    @Column(name = "object_url", nullable = false, length = 255)
    private String objectUrl;

    @Column(name = "stage", length = 50) // DEFECT_CAPTURE, RECTIFICATION_PROOF, VERIFICATION
    private String stage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

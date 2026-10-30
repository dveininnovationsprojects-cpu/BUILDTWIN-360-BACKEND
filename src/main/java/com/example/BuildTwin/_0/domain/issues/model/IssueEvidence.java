package com.example.BuildTwin._0.domain.issues.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_evidence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Photographic or document proof attached to a Site Issue or Work Blocker")
public class IssueEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Issue ID is required")
    @Column(name = "issue_id", nullable = false)
    private Long issueId;

    @NotBlank(message = "Object URL is required")
    @Column(name = "object_url", nullable = false, length = 500)
    private String objectUrl;

    @Builder.Default
    @Column(name = "stage", length = 50) // BLOCKER_PROOF, RESOLUTION_PROOF, SITE_INSPECTION
    private String stage = "BLOCKER_PROOF";

    @Column(name = "caption", length = 255)
    private String caption;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

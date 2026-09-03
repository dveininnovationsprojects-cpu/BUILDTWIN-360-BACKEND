package com.example.BuildTwin._0.domain.documents.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false, length = 50) // DRAWING, APPROVAL, BOQ, QUOTATION, REPORT, INVOICE_EVIDENCE
    private String category;

    @Builder.Default
    @Column(name = "version", nullable = false, length = 20)
    private String version = "v1.0";

    @NotBlank(message = "Document object URL is required")
    @Column(name = "object_url", nullable = false, length = 255)
    private String objectUrl;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, SUPERSEDES

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

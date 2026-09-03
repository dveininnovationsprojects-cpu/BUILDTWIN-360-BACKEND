package com.example.BuildTwin._0.domain.dpr.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_headers", indexes = {
        @Index(name = "idx_dpr_project_date", columnList = "project_id, report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "site_id")
    private Long siteId;

    @NotNull(message = "Report date is required")
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "weather", length = 50)
    private String weather;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "DRAFT"; // DRAFT, SUBMITTED, APPROVED

    @Column(name = "submitted_by")
    private String submittedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

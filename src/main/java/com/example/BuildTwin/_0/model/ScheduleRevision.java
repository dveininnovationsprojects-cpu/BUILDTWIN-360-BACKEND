package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_revisions",
        indexes = {
                @Index(name = "idx_sched_rev_proj", columnList = "project_id"),
                @Index(name = "idx_sched_rev_status", columnList = "status"),
                @Index(name = "idx_sched_rev_code", columnList = "revision_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(name = "revision_code", nullable = false, length = 50)
    private String revisionCode; // e.g. "REV-01", "REV-02"

    @Column(name = "revision_name", nullable = false)
    private String revisionName; // e.g. "Monsoon Delay & Scope Addition Revision"

    @Column(name = "revision_date", nullable = false)
    private LocalDate revisionDate;

    @Column(name = "reason_for_revision", columnDefinition = "TEXT")
    private String reasonForRevision;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ScheduleRevisionStatus status = ScheduleRevisionStatus.DRAFT;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "time_extension_days")
    private Integer timeExtensionDays;

    @Column(name = "cost_impact", precision = 15, scale = 2)
    private BigDecimal costImpact;

    @Column(name = "prepared_by")
    private String preparedBy;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "scheduleRevision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleRevisionItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

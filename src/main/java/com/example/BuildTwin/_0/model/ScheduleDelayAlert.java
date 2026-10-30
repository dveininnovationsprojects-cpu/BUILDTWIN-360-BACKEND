package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.BuildTwin._0.domain.projects.model.Project;

/**
 * Entity representing an automated or monitored Schedule Delay Alert for construction tasks.
 * Tracks detection of overdue activities, slippage warnings, successor cascading impacts,
 * review acknowledgments, mitigation catch-up execution, and resolution lifecycle.
 */
@Entity
@Table(name = "schedule_delay_alerts",
        indexes = {
                @Index(name = "idx_delay_alert_proj", columnList = "project_id"),
                @Index(name = "idx_delay_alert_act", columnList = "activity_id"),
                @Index(name = "idx_delay_alert_status", columnList = "status"),
                @Index(name = "idx_delay_alert_sev", columnList = "severity"),
                @Index(name = "idx_delay_alert_type", columnList = "alert_type"),
                @Index(name = "idx_delay_alert_code", columnList = "alert_code"),
                @Index(name = "idx_delay_alert_triggered", columnList = "triggered_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDelayAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @JsonIgnore
    private WbsActivity activity;

    @Column(name = "alert_code", nullable = false, length = 60)
    private String alertCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 40)
    private DelayAlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private OverdueSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DelayAlertStatus status = DelayAlertStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "aging_bucket", length = 30)
    private OverdueAgingBucket agingBucket;

    @Column(name = "delay_days")
    @Builder.Default
    private Long delayDays = 0L;

    @Column(name = "progress_slippage_percentage")
    @Builder.Default
    private Double progressSlippagePercentage = 0.0;

    @Column(name = "current_progress_percentage")
    @Builder.Default
    private Double currentProgressPercentage = 0.0;

    @Column(name = "expected_progress_percentage")
    @Builder.Default
    private Double expectedProgressPercentage = 0.0;

    @Column(name = "impacted_successor_count")
    @Builder.Default
    private Integer impactedSuccessorCount = 0;

    @Column(name = "impacted_successor_codes", length = 1000)
    private String impactedSuccessorCodes;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "discipline", length = 50)
    private String discipline;

    @Column(name = "assigned_contractor", length = 150)
    private String assignedContractor;

    // Acknowledgment tracking
    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledgment_notes", columnDefinition = "TEXT")
    private String acknowledgmentNotes;

    @Column(name = "target_resolution_date")
    private LocalDate targetResolutionDate;

    // Mitigation plan tracking
    @Column(name = "mitigation_action", columnDefinition = "TEXT")
    private String mitigationAction;

    @Column(name = "delay_reason_category", length = 80)
    private String delayReasonCategory;

    @Column(name = "delay_reason_details", columnDefinition = "TEXT")
    private String delayReasonDetails;

    @Column(name = "additional_labor_allocated")
    private Integer additionalLaborAllocated;

    // Resolution tracking
    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // Dismissal tracking
    @Column(name = "dismissed_by", length = 100)
    private String dismissedBy;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "dismissal_reason", columnDefinition = "TEXT")
    private String dismissalReason;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.triggeredAt == null) {
            this.triggeredAt = LocalDateTime.now();
        }
        if (this.lastEvaluatedAt == null) {
            this.lastEvaluatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = DelayAlertStatus.ACTIVE;
        }
    }
}

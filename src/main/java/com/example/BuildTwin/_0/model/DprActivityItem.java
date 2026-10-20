package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_activity_items",
        indexes = {
                @Index(name = "idx_dpr_act_dpr", columnList = "dpr_id"),
                @Index(name = "idx_dpr_act_activity", columnList = "activity_id"),
                @Index(name = "idx_dpr_act_blocker", columnList = "is_blocker"),
                @Index(name = "idx_dpr_act_blocker_status", columnList = "blocker_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprActivityItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dpr_id", nullable = false)
    @JsonIgnore
    private DailyProgressReport dpr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @JsonIgnore
    private WbsActivity activity;

    @Column(name = "quantity_today", nullable = false)
    private Double quantityToday;

    @Column(name = "cumulative_quantity")
    private Double cumulativeQuantity;

    @Column(name = "progress_percentage_today")
    private Double progressPercentageToday;

    @Column(name = "cumulative_progress_percentage")
    private Double cumulativeProgressPercentage;

    @Column(name = "location_reference", length = 200)
    private String locationReference;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // =========================================================================
    // Blocker & Hindrance Support
    // =========================================================================

    @Column(name = "is_blocker")
    @Builder.Default
    private Boolean isBlocker = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "blocker_category", length = 50)
    private BlockerCategory blockerCategory;

    @Column(name = "blocker_reason", columnDefinition = "TEXT")
    private String blockerReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "blocker_severity", length = 30)
    @Builder.Default
    private BlockerSeverity blockerSeverity = BlockerSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "blocker_status", length = 30)
    @Builder.Default
    private BlockerStatus blockerStatus = BlockerStatus.OPEN;

    @Column(name = "blocker_impact_hours")
    private Double impactHours;

    @Column(name = "blocker_resolution_notes", columnDefinition = "TEXT")
    private String blockerResolutionNotes;

    @Column(name = "blocker_resolved_by", length = 150)
    private String blockerResolvedBy;

    @Column(name = "blocker_resolved_at")
    private LocalDateTime blockerResolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lookahead_commitments",
        indexes = {
                @Index(name = "idx_lookahead_proj", columnList = "project_id"),
                @Index(name = "idx_lookahead_act", columnList = "activity_id"),
                @Index(name = "idx_lookahead_dates", columnList = "week_start_date, week_end_date"),
                @Index(name = "idx_lookahead_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookaheadCommitment {

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

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "target_quantity")
    private Double targetQuantity;

    @Column(name = "uom", length = 30)
    private String uom;

    @Column(name = "actual_quantity_achieved")
    @Builder.Default
    private Double actualQuantityAchieved = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private CommitmentStatus status = CommitmentStatus.COMMITTED;

    @Column(name = "crew_size")
    private Integer crewSize;

    @Column(name = "assigned_contractor")
    private String assignedContractor;

    // Last Planner Constraint / Readiness Checklist
    @Column(name = "materials_ready")
    @Builder.Default
    private Boolean materialsReady = true;

    @Column(name = "drawings_ready")
    @Builder.Default
    private Boolean drawingsReady = true;

    @Column(name = "equipment_ready")
    @Builder.Default
    private Boolean equipmentReady = true;

    @Column(name = "manpower_ready")
    @Builder.Default
    private Boolean manpowerReady = true;

    @Column(name = "safety_permit_approved")
    @Builder.Default
    private Boolean safetyPermitApproved = true;

    @Column(name = "access_clear")
    @Builder.Default
    private Boolean accessClear = true;

    @Column(name = "blocker_reason", columnDefinition = "TEXT")
    private String blockerReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "variance_reason", columnDefinition = "TEXT")
    private String varianceReason;

    @Column(name = "committed_by")
    private String committedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

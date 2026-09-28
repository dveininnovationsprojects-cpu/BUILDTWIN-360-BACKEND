package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a precedence relationship between two construction activities (WBS Activities).
 * Forms the directed edge in the CPM (Critical Path Method) and Gantt schedule network.
 */
@Entity
@Table(name = "activity_dependencies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_act_dep_pred_succ", columnNames = {"predecessor_id", "successor_id"})
        },
        indexes = {
                @Index(name = "idx_act_dep_project", columnList = "project_id"),
                @Index(name = "idx_act_dep_pred", columnList = "predecessor_id"),
                @Index(name = "idx_act_dep_succ", columnList = "successor_id"),
                @Index(name = "idx_act_dep_type", columnList = "dependency_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    /**
     * The activity that must precede (Predecessor).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_id", nullable = false)
    @JsonIgnore
    private WbsActivity predecessor;

    /**
     * The activity that depends on the predecessor (Successor).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_id", nullable = false)
    @JsonIgnore
    private WbsActivity successor;

    /**
     * Precedence type: FS (Finish-to-Start), SS (Start-to-Start), FF (Finish-to-Finish), SF (Start-to-Finish).
     * Defaults to FS.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 10)
    @Builder.Default
    private DependencyType dependencyType = DependencyType.FS;

    /**
     * Lead or Lag time in days.
     * Positive value = Lag (delay before successor can begin/end).
     * Negative value = Lead (successor can start before predecessor finishes).
     * Defaults to 0.
     */
    @Column(name = "lag_days", nullable = false)
    @Builder.Default
    private Integer lagDays = 0;

    /**
     * Engineering justification or notes for this dependency.
     * e.g., "7 days concrete curing time required before shuttering strike"
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_revision_items",
        indexes = {
                @Index(name = "idx_rev_item_rev_id", columnList = "schedule_revision_id"),
                @Index(name = "idx_rev_item_act_id", columnList = "activity_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRevisionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_revision_id", nullable = false)
    @JsonIgnore
    private ScheduleRevision scheduleRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @JsonIgnore
    private WbsActivity activity;

    @Column(name = "activity_code", nullable = false)
    private String activityCode;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "original_planned_start_date")
    private LocalDate originalPlannedStartDate;

    @Column(name = "original_planned_end_date")
    private LocalDate originalPlannedEndDate;

    @Column(name = "revised_planned_start_date")
    private LocalDate revisedPlannedStartDate;

    @Column(name = "revised_planned_end_date", nullable = false)
    private LocalDate revisedPlannedEndDate;

    @Column(name = "original_duration_days")
    private Integer originalDurationDays;

    @Column(name = "revised_duration_days")
    private Integer revisedDurationDays;

    @Column(name = "variance_days")
    private Long varianceDays;

    @Column(name = "delay_reason")
    private String delayReason;

    @Column(name = "mitigation_action", columnDefinition = "TEXT")
    private String mitigationAction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

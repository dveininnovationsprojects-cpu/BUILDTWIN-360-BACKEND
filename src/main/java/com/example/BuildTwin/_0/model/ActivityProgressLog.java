package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_progress_logs",
        indexes = {
                @Index(name = "idx_prog_log_act", columnList = "activity_id"),
                @Index(name = "idx_prog_log_date", columnList = "log_date"),
                @Index(name = "idx_prog_log_source", columnList = "source")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @JsonIgnore
    private WbsActivity activity;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "quantity_completed_today", nullable = false)
    private Double quantityCompletedToday;

    @Column(name = "cumulative_quantity_completed", nullable = false)
    private Double cumulativeQuantityCompleted;

    @Column(name = "cumulative_progress_percentage", nullable = false)
    private Double cumulativeProgressPercentage;

    @Column(name = "manpower_count")
    private Integer manpowerCount;

    @Column(name = "equipment_used", length = 300)
    private String equipmentUsed;

    @Column(name = "site_hindrance_notes", columnDefinition = "TEXT")
    private String siteHindranceNotes;

    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    @Column(name = "recorded_by", nullable = false)
    private String recordedBy;

    // Blocker & Remarks tracking
    @Column(name = "is_blocker")
    @Builder.Default
    private Boolean isBlocker = false;

    @Column(name = "blocker_category", length = 50)
    private String blockerCategory;

    @Column(name = "blocker_reason", columnDefinition = "TEXT")
    private String blockerReason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Source tracking: "DPR" or "MANUAL_LOG"
    @Column(name = "source", length = 30)
    @Builder.Default
    private String source = "MANUAL_LOG";

    @Column(name = "dpr_id")
    private Long dprId;

    @Column(name = "dpr_report_code", length = 100)
    private String dprReportCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

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
                @Index(name = "idx_dpr_act_activity", columnList = "activity_id")
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

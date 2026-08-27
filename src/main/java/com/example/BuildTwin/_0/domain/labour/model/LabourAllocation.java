package com.example.BuildTwin._0.domain.labour.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "labour_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabourAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labour_daily_id", nullable = false)
    private LabourDaily labourDaily;

    @Column(name = "wbs_activity_id")
    private Long wbsActivityId;

    @NotNull(message = "Hours allocated is required")
    @Positive(message = "Allocated hours must be greater than zero")
    @Column(name = "hours_allocated", nullable = false, precision = 6, scale = 2)
    private BigDecimal hoursAllocated;

    @Column(name = "activity_description", length = 255)
    private String activityDescription;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

package com.example.BuildTwin._0.domain.dpr.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "dpr_activity_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprActivityProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "DPR Header ID is required")
    @Column(name = "dpr_header_id", nullable = false)
    private Long dprHeaderId;

    @NotNull(message = "Activity ID is required")
    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "qty_today", precision = 12, scale = 2)
    private BigDecimal qtyToday;

    @Column(name = "cumulative_qty", precision = 12, scale = 2)
    private BigDecimal cumulativeQty;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}

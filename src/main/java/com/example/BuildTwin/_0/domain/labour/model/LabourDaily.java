package com.example.BuildTwin._0.domain.labour.model;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "labour_daily_records", indexes = {
        @Index(name = "idx_labour_daily_date_project", columnList = "record_date, project_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabourDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Record date is required")
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "site_id")
    private Long siteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    @NotNull(message = "Trade category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_category", nullable = false, length = 50)
    private TradeCategory tradeCategory;

    @Min(value = 0, message = "Headcount must be non-negative")
    @Column(name = "headcount", nullable = false)
    private Integer headcount;

    @Column(name = "standard_hours", precision = 5, scale = 2)
    private BigDecimal standardHours;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

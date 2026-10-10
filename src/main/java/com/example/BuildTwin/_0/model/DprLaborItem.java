package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_labor_items",
        indexes = {
                @Index(name = "idx_dpr_labor_dpr", columnList = "dpr_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprLaborItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dpr_id", nullable = false)
    @JsonIgnore
    private DailyProgressReport dpr;

    @Column(name = "trade_category", nullable = false, length = 100)
    private String tradeCategory;

    @Column(name = "headcount", nullable = false)
    private Integer headcount;

    @Column(name = "hours_worked")
    @Builder.Default
    private Double hoursWorked = 8.0;

    @Column(name = "subcontractor_name", length = 150)
    private String subcontractorName;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

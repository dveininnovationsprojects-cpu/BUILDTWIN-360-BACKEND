package com.example.BuildTwin._0.domain.cost.model;

import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", indexes = {
        @Index(name = "idx_budget_project_cost", columnList = "project_id, cost_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "activity_id")
    private Long activityId;

    @NotBlank(message = "Cost code is required")
    @Column(name = "cost_code", nullable = false, length = 50)
    private String costCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_head", length = 30)
    private CostHead costHead;

    @NotNull(message = "Baseline amount is required")
    @Min(value = 0, message = "Baseline amount must be non-negative")
    @Column(name = "baseline_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal baselineAmount;

    @Builder.Default
    @Column(name = "revised_amount", precision = 14, scale = 2)
    private BigDecimal revisedAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

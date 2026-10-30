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
import com.example.BuildTwin._0.domain.projects.model.Project;

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

    @Column(name = "wbs_activity_id")
    private Long wbsActivityId;

    @NotBlank(message = "Cost code is required")
    @Column(name = "cost_code", nullable = false, length = 50)
    private String costCode;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_head", length = 30)
    private CostHead costHead;

    @NotNull(message = "Baseline amount is required")
    @Min(value = 0, message = "Baseline amount must be non-negative")
    @Column(name = "baseline_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal baselineAmount;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "revised_amount", precision = 14, scale = 2)
    private BigDecimal revisedAmount = BigDecimal.ZERO;

    @Column(name = "revision_reason", length = 500)
    private String revisionReason;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "APPROVED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getWbsActivityId() {
        return wbsActivityId != null ? wbsActivityId : activityId;
    }

    public BigDecimal getAllocatedAmount() {
        return baselineAmount;
    }

    public BigDecimal getTotalWorkingBudget() {
        BigDecimal base = baselineAmount != null ? baselineAmount : BigDecimal.ZERO;
        BigDecimal rev = revisedAmount != null ? revisedAmount : BigDecimal.ZERO;
        return base.add(rev);
    }
}

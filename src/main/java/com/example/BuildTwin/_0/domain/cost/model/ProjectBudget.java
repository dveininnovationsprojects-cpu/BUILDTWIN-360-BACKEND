package com.example.BuildTwin._0.domain.cost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_budgets", indexes = {
        @Index(name = "idx_project_budget_act_code", columnList = "project_id, wbs_activity_id, cost_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "wbs_activity_id")
    private Long wbsActivityId;

    @NotBlank(message = "Cost code is required")
    @Column(name = "cost_code", nullable = false, length = 50)
    private String costCode;

    @NotNull(message = "Allocated amount is required")
    @Min(value = 0, message = "Allocated amount must be non-negative")
    @Column(name = "allocated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedAmount;

    @Builder.Default
    @Column(name = "revised_amount", precision = 15, scale = 2)
    private BigDecimal revisedAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

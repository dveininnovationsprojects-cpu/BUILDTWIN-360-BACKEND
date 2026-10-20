package com.example.BuildTwin._0.domain.cost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cost_actual_ledgers", indexes = {
        @Index(name = "idx_cost_actual_proj_code", columnList = "project_id, cost_code"),
        @Index(name = "idx_cost_actual_source", columnList = "source_type, source_reference_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostActualLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "wbs_activity_id")
    private Long wbsActivityId;

    @NotBlank(message = "Source type is required")
    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType; // LABOUR_TIMESHEET, STOCK_CONSUMPTION, DIRECT_INVOICE

    @Column(name = "source_reference_id")
    private Long sourceReferenceId; // Timesheet ID or Stock Ledger Transaction ID

    @NotBlank(message = "Cost code is required")
    @Column(name = "cost_code", nullable = false, length = 50)
    private String costCode;

    @NotNull(message = "Incurred quantity is required")
    @Column(name = "incurred_qty", nullable = false, precision = 12, scale = 2)
    private BigDecimal incurredQty;

    @NotNull(message = "Unit rate is required")
    @Column(name = "unit_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitRate;

    @NotNull(message = "Total cost is required")
    @Column(name = "total_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCost;

    @NotNull(message = "Expense date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

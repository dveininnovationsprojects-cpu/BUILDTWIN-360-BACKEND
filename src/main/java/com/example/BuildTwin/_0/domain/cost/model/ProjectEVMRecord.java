package com.example.BuildTwin._0.domain.cost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_evm_records", indexes = {
        @Index(name = "idx_evm_project_date", columnList = "project_id, evaluation_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEVMRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotNull(message = "Evaluation date is required")
    @Column(name = "evaluation_date", nullable = false)
    private LocalDate evaluationDate;

    @NotNull
    @Column(name = "planned_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal plannedValue; // PV

    @NotNull
    @Column(name = "earned_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal earnedValue; // EV

    @NotNull
    @Column(name = "actual_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal actualCost; // AC

    @NotNull
    @Column(name = "budget_at_completion", nullable = false, precision = 15, scale = 2)
    private BigDecimal budgetAtCompletion; // BAC

    @Column(name = "cost_variance", precision = 15, scale = 2)
    private BigDecimal costVariance; // CV = EV - AC

    @Column(name = "schedule_variance", precision = 15, scale = 2)
    private BigDecimal scheduleVariance; // SV = EV - PV

    @Column(name = "cpi", precision = 6, scale = 3)
    private BigDecimal cpi; // CPI = EV / AC

    @Column(name = "spi", precision = 6, scale = 3)
    private BigDecimal spi; // SPI = EV / PV

    @Column(name = "estimate_at_completion", precision = 15, scale = 2)
    private BigDecimal estimateAtCompletion; // EAC = BAC / CPI

    @Column(name = "cost_status", length = 30)
    private String costStatus;

    @Column(name = "schedule_status", length = 30)
    private String scheduleStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

package com.example.BuildTwin._0.domain.materials.model;

import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable Stock Ledger Entity.
 * Represents an append-only audit trail for material receipts, issues, returns, and adjustments.
 * All fields are updatable = false to guarantee ledger immutability.
 */
@Entity
@Table(name = "stock_ledger_entries", indexes = {
        @Index(name = "idx_ledger_material_project", columnList = "material_id, project_id"),
        @Index(name = "idx_ledger_timestamp", columnList = "timestamp")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", updatable = false, nullable = false)
    private Long projectId;

    @Column(name = "site_id", updatable = false)
    private Long siteId;

    @Column(name = "activity_id", updatable = false)
    private Long activityId;

    @Column(name = "zone", updatable = false, length = 100)
    private String zone;

    @Column(name = "contractor_id", updatable = false)
    private Long contractorId;

    @NotNull(message = "Material is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", updatable = false, nullable = false)
    private Material material;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", updatable = false, nullable = false, length = 30)
    private StockTransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Transaction quantity must be positive")
    @Column(name = "quantity", updatable = false, nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", updatable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reference_id", updatable = false, length = 100)
    private String referenceId;

    @Column(name = "remarks", updatable = false, length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false, nullable = false)
    private LocalDateTime timestamp;

    @PreUpdate
    public void onPreUpdate() {
        throw new UnsupportedOperationException("Stock ledger entries are immutable and cannot be updated.");
    }

    @PreRemove
    public void onPreRemove() {
        throw new UnsupportedOperationException("Stock ledger entries are immutable and cannot be deleted.");
    }
}

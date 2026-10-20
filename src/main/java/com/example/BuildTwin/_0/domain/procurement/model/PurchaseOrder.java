package com.example.BuildTwin._0.domain.procurement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number", length = 50)
    private String poNumber;

    @NotNull(message = "Supplier ID is required")
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "order_qty", precision = 12, scale = 2)
    private BigDecimal orderQty;

    @Column(name = "unit_rate", precision = 12, scale = 2)
    private BigDecimal unitRate;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING_APPROVAL"; // PENDING_APPROVAL, APPROVED, ISSUED, PARTIALLY_DELIVERED, FULFILLED, CANCELLED

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

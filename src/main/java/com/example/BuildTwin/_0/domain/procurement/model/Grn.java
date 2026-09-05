package com.example.BuildTwin._0.domain.procurement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grn")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Purchase Order ID is required")
    @Column(name = "po_id", nullable = false)
    private Long poId;

    @NotNull(message = "Material ID is required")
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "received_qty", precision = 12, scale = 2)
    private BigDecimal receivedQty;

    @Column(name = "accepted_qty", precision = 12, scale = 2)
    private BigDecimal acceptedQty;

    @Column(name = "rejected_qty", precision = 12, scale = 2)
    private BigDecimal rejectedQty;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "grn_number", length = 50)
    private String grnNumber;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "delivery_evidence_url", length = 255)
    private String deliveryEvidenceUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

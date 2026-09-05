package com.example.BuildTwin._0.domain.procurement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotNull(message = "Material ID is required")
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @NotNull(message = "Required quantity is required")
    @Column(name = "required_qty", nullable = false, precision = 12, scale = 2)
    private BigDecimal requiredQty;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, ORDERED

    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "wbs_activity_id")
    private Long wbsActivityId;

    @Column(name = "zone", length = 100)
    private String zone;

    @Column(name = "contractor_id")
    private Long contractorId;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

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

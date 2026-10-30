package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_material_items",
        indexes = {
                @Index(name = "idx_dpr_mat_dpr", columnList = "dpr_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprMaterialItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dpr_id", nullable = false)
    @JsonIgnore
    private DailyProgressReport dpr;

    @Column(name = "material_name", nullable = false, length = 150)
    private String materialName;

    @Column(name = "unit_of_measure", nullable = false, length = 30)
    private String unitOfMeasure;

    @Column(name = "quantity_received")
    @Builder.Default
    private Double quantityReceived = 0.0;

    @Column(name = "quantity_consumed")
    @Builder.Default
    private Double quantityConsumed = 0.0;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @Column(name = "challan_or_invoice_number", length = 100)
    private String challanOrInvoiceNumber;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

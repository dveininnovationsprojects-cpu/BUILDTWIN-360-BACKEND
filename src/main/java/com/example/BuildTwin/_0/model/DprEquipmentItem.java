package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_equipment_items",
        indexes = {
                @Index(name = "idx_dpr_equip_dpr", columnList = "dpr_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprEquipmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dpr_id", nullable = false)
    @JsonIgnore
    private DailyProgressReport dpr;

    @Column(name = "equipment_name", nullable = false, length = 150)
    private String equipmentName;

    @Column(name = "equipment_code", length = 50)
    private String equipmentCode;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "operating_hours")
    @Builder.Default
    private Double operatingHours = 0.0;

    @Column(name = "idle_hours")
    @Builder.Default
    private Double idleHours = 0.0;

    @Column(name = "breakdown_hours")
    @Builder.Default
    private Double breakdownHours = 0.0;

    @Column(name = "fuel_consumed_liters")
    private Double fuelConsumedLiters;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

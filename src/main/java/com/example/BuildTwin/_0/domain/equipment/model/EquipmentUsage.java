package com.example.BuildTwin._0.domain.equipment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Equipment ID is required")
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotNull(message = "Usage date is required")
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "hours_used", precision = 5, scale = 2)
    private BigDecimal hoursUsed;

    @Column(name = "downtime_hours", precision = 5, scale = 2)
    private BigDecimal downtimeHours;
}

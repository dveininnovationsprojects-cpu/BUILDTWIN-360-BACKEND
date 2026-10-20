package com.example.BuildTwin._0.domain.wbs.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activity_wbs", columnList = "project_id, wbs_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotBlank(message = "WBS Code is required")
    @Column(name = "wbs_code", nullable = false, length = 50)
    private String wbsCode;

    @NotBlank(message = "Activity name is required")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "planned_qty", precision = 12, scale = 2)
    private BigDecimal plannedQty;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "weightage", precision = 5, scale = 2)
    private BigDecimal weightage;

    @Column(name = "contractor_id")
    private Long contractorId;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "PLANNED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

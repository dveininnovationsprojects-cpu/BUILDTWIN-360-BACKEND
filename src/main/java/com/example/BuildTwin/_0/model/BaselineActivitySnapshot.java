package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "baseline_activity_snapshots",
        indexes = {
                @Index(name = "idx_snap_baseline", columnList = "baseline_id"),
                @Index(name = "idx_snap_act", columnList = "activity_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaselineActivitySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baseline_id", nullable = false)
    @JsonIgnore
    private ProjectBaseline baseline;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "activity_code", nullable = false)
    private String activityCode;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "work_package_id")
    private Long workPackageId;

    @Column(name = "work_package_name")
    private String workPackageName;

    @Column(name = "discipline")
    private String discipline;

    @Column(name = "uom")
    private String uom;

    @Column(name = "planned_quantity")
    private Double plannedQuantity;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "planned_duration_days")
    private Integer plannedDurationDays;

    @Column(name = "weightage")
    private Double weightage;

    @Column(name = "assigned_contractor")
    private String assignedContractor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

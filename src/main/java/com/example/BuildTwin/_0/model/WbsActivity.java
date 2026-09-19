package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wbs_activities", indexes = {
        @Index(name = "idx_wbs_act_work_pkg", columnList = "work_package_id"),
        @Index(name = "idx_wbs_act_proj", columnList = "project_id"),
        @Index(name = "idx_wbs_act_parent", columnList = "parent_id"),
        @Index(name = "idx_wbs_act_status", columnList = "status"),
        @Index(name = "idx_wbs_act_discipline", columnList = "discipline")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WbsActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_package_id", nullable = false)
    @JsonIgnore
    private WorkPackage workPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private WbsActivity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<WbsActivity> children = new ArrayList<>();

    @Column(name = "wbs_level")
    @Builder.Default
    private Integer level = 1; // 1 = Top-level task under WorkPackage, 2 = Sub-task, 3 = Micro-task

    @Column(name = "wbs_path", length = 500)
    private String wbsPath; // e.g. "/1" or "/1/4" or "/1/4/9"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    @JsonIgnore
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    @JsonIgnore
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    @JsonIgnore
    private Floor floor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    @JsonIgnore
    private Zone zone;

    @Column(name = "code", nullable = false)
    private String code; // e.g., "ACT-CIV-001" or "WBS-1.1"

    @Column(name = "name", nullable = false)
    private String name; // e.g., "Substructure Concrete Works"

    @Column(name = "discipline", nullable = false)
    private String discipline; // CIVIL, STRUCTURAL, MEP, ELECTRICAL, PLUMBING, HVAC, FINISHING

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "uom", nullable = false, length = 30)
    private String uom; // Unit of Measure: CUM, SQFT, SQM, RMT, KG, MT, NOS, POINTS, PERCENT

    @Column(name = "planned_quantity", nullable = false)
    private Double plannedQuantity;

    @Column(name = "completed_quantity")
    @Builder.Default
    private Double completedQuantity = 0.0;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Double progressPercentage = 0.0;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PLANNED"; // PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD

    @Column(name = "assigned_contractor")
    private String assignedContractor;

    @Column(name = "incharge_user_id")
    private Long inchargeUserId;

    @Column(name = "weightage")
    @Builder.Default
    private Double weightage = 1.0;

    @Column(name = "sequence_order")
    @Builder.Default
    private Integer sequenceOrder = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}

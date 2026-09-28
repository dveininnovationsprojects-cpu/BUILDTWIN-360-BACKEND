package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_baselines",
        indexes = {
                @Index(name = "idx_baseline_proj", columnList = "project_id"),
                @Index(name = "idx_baseline_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectBaseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(name = "name", nullable = false)
    private String name; // e.g. "Baseline 1.0 (Approved Master Schedule)"

    @Column(name = "version", nullable = false)
    private Integer version; // e.g. 1, 2, 3

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "total_activities")
    private Integer totalActivities;

    @Column(name = "total_work_packages")
    private Integer totalWorkPackages;

    @Column(name = "total_budget_amount", precision = 15, scale = 2)
    private BigDecimal totalBudgetAmount;

    @Column(name = "baseline_start_date")
    private LocalDate baselineStartDate;

    @Column(name = "baseline_end_date")
    private LocalDate baselineEndDate;

    @OneToMany(mappedBy = "baseline", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BaselineActivitySnapshot> snapshots = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

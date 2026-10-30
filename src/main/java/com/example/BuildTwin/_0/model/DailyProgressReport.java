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
@Table(name = "daily_progress_reports",
        indexes = {
                @Index(name = "idx_dpr_project", columnList = "project_id"),
                @Index(name = "idx_dpr_date", columnList = "report_date"),
                @Index(name = "idx_dpr_status", columnList = "status"),
                @Index(name = "idx_dpr_code", columnList = "report_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dpr_project_date_shift", columnNames = {"project_id", "report_date", "shift"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, unique = true, length = 60)
    private String reportCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false, length = 30)
    @Builder.Default
    private WorkShift shift = WorkShift.DAY_SHIFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_condition", nullable = false, length = 30)
    @Builder.Default
    private WeatherCondition weatherCondition = WeatherCondition.SUNNY;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DprStatus status = DprStatus.DRAFT;

    @Column(name = "site_engineer", length = 150)
    private String siteEngineer;

    // Workflow Submission Details
    @Column(name = "submitted_by", length = 150)
    private String submittedBy;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes;

    // Workflow Approval Details
    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    // Workflow Rejection Details
    @Column(name = "rejected_by", length = 150)
    private String rejectedBy;

    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "safety_incidents_count")
    @Builder.Default
    private Integer safetyIncidentsCount = 0;

    @Column(name = "safety_notes", columnDefinition = "TEXT")
    private String safetyNotes;

    @Column(name = "quality_observations", columnDefinition = "TEXT")
    private String qualityObservations;

    @Column(name = "site_hindrances", columnDefinition = "TEXT")
    private String siteHindrances;

    @Column(name = "total_manpower_count")
    @Builder.Default
    private Integer totalManpowerCount = 0;

    @Column(name = "total_equipment_count")
    @Builder.Default
    private Integer totalEquipmentCount = 0;

    @Column(name = "total_activities_logged")
    @Builder.Default
    private Integer totalActivitiesLogged = 0;

    @Column(name = "total_blockers_count")
    @Builder.Default
    private Integer totalBlockersCount = 0;

    @Column(name = "open_blockers_count")
    @Builder.Default
    private Integer openBlockersCount = 0;

    @OneToMany(mappedBy = "dpr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DprActivityItem> activityItems = new ArrayList<>();

    @OneToMany(mappedBy = "dpr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DprLaborItem> laborItems = new ArrayList<>();

    @OneToMany(mappedBy = "dpr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DprEquipmentItem> equipmentItems = new ArrayList<>();

    @OneToMany(mappedBy = "dpr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DprMaterialItem> materialItems = new ArrayList<>();

    @OneToMany(mappedBy = "dpr", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DprPhotoAttachment> photoAttachments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void recalculateTotals() {
        this.totalActivitiesLogged = activityItems != null ? activityItems.size() : 0;
        this.totalManpowerCount = laborItems != null
                ? laborItems.stream().mapToInt(l -> l.getHeadcount() != null ? l.getHeadcount() : 0).sum()
                : 0;
        this.totalEquipmentCount = equipmentItems != null
                ? equipmentItems.stream().mapToInt(e -> e.getQuantity() != null ? e.getQuantity() : 0).sum()
                : 0;
        this.totalBlockersCount = activityItems != null
                ? (int) activityItems.stream().filter(i -> Boolean.TRUE.equals(i.getIsBlocker())).count()
                : 0;
        this.openBlockersCount = activityItems != null
                ? (int) activityItems.stream().filter(i -> Boolean.TRUE.equals(i.getIsBlocker())
                        && (i.getBlockerStatus() == null || i.getBlockerStatus() == BlockerStatus.OPEN || i.getBlockerStatus() == BlockerStatus.IN_PROGRESS)).count()
                : 0;
    }
}

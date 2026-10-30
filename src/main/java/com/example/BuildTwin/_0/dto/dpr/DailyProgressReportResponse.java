package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.DprStatus;
import com.example.BuildTwin._0.model.WeatherCondition;
import com.example.BuildTwin._0.model.WorkShift;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Complete Daily Progress Report (DPR) detail representation with Workflow & Blocker Support")
public class DailyProgressReportResponse {

    @Schema(description = "DPR Report ID", example = "1")
    private Long id;

    @Schema(description = "Unique Report Tracking Code", example = "DPR-PRJ1-20261010-DAY")
    private String reportCode;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Heights Luxury Towers")
    private String projectName;

    @Schema(description = "Project Code", example = "PRJ-SKY-001")
    private String projectCode;

    @Schema(description = "Date of Report", example = "2026-10-10")
    private LocalDate reportDate;

    @Schema(description = "Shift of work", example = "DAY_SHIFT")
    private WorkShift shift;

    @Schema(description = "Weather condition", example = "SUNNY")
    private WeatherCondition weatherCondition;

    @Schema(description = "Ambient site temperature in Celsius", example = "32.5")
    private Double temperatureCelsius;

    @Schema(description = "Report lifecycle status: DRAFT, SUBMITTED, APPROVED, REJECTED", example = "APPROVED")
    private DprStatus status;

    @Schema(description = "Site engineer author", example = "Engr. Rajesh Kumar")
    private String siteEngineer;

    // Workflow Tracking Fields
    @Schema(description = "User who submitted the report for review", example = "Engr. Rajesh Kumar")
    private String submittedBy;

    @Schema(description = "Timestamp when report was submitted", example = "2026-10-10T17:30:00")
    private LocalDateTime submissionDate;

    @Schema(description = "Submission handover remarks", example = "Slab pour Level 4 completed; ready for sign-off.")
    private String submissionNotes;

    @Schema(description = "Approving Project Manager / Director", example = "PM Senthil Nathan")
    private String approvedBy;

    @Schema(description = "Timestamp when report was approved", example = "2026-10-10T18:00:00")
    private LocalDateTime approvalDate;

    @Schema(description = "Managerial approval notes", example = "Approved. Cube tests logged with QA.")
    private String approvalNotes;

    @Schema(description = "User who rejected the report", example = "PM Senthil Nathan")
    private String rejectedBy;

    @Schema(description = "Timestamp when report was rejected", example = "2026-10-10T18:30:00")
    private LocalDateTime rejectionDate;

    @Schema(description = "Rejection feedback if returned", example = "null")
    private String rejectionReason;

    @Schema(description = "General notes", example = "Tower A 3rd floor slab casting in progress")
    private String generalNotes;

    @Schema(description = "Safety incidents count", example = "0")
    private Integer safetyIncidentsCount;

    @Schema(description = "Safety notes", example = "Toolbox talk conducted on height safety")
    private String safetyNotes;

    @Schema(description = "Quality observations", example = "Concrete slump 120mm confirmed")
    private String qualityObservations;

    @Schema(description = "Site hindrances", example = "None")
    private String siteHindrances;

    @Schema(description = "Total manpower logged today across trades", example = "48")
    private Integer totalManpowerCount;

    @Schema(description = "Total equipment deployed today", example = "5")
    private Integer totalEquipmentCount;

    @Schema(description = "Number of active activities logged today", example = "3")
    private Integer totalActivitiesLogged;

    @Schema(description = "Total blockers logged today in this DPR", example = "2")
    private Integer totalBlockersCount;

    @Schema(description = "Total open blockers requiring resolution", example = "1")
    private Integer openBlockersCount;

    @Schema(description = "Creation timestamp", example = "2026-10-10T17:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-10-10T18:00:00")
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<DprActivityItemResponse> activityItems = new ArrayList<>();

    @Builder.Default
    private List<DprLaborItemResponse> laborItems = new ArrayList<>();

    @Builder.Default
    private List<DprEquipmentItemResponse> equipmentItems = new ArrayList<>();

    @Builder.Default
    private List<DprMaterialItemResponse> materialItems = new ArrayList<>();

    @Builder.Default
    private List<DprPhotoAttachmentResponse> photoAttachments = new ArrayList<>();
}

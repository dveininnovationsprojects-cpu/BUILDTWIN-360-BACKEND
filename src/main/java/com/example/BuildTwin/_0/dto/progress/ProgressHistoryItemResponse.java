package com.example.BuildTwin._0.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Individual chronological site progress history entry with production quantities, manpower, equipment, and blocker details")
public class ProgressHistoryItemResponse {

    @Schema(description = "Progress Log Entry ID", example = "101")
    private Long id;

    @Schema(description = "Activity ID", example = "15")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Trade Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Work Package ID", example = "3")
    private Long workPackageId;

    @Schema(description = "Work Package Name", example = "Civil Works Package")
    private String workPackageName;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Unit of Measure (UOM)", example = "CUM")
    private String uom;

    @Schema(description = "Date progress was logged", example = "2026-09-28")
    private LocalDate logDate;

    @Schema(description = "Physical quantity completed on this specific day", example = "45.0")
    private Double quantityCompletedToday;

    @Schema(description = "Cumulative quantity completed up to and including this date", example = "220.0")
    private Double cumulativeQuantityCompleted;

    @Schema(description = "Cumulative progress percentage achieved to date", example = "44.0")
    private Double cumulativeProgressPercentage;

    @Schema(description = "Progress percentage contribution achieved on this date alone", example = "9.0")
    private Double progressPercentageToday;

    @Schema(description = "Source channel: DPR (approved Daily Progress Report) or MANUAL_LOG (site engineer direct entry)", example = "DPR")
    private String source;

    @Schema(description = "Linked Daily Progress Report ID if origin is DPR", example = "12")
    private Long dprId;

    @Schema(description = "Linked Daily Progress Report Code", example = "DPR-20260928-S1")
    private String dprReportCode;

    @Schema(description = "Total manpower count deployed on shift", example = "14")
    private Integer manpowerCount;

    @Schema(description = "Equipment and machinery deployed on site", example = "1 JCB Excavator, 2 Tipper Trucks")
    private String equipmentUsed;

    @Schema(description = "Site delays, material shortage notes or environmental hindrances", example = "Delayed 1.5 hours due to afternoon thunderstorm")
    private String siteHindranceNotes;

    @Schema(description = "Weather condition recorded on site", example = "RAINY")
    private String weatherCondition;

    @Schema(description = "User or engineer who recorded this progress", example = "site_engineer_01")
    private String recordedBy;

    @Schema(description = "Whether this progress entry logged an active site blocker", example = "false")
    private Boolean isBlocker;

    @Schema(description = "Category of blocker if applicable", example = "WEATHER")
    private String blockerCategory;

    @Schema(description = "Root cause justification for blocker", example = "Heavy rainfall flooded lower basement trench")
    private String blockerReason;

    @Schema(description = "Operational site remarks and handover notes", example = "Completed grid A1-A4 slab pouring. Ready for cube curing testing.")
    private String remarks;

    @Schema(description = "Audit timestamp when log entry was created")
    private LocalDateTime createdAt;
}

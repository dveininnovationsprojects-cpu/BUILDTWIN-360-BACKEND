package com.example.BuildTwin._0.dto.baseline;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a logged daily progress entry")
public class ActivityProgressLogResponse {

    @Schema(description = "Log entry ID", example = "1")
    private Long id;

    @Schema(description = "Activity ID", example = "1")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Site Excavation & Earthwork")
    private String activityName;

    @Schema(description = "UOM", example = "CUM")
    private String uom;

    @Schema(description = "Log date", example = "2026-09-28")
    private LocalDate logDate;

    @Schema(description = "Quantity completed today", example = "45.0")
    private Double quantityCompletedToday;

    @Schema(description = "Cumulative quantity completed to date", example = "220.0")
    private Double cumulativeQuantityCompleted;

    @Schema(description = "Cumulative progress percentage to date", example = "44.0")
    private Double cumulativeProgressPercentage;

    @Schema(description = "Progress percentage achieved today", example = "9.0")
    private Double progressPercentageToday;

    @Schema(description = "Manpower count deployed", example = "12")
    private Integer manpowerCount;

    @Schema(description = "Equipment utilized", example = "1 JCB Excavator")
    private String equipmentUsed;

    @Schema(description = "Site hindrance notes", example = "Delayed due to afternoon shower")
    private String siteHindranceNotes;

    @Schema(description = "Weather condition", example = "OVERCAST")
    private String weatherCondition;

    @Schema(description = "Site engineer or user who recorded progress", example = "site_engineer_01")
    private String recordedBy;

    @Schema(description = "Whether this log entry flagged a site hindrance or blocker", example = "false")
    private Boolean isBlocker;

    @Schema(description = "Blocker category if applicable", example = "WEATHER")
    private String blockerCategory;

    @Schema(description = "Blocker reason or root cause", example = "Heavy rainfall caused flooding in excavation pit")
    private String blockerReason;

    @Schema(description = "Operational remarks or handover comments", example = "Excavation pit 2 pumped dry; ready for rebar inspection")
    private String remarks;

    @Schema(description = "Source origin: DPR or MANUAL_LOG", example = "DPR")
    private String source;

    @Schema(description = "Linked DPR ID if created via Daily Progress Report approval", example = "10")
    private Long dprId;

    @Schema(description = "Linked DPR report code", example = "DPR-20260928-S1")
    private String dprReportCode;

    @Schema(description = "Record timestamp")
    private LocalDateTime createdAt;
}

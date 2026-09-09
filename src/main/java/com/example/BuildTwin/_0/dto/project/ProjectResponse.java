package com.example.BuildTwin._0.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary representation of a construction project")
public class ProjectResponse {

    @Schema(description = "Project ID", example = "1")
    private Long id;

    @Schema(description = "Project display name", example = "Ashok Grandeur")
    private String name;

    @Schema(description = "Unique project code", example = "PADUR-AG-01")
    private String code;

    @Schema(description = "Brief scope of work or description", example = "18-storey premium residential development with 220 luxury units in Padur, Chennai.")
    private String description;

    @Schema(description = "Client or developer name", example = "Ashok Builders & Developers")
    private String clientName;

    @Schema(description = "Project type (RESIDENTIAL, COMMERCIAL, INFRASTRUCTURE, INDUSTRIAL)", example = "RESIDENTIAL")
    private String projectType;

    @Schema(description = "Project site address / geographic location", example = "Padur, OMR, Chennai, Tamil Nadu")
    private String location;

    @Schema(description = "Current lifecycle status (PLANNED, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED)", example = "ACTIVE")
    private String status;

    @Schema(description = "Target project start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Target project completion date", example = "2028-06-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual execution start date", example = "2026-09-05")
    private LocalDate actualStartDate;

    @Schema(description = "Actual handover date")
    private LocalDate actualEndDate;

    @Schema(description = "Total estimated budget allocated", example = "45000000.00")
    private BigDecimal estimatedBudget;

    @Schema(description = "Currency denomination", example = "INR")
    private String currency;

    @Schema(description = "Total built-up construction area in Sq.Ft", example = "350000.0")
    private Double totalBuiltUpAreaSqFt;

    @Schema(description = "Assigned Lead Project Manager user ID", example = "2")
    private Long projectManagerId;

    @Schema(description = "Assigned Lead Project Manager username", example = "karthik_pm")
    private String projectManagerName;

    @Schema(description = "Total number of physical sites / towers registered", example = "2")
    private int totalSitesCount;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

package com.example.BuildTwin._0.dto.baseline;

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
@Schema(description = "Response representation of a frozen Project Baseline")
public class ProjectBaselineResponse {

    @Schema(description = "Baseline ID", example = "1")
    private Long id;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Baseline Name", example = "Baseline 1.0 (Master Contract Schedule)")
    private String name;

    @Schema(description = "Version number", example = "1")
    private Integer version;

    @Schema(description = "Description or change control reason")
    private String description;

    @Schema(description = "Whether this baseline is currently the active comparison baseline", example = "true")
    private Boolean isActive;

    @Schema(description = "User who created this baseline", example = "admin")
    private String createdBy;

    @Schema(description = "User who approved this baseline")
    private String approvedBy;

    @Schema(description = "Approval timestamp")
    private LocalDateTime approvedAt;

    @Schema(description = "Total activities frozen in this baseline", example = "24")
    private Integer totalActivities;

    @Schema(description = "Total work packages captured", example = "4")
    private Integer totalWorkPackages;

    @Schema(description = "Total budget captured in baseline", example = "15000000.00")
    private BigDecimal totalBudgetAmount;

    @Schema(description = "Overall baseline planned start date", example = "2026-04-01")
    private LocalDate baselineStartDate;

    @Schema(description = "Overall baseline planned completion date", example = "2026-12-31")
    private LocalDate baselineEndDate;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
}

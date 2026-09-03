package com.example.BuildTwin._0.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new construction project")
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Schema(description = "Project display name", example = "Ashok Grandeur")
    private String name;

    @NotBlank(message = "Project code is required")
    @Schema(description = "Unique identifier code for project", example = "PADUR-AG-01")
    private String code;

    @Schema(description = "Detailed project description or scope", example = "18-storey residential development in Padur, Chennai")
    private String description;

    @Schema(description = "Client or developer name", example = "Ashok Builders & Developers")
    private String clientName;

    @Schema(description = "Project category: RESIDENTIAL, COMMERCIAL, INFRASTRUCTURE, INDUSTRIAL", example = "RESIDENTIAL")
    private String projectType;

    @Schema(description = "Project site location / city", example = "Padur, OMR, Chennai")
    private String location;

    @Schema(description = "Initial lifecycle status: PLANNED, ACTIVE, ON_HOLD", example = "ACTIVE")
    private String status;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned handover date", example = "2028-06-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual execution start date", example = "2026-09-05")
    private LocalDate actualStartDate;

    @Schema(description = "Total estimated budget allocated", example = "45000000.00")
    private BigDecimal estimatedBudget;

    @Schema(description = "Currency code (default INR)", example = "INR")
    private String currency;

    @Schema(description = "Total built-up construction area in Sq.Ft", example = "350000.0")
    private Double totalBuiltUpAreaSqFt;

    @Schema(description = "Optional Lead Project Manager user ID", example = "2")
    private Long projectManagerId;
}

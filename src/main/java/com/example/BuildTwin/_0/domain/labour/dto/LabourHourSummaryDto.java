package com.example.BuildTwin._0.domain.labour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary of labour hour tracking and headcount metrics")
public class LabourHourSummaryDto {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Contractor ID if filtered", example = "2")
    private Long contractorId;

    @Schema(description = "Total headcount deployed", example = "45")
    private Integer totalHeadcount;

    @Schema(description = "Total standard hours logged", example = "360.00")
    private BigDecimal totalStandardHours;

    @Schema(description = "Total overtime hours logged", example = "45.50")
    private BigDecimal totalOvertimeHours;

    @Schema(description = "Total combined labour hours logged", example = "405.50")
    private BigDecimal totalLabourHours;

    @Schema(description = "Total number of daily logs recorded", example = "12")
    private Integer totalRecordsLogged;
}

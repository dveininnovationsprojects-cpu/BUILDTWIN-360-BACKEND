package com.example.BuildTwin._0.domain.labour.dto;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for logging daily labour deployment")
public class LabourDailyRecordDto {

    @NotNull(message = "Record date is required")
    @Schema(description = "Date of labour deployment", example = "2026-08-26")
    private LocalDate recordDate;

    @NotNull(message = "Project ID is required")
    @Schema(description = "ID of project", example = "101")
    private Long projectId;

    @Schema(description = "ID of site location", example = "201")
    private Long siteId;

    @NotNull(message = "Contractor ID is required")
    @Schema(description = "ID of deployed contractor", example = "1")
    private Long contractorId;

    @NotNull(message = "Trade category is required")
    @Schema(description = "Trade discipline", example = "MASON")
    private TradeCategory tradeCategory;

    @NotNull(message = "Headcount is required")
    @Min(value = 0, message = "Headcount cannot be negative")
    @Schema(description = "Number of deployed workers", example = "12")
    private Integer headcount;

    @Schema(description = "Standard shift hours", example = "8.0")
    private BigDecimal standardHours;

    @Schema(description = "Overtime hours worked", example = "2.5")
    private BigDecimal overtimeHours;

    @Schema(description = "Remarks or site observations", example = "Slab shuttering works in progress")
    private String remarks;

    @Schema(description = "List of WBS activity task allocations for this labour record")
    private java.util.List<LabourAllocationDto> allocations;
}

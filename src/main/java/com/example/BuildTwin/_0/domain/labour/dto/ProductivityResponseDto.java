package com.example.BuildTwin._0.domain.labour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Productivity calculation result based on activity unit and labour hours")
public class ProductivityResponseDto {

    @Schema(description = "WBS Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "Activity Name", example = "RCC Slab Concreting")
    private String activityName;

    @Schema(description = "Configured activity unit of measurement", example = "m3")
    private String unit;

    @Schema(description = "Completed quantity executed", example = "150.00")
    private BigDecimal completedQuantity;

    @Schema(description = "Total labour hours allocated to this activity", example = "300.00")
    private BigDecimal totalLabourHours;

    @Schema(description = "Output rate per labour hour (Completed Qty / Labour Hours)", example = "0.50")
    private BigDecimal outputPerLabourHour;

    @Schema(description = "Man-hours required per unit of output (Labour Hours / Completed Qty)", example = "2.00")
    private BigDecimal manHoursPerUnit;

    @Schema(description = "Productivity rating status", example = "OPTIMAL")
    private String productivityStatus;
}

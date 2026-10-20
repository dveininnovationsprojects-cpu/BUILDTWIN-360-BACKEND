package com.example.BuildTwin._0.dto.baseline;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to log daily progress, work output, manpower and site hindrances for an activity")
public class LogDailyProgressRequest {

    @Schema(description = "Date of work progress", example = "2026-09-28")
    private LocalDate logDate;

    @NotNull(message = "Quantity completed today is required")
    @PositiveOrZero(message = "Quantity completed today must be 0 or positive")
    @Schema(description = "Physical quantity completed on this date in activity UOM", example = "45.0")
    private Double quantityCompletedToday;

    @Schema(description = "Number of manpower deployed (masons, helpers, bar benders)", example = "12")
    private Integer manpowerCount;

    @Schema(description = "Equipment or machinery utilized on site", example = "1 JCB Excavator, 2 Tipper Trucks")
    private String equipmentUsed;

    @Schema(description = "Site delays, material shortages or weather hindrances", example = "Rain halted concreting for 2 hours in afternoon.")
    private String siteHindranceNotes;

    @Schema(description = "Site weather condition", example = "RAINY")
    private String weatherCondition;
}

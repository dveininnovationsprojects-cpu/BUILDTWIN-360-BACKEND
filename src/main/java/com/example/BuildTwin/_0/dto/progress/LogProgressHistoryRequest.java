package com.example.BuildTwin._0.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to record a new site progress history journal entry for a WBS Activity")
public class LogProgressHistoryRequest {

    @Schema(description = "Date on which progress was executed (defaults to today if omitted)", example = "2026-09-28")
    private LocalDate logDate;

    @NotNull(message = "Quantity completed today is required")
    @PositiveOrZero(message = "Quantity completed today must be 0 or positive")
    @Schema(description = "Physical quantity completed on this date in activity UOM", example = "45.0")
    private Double quantityCompletedToday;

    @Schema(description = "Number of manpower deployed (masons, carpenters, bar benders, helpers)", example = "14")
    private Integer manpowerCount;

    @Schema(description = "Machinery and equipment deployed", example = "1 JCB Excavator, 1 Transit Mixer")
    private String equipmentUsed;

    @Schema(description = "Site delays, hindrances, or weather bottlenecks", example = "Delayed 1 hour due to brief morning drizzle")
    private String siteHindranceNotes;

    @Schema(description = "Weather condition recorded on site (CLEAR, RAINY, OVERCAST, WINDY, EXTREME_HEAT)", example = "OVERCAST")
    private String weatherCondition;

    @Schema(description = "Flag indicating if this progress entry encountered a critical blocker", example = "false")
    private Boolean isBlocker;

    @Schema(description = "Blocker category (MATERIAL_SHORTAGE, DESIGN_CLARIFICATION, PERMIT_DELAY, WEATHER, LABOUR_SHORTAGE)", example = "WEATHER")
    private String blockerCategory;

    @Schema(description = "Detailed description of blocker reason", example = "Rain flooded excavation trench; pumping was required")
    private String blockerReason;

    @Schema(description = "Operational site remarks and handover notes", example = "Shift target met; concrete poured for column footing C1-C4")
    private String remarks;
}

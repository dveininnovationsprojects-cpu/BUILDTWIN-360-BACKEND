package com.example.BuildTwin._0.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to update remarks, site hindrance, manpower, equipment or blocker details on an existing progress history record")
public class UpdateProgressHistoryRequest {

    @Schema(description = "Updated date of progress log", example = "2026-09-28")
    private LocalDate logDate;

    @PositiveOrZero(message = "Quantity completed today must be 0 or positive")
    @Schema(description = "Updated quantity completed today if adjusting measurement", example = "48.0")
    private Double quantityCompletedToday;

    @Schema(description = "Updated manpower count", example = "16")
    private Integer manpowerCount;

    @Schema(description = "Updated equipment used", example = "1 JCB Excavator, 2 Transit Mixers")
    private String equipmentUsed;

    @Schema(description = "Updated hindrance notes", example = "Resolved pump issue; resumed normal pouring")
    private String siteHindranceNotes;

    @Schema(description = "Updated weather condition", example = "CLEAR")
    private String weatherCondition;

    @Schema(description = "Updated blocker flag", example = "false")
    private Boolean isBlocker;

    @Schema(description = "Updated blocker category", example = "WEATHER")
    private String blockerCategory;

    @Schema(description = "Updated blocker reason", example = "Pumping complete and pit certified dry")
    private String blockerReason;

    @Schema(description = "Updated operational remarks", example = "Inspected by QA engineer and passed")
    private String remarks;
}

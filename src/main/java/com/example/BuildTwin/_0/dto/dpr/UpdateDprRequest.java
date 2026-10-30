package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.WeatherCondition;
import com.example.BuildTwin._0.model.WorkShift;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to update an existing draft Daily Progress Report (DPR) header")
public class UpdateDprRequest {

    @Schema(description = "Shift of work: GENERAL_SHIFT, DAY_SHIFT, NIGHT_SHIFT, DOUBLE_SHIFT", example = "DAY_SHIFT")
    private WorkShift shift;

    @Schema(description = "Site weather condition", example = "CLOUDY")
    private WeatherCondition weatherCondition;

    @Schema(description = "Site ambient temperature in Celsius", example = "30.0")
    private Double temperatureCelsius;

    @Schema(description = "Name or employee code of site engineer", example = "Engr. Rajesh Kumar")
    private String siteEngineer;

    @Schema(description = "General executive summary notes for the day", example = "Updated progress remarks after evening inspection")
    private String generalNotes;

    @Schema(description = "Number of safety incidents / near-misses occurred today", example = "0")
    private Integer safetyIncidentsCount;

    @Schema(description = "Safety notes and observations", example = "Toolbox talk completed, all PPE compliant")
    private String safetyNotes;

    @Schema(description = "Quality assurance notes", example = "RMC batch plant calibration checked")
    private String qualityObservations;

    @Schema(description = "Site hindrances notes", example = "Grid power fully restored")
    private String siteHindrances;
}

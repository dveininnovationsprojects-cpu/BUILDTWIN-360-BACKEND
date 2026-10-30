package com.example.BuildTwin._0.dto.dpr;

import com.example.BuildTwin._0.model.WeatherCondition;
import com.example.BuildTwin._0.model.WorkShift;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to create a new Daily Progress Report (DPR) header and initial line items")
public class CreateDprRequest {

    @NotNull(message = "Report date is required")
    @Schema(description = "Date of the site report", example = "2026-10-10")
    private LocalDate reportDate;

    @Schema(description = "Shift of work: GENERAL_SHIFT, DAY_SHIFT, NIGHT_SHIFT, DOUBLE_SHIFT", example = "DAY_SHIFT")
    @Builder.Default
    private WorkShift shift = WorkShift.DAY_SHIFT;

    @Schema(description = "Site weather condition: SUNNY, CLEAR, CLOUDY, RAINY, HEAVY_RAIN, EXTREME_HEAT, WINDY, STORMY", example = "SUNNY")
    @Builder.Default
    private WeatherCondition weatherCondition = WeatherCondition.SUNNY;

    @Schema(description = "Site ambient temperature in Celsius", example = "32.5")
    private Double temperatureCelsius;

    @Schema(description = "Name or employee code of site engineer drafting the report", example = "Engr. Rajesh Kumar")
    private String siteEngineer;

    @Schema(description = "General executive summary notes for the day", example = "Tower A 3rd floor slab casting in progress, shuttering team active on Block B")
    private String generalNotes;

    @Schema(description = "Number of safety incidents / near-misses occurred today", example = "0")
    @Builder.Default
    private Integer safetyIncidentsCount = 0;

    @Schema(description = "Safety toolbox talk topics, observations, or incident details", example = "Morning toolbox meeting conducted on working at heights and harness compliance. Zero incidents.")
    private String safetyNotes;

    @Schema(description = "Quality assurance notes, concrete slump tests, cube cast logs", example = "Slump test 120mm accepted; 6 concrete cubes cast for 7-day and 28-day compression testing.")
    private String qualityObservations;

    @Schema(description = "Site hindrances, unseasonal rain delays, power outages, material shortages", example = "30 mins power cut in morning resolved via standby diesel generator.")
    private String siteHindrances;

    @Valid
    @Builder.Default
    private List<DprActivityItemRequest> activityItems = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<DprLaborItemRequest> laborItems = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<DprEquipmentItemRequest> equipmentItems = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<DprMaterialItemRequest> materialItems = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<DprPhotoAttachmentRequest> photoAttachments = new ArrayList<>();
}

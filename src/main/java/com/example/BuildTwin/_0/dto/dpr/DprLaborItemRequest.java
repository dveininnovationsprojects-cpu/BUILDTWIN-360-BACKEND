package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to log site manpower headcount by trade in DPR")
public class DprLaborItemRequest {

    @NotBlank(message = "Trade category is required")
    @Schema(description = "Trade or labor category", example = "Masons / Bricklayers")
    private String tradeCategory;

    @NotNull(message = "Headcount is required")
    @Positive(message = "Headcount must be positive")
    @Schema(description = "Total workers present in this category", example = "12")
    private Integer headcount;

    @Schema(description = "Operating shift hours worked per laborer", example = "8.0")
    @Builder.Default
    private Double hoursWorked = 8.0;

    @Schema(description = "Name of subcontractor or labor agency", example = "Apex Construction Services")
    private String subcontractorName;

    @Schema(description = "Remarks or deployment notes", example = "Deployed for external block masonry work")
    private String remarks;
}

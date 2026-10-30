package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representation of a DPR labor headcount entry")
public class DprLaborItemResponse {

    @Schema(description = "Labor entry ID", example = "1")
    private Long id;

    @Schema(description = "Trade or labor category", example = "Masons / Bricklayers")
    private String tradeCategory;

    @Schema(description = "Headcount of workers", example = "12")
    private Integer headcount;

    @Schema(description = "Hours worked per person", example = "8.0")
    private Double hoursWorked;

    @Schema(description = "Subcontractor or agency name", example = "Apex Construction Services")
    private String subcontractorName;

    @Schema(description = "Remarks", example = "Deployed on external blockwork")
    private String remarks;
}

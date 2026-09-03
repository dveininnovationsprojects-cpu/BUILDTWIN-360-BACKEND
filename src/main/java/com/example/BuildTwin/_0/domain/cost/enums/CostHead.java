package com.example.BuildTwin._0.domain.cost.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard Construction Cost Head / Cost Breakdown Category")
public enum CostHead {
    LABOUR,
    MATERIAL,
    EQUIPMENT,
    SUBCONTRACTOR,
    OVERHEAD
}

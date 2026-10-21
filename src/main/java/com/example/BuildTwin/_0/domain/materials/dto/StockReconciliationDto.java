package com.example.BuildTwin._0.domain.materials.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for stock audit and physical inventory reconciliation")
public class StockReconciliationDto {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Target project ID", example = "101")
    private Long projectId;

    @Schema(description = "Site ID", example = "201")
    private Long siteId;

    @NotNull(message = "Material ID is required")
    @Schema(description = "Target material ID", example = "5")
    private Long materialId;

    @NotNull(message = "Physical stock quantity is required")
    @PositiveOrZero(message = "Physical stock quantity cannot be negative")
    @JsonAlias({"physicalStockCount", "physicalStock", "actualPhysicalStock", "physicalCount"})
    @Schema(description = "Actual physical stock counted on site", example = "10000.00")
    private BigDecimal physicalQty;

    @JsonAlias({"auditorName", "auditor"})
    @Schema(description = "User / Stock Auditor name performing reconciliation", example = "admin")
    private String auditedBy;

    @Schema(description = "Audit observations or variance remarks", example = "Physical audit found 10 broken bricks")
    private String remarks;
}


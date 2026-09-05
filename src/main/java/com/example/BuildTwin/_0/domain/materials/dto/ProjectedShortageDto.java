package com.example.BuildTwin._0.domain.materials.dto;

import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO representing projected inventory shortage analysis")
public class ProjectedShortageDto {

    @Schema(description = "Material ID", example = "5")
    private Long materialId;

    @Schema(description = "Material SKU code", example = "MAT-CEM-001")
    private String materialCode;

    @Schema(description = "Material name", example = "Portland Cement (PPC)")
    private String materialName;

    @Schema(description = "Material category", example = "CEMENT")
    private String category;

    @Schema(description = "Unit of measure")
    private MaterialUnit unit;

    @Schema(description = "Current available stock in store", example = "200.00")
    private BigDecimal currentStock;

    @Schema(description = "Minimum reorder threshold", example = "100.00")
    private BigDecimal reorderLevel;

    @Schema(description = "Total pending and approved requested quantity", example = "450.00")
    private BigDecimal totalRequestedQty;

    @Schema(description = "Projected shortage quantity (Requested - Current Stock)", example = "250.00")
    private BigDecimal projectedShortage;

    @Schema(description = "Shortage risk status (SHORTAGE, LOW_STOCK, OPTIMAL)", example = "SHORTAGE")
    private String status;
}

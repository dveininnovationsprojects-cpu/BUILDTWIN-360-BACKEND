package com.example.BuildTwin._0.domain.materials.dto;

import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for creating or updating a Material item in catalog")
public class MaterialRequestDto {

    @NotBlank(message = "Material code is required")
    @Schema(description = "Unique SKU material code", example = "MAT-CEM-53")
    private String materialCode;

    @NotBlank(message = "Material name is required")
    @Schema(description = "Name of material", example = "OPC 53 Grade Cement")
    private String name;

    @Schema(description = "Category of material", example = "CEMENT")
    private String category;

    @NotNull(message = "Material unit is required")
    @Schema(description = "Unit of measurement", example = "BAGS")
    private MaterialUnit unit;

    @NotNull(message = "Standard rate is required")
    @Positive(message = "Standard rate must be positive")
    @Schema(description = "Standard unit rate (INR)", example = "380.00")
    private BigDecimal standardRate;

    @Positive(message = "Reorder level must be positive")
    @Schema(description = "Minimum stock reorder threshold", example = "100.00")
    private BigDecimal reorderLevel;

    @Schema(description = "Detailed specifications", example = "UltraTech 53 Grade OPC Bags")
    private String description;
}

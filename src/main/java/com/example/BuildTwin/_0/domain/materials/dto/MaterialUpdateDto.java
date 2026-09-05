package com.example.BuildTwin._0.domain.materials.dto;

import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for updating material catalog entry")
public class MaterialUpdateDto {

    @Schema(description = "Material display name", example = "Portland Pozzolana Cement (PPC)")
    private String name;

    @Schema(description = "Material category", example = "CEMENT")
    private String category;

    @Schema(description = "Standard unit of measure")
    private MaterialUnit unit;

    @Positive(message = "Standard rate must be positive")
    @Schema(description = "Standard unit price / rate", example = "390.00")
    private BigDecimal standardRate;

    @Schema(description = "Minimum reorder threshold quantity", example = "100.00")
    private BigDecimal reorderLevel;

    @Schema(description = "Material detailed specification / description", example = "IS 1489 Part 1 compliant 50kg bag")
    private String description;
}

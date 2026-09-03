package com.example.BuildTwin._0.domain.labour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for allocating daily labour deployment to specific WBS activities")
public class LabourAllocationDto {

    @Schema(description = "Target WBS Activity ID", example = "301")
    private Long activityId;

    @NotNull(message = "Allocated hours is required")
    @Positive(message = "Allocated hours must be positive")
    @Schema(description = "Hours allocated to this activity", example = "8.00")
    private BigDecimal hoursAllocated;

    @Schema(description = "Description of work performed for this activity", example = "Column rebar tying and shuttering alignment")
    private String activityDescription;
}

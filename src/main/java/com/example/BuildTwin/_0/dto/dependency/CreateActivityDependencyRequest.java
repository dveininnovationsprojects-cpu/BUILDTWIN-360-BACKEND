package com.example.BuildTwin._0.dto.dependency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to establish a precedence dependency between two activities")
public class CreateActivityDependencyRequest {

    @NotNull(message = "Predecessor Activity ID is required")
    @Schema(description = "ID of the activity that must precede (Predecessor)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long predecessorId;

    @NotNull(message = "Successor Activity ID is required")
    @Schema(description = "ID of the activity that depends on the predecessor (Successor)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long successorId;

    @Schema(description = "Dependency type: FS (Finish-to-Start), SS (Start-to-Start), FF (Finish-to-Finish), SF (Start-to-Finish). Default is FS", example = "FS")
    private String dependencyType;

    @Schema(description = "Lead / Lag time in calendar days (positive = lag, negative = lead). Default is 0", example = "2")
    private Integer lagDays;

    @Schema(description = "Optional engineering remarks or reason for dependency", example = "Curing lag required before stripping formwork")
    private String remarks;
}

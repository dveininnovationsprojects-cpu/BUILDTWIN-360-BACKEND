package com.example.BuildTwin._0.dto.dependency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to update an existing activity dependency relationship")
public class UpdateActivityDependencyRequest {

    @Schema(description = "Updated dependency type: FS, SS, FF, SF", example = "SS")
    private String dependencyType;

    @Schema(description = "Updated lead / lag time in days", example = "3")
    private Integer lagDays;

    @Schema(description = "Updated engineering remarks", example = "Modified lag to align with revised curing protocol")
    private String remarks;
}

package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registered Modules and Integration Readiness List")
public class ProjectModulesListResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-001")
    private String projectCode;

    @Schema(description = "Total registered modules", example = "8")
    private Integer totalModules;

    @Schema(description = "Total active/healthy modules", example = "8")
    private Integer activeModules;

    @Schema(description = "List of module descriptors")
    private List<ModuleDescriptorDto> modules;
}

package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual Module Metadata & Readiness Descriptor")
public class ModuleDescriptorDto {

    @Schema(description = "Unique module identifier key", example = "HIERARCHY")
    private String moduleKey;

    @Schema(description = "Human-readable module name", example = "Spatial Hierarchy & Location Structure")
    private String moduleName;

    @Schema(description = "API Base Path for the module", example = "/api/v1/hierarchy")
    private String basePath;

    @Schema(description = "Module semantic version", example = "1.0.0")
    private String version;

    @Schema(description = "Module operational status (ACTIVE, SYNCHRONIZED, IDLE)", example = "SYNCHRONIZED")
    private String status;

    @Schema(description = "Live entity count tracked by the module", example = "28")
    private Long entityCount;

    @Schema(description = "Health condition (HEALTHY, DEGRADED, UNCONFIGURED)", example = "HEALTHY")
    private String health;
}

package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System-Wide Module Integration Health Response")
public class ModuleHealthResponse {

    @Schema(description = "Overall integration operational status (UP, DEGRADED, DOWN)", example = "UP")
    private String status;

    @Schema(description = "Platform version", example = "1.0.0-ENTERPRISE")
    private String version;

    @Schema(description = "Server check timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Subsystem health status mapping")
    private Map<String, String> subsystemStatus;
}

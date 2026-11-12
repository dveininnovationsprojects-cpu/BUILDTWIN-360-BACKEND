package com.example.BuildTwin._0.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "System Audit Log Analytics & Health Summary Response")
public class AuditStatsResponse {

    @Schema(description = "Total number of audit logs recorded in system", example = "1250")
    private Long totalLogs;

    @Schema(description = "Total number of audit events recorded today", example = "45")
    private Long todayLogs;

    @Schema(description = "Total distinct users who performed auditable actions", example = "18")
    private Long uniqueUsersCount;

    @Schema(description = "Top performed actions and their frequency breakdown")
    private Map<String, Long> topActions;

    @Schema(description = "Top target entity types and their audit volume")
    private Map<String, Long> topEntities;

    @Schema(description = "Recent audit log entries")
    private List<AuditLogResponse> recentActivities;

    @Schema(description = "Audit subsystem operational health", example = "HEALTHY")
    private String systemHealth;

    @Schema(description = "Snapshot generation timestamp")
    private LocalDateTime generatedAt;
}

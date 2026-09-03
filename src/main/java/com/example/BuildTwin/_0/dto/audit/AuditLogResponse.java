package com.example.BuildTwin._0.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "System Audit Log Entry")
public class AuditLogResponse {

    @Schema(description = "Audit Log ID", example = "1")
    private Long id;

    @Schema(description = "User who performed the action", example = "admin")
    private String username;

    @Schema(description = "Action performed", example = "CREATE_USER")
    private String action;

    @Schema(description = "Target Entity Type", example = "USER")
    private String entityType;

    @Schema(description = "Target Entity ID", example = "5")
    private String entityId;

    @Schema(description = "Action description/details", example = "Created user with username: suresh_engineer")
    private String details;

    @Schema(description = "Client IP Address", example = "127.0.0.1")
    private String ipAddress;

    @Schema(description = "Audit event timestamp")
    private LocalDateTime createdAt;
}

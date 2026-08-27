package com.example.BuildTwin._0.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile summary")
public class UserSummaryDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "rajesh_kumar")
    private String username;

    @Schema(description = "Email address", example = "rajesh.kumar@ashokbuilders.com")
    private String email;

    @Schema(description = "Account status", example = "ACTIVE")
    private String status;

    @Schema(description = "Assigned system roles", example = "[\"ROLE_ADMIN\", \"ROLE_PROJECT_MANAGER\"]")
    private Set<String> roles;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;
}

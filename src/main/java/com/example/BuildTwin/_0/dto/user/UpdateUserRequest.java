package com.example.BuildTwin._0.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update User Profile Payload")
public class UpdateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Updated email address", example = "karthik.new@ashokbuilders.com")
    private String email;

    @Schema(description = "Updated account status (ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE")
    private String status;

    @Schema(description = "Updated set of system roles", example = "[\"ROLE_PROJECT_MANAGER\", \"ROLE_SITE_ENGINEER\"]")
    private Set<String> roles;
}

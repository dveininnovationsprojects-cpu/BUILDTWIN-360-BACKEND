package com.example.BuildTwin._0.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin Create User Request Payload")
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Unique username for the user", example = "karthik_pm")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "User official email address", example = "karthik.pm@ashokbuilders.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    @Schema(description = "Initial password for the user", example = "Karthik@123")
    private String password;

    @Schema(description = "Account status (e.g. ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE")
    private String status;

    @Schema(description = "Set of system roles to assign (e.g., [\"ROLE_PROJECT_MANAGER\"])", example = "[\"ROLE_PROJECT_MANAGER\"]")
    private Set<String> roles;
}

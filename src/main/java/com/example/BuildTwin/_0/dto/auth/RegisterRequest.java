package com.example.BuildTwin._0.dto.auth;

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
@Schema(description = "User registration payload")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Unique username", example = "rajesh_kumar")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User email address", example = "rajesh.kumar@ashokbuilders.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    @Schema(description = "User password", example = "SecurePass@123")
    private String password;

    @Schema(description = "Optional role names (e.g. ROLE_PROJECT_MANAGER, ROLE_SITE_ENGINEER)", example = "[\"ROLE_PROJECT_MANAGER\"]")
    private Set<String> roles;
}

package com.example.BuildTwin._0.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin Reset Password Request")
public class ResetPasswordRequest {

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be at least 6 characters")
    @Schema(description = "New password for the user", example = "ResetPass@123")
    private String newPassword;
}

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
@Schema(description = "Self-Service Change Password Request")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "Existing current password", example = "OldPass@123")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be at least 6 characters")
    @Schema(description = "New password", example = "NewSecurePass@2026")
    private String newPassword;
}

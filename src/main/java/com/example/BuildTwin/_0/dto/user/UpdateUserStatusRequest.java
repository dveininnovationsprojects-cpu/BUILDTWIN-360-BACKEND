package com.example.BuildTwin._0.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update User Account Status Request")
public class UpdateUserStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)$", message = "Status must be ACTIVE, INACTIVE, or SUSPENDED")
    @Schema(description = "New account status (ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE")
    private String status;
}

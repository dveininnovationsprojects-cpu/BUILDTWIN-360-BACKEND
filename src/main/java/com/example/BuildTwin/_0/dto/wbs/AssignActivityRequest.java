package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for assigning a contractor and site engineer to an activity")
public class AssignActivityRequest {

    @NotBlank(message = "Assigned contractor is required")
    @Schema(description = "Contractor or vendor agency name", example = "L&T Construction (Civil Div)")
    private String assignedContractor;

    @Schema(description = "User ID of the site incharge engineer / supervisor", example = "1")
    private Long inchargeUserId;
}

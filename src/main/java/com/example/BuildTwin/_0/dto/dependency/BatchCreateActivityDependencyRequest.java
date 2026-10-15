package com.example.BuildTwin._0.dto.dependency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to establish multiple activity dependencies simultaneously")
public class BatchCreateActivityDependencyRequest {

    @NotEmpty(message = "Dependencies list cannot be empty")
    @Valid
    @Schema(description = "List of activity dependency links to create", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CreateActivityDependencyRequest> dependencies;
}

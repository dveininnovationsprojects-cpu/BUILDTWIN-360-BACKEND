package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to re-parent a WBS Activity to a new parent or move to root")
public class ReparentWbsActivityRequest {

    @Schema(description = "New parent WBS Activity ID. Pass null to convert to a root activity directly under the work package.", example = "1")
    private Long newParentId;

    @Schema(description = "Optional sequence order under the new parent or root", example = "2")
    private Integer newSequenceOrder;
}

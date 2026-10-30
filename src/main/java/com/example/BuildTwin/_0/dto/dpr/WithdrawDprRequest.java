package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional request payload when withdrawing a submitted DPR back to DRAFT")
public class WithdrawDprRequest {

    @Schema(description = "Optional reason for withdrawing the report back to DRAFT",
            example = "Need to append additional material delivery challans before final sign-off.")
    private String withdrawalReason;
}

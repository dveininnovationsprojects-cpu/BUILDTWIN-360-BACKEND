package com.example.BuildTwin._0.domain.labour.dto;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Trade category information and specification")
public class TradeDto {
    @Schema(description = "Trade category enum value", example = "ELECTRICIAN")
    private TradeCategory trade;

    @Schema(description = "Display name of trade category", example = "Electrical Works & Wiring")
    private String displayName;

    @Schema(description = "Description of trade responsibilities", example = "Conduit laying, wiring, DB dressing, fixture installation")
    private String description;
}

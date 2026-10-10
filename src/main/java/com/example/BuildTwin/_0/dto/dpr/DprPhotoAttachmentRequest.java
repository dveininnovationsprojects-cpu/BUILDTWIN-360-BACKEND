package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to attach site visual evidence and geotagged photos to DPR")
public class DprPhotoAttachmentRequest {

    @NotBlank(message = "Photo URL is required")
    @Schema(description = "S3 / CDN / storage URL of the photo", example = "https://storage.buildtwin360.com/sites/prj1/photos/20261010_pour.jpg")
    private String photoUrl;

    @Schema(description = "Detailed caption explaining the photo", example = "Slab reinforcement inspection before pour")
    private String caption;

    @Schema(description = "Category of photo: PROGRESS, SAFETY, QUALITY, HINDRANCE", example = "PROGRESS")
    @Builder.Default
    private String category = "PROGRESS";

    @Schema(description = "Timestamp when photo was captured", example = "2026-10-10T11:30:00")
    private LocalDateTime takenAt;
}

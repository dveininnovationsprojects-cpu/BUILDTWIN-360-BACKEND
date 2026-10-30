package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representation of a DPR photo attachment")
public class DprPhotoAttachmentResponse {

    @Schema(description = "Attachment ID", example = "1")
    private Long id;

    @Schema(description = "Photo URL", example = "https://storage.buildtwin360.com/sites/prj1/photos/20261010_pour.jpg")
    private String photoUrl;

    @Schema(description = "Photo caption", example = "Slab reinforcement inspection before pour")
    private String caption;

    @Schema(description = "Photo category", example = "PROGRESS")
    private String category;

    @Schema(description = "Timestamp when photo was taken", example = "2026-10-10T11:30:00")
    private LocalDateTime takenAt;

    @Schema(description = "Timestamp when photo was uploaded", example = "2026-10-10T12:00:00")
    private LocalDateTime createdAt;
}

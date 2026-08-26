package com.example.BuildTwin._0.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized Error Response Payload for BuildTwin 360")
public class ErrorResponse {

    @Schema(description = "HTTP Status Code", example = "404")
    private int status;

    @Schema(description = "HTTP Status Name", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Human-readable error summary", example = "Resource not found")
    private String message;

    @Schema(description = "Requested API Path", example = "/api/v1/projects/999")
    private String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Schema(description = "Timestamp when error occurred", example = "2026-08-26T12:50:00.000")
    private LocalDateTime timestamp;

    @Schema(description = "Detailed list of field validation errors (for 400 Bad Request)")
    private Map<String, String> validationErrors;

    @Schema(description = "Additional context or sub-error messages")
    private List<String> details;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

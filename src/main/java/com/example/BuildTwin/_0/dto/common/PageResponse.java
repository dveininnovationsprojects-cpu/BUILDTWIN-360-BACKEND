package com.example.BuildTwin._0.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generic Paginated Response Wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items in the current page")
    private List<T> content;

    @Schema(description = "Current page index (0-based)", example = "0")
    private int pageNumber;

    @Schema(description = "Page size (number of records per page)", example = "10")
    private int pageSize;

    @Schema(description = "Total number of elements across all pages", example = "42")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean isLast;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean isFirst;
}

package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.floor.CreateFloorRequest;
import com.example.BuildTwin._0.dto.floor.FloorResponse;
import com.example.BuildTwin._0.dto.floor.UpdateFloorRequest;
import com.example.BuildTwin._0.dto.floor.UpdateFloorStatusRequest;
import com.example.BuildTwin._0.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "2. Project & Site Master Setup", description = "Floor / Level Management, Basements, Stilts & High-Rise Slabs")
@SecurityRequirement(name = "BearerAuth")
public class FloorController {

    private final FloorService floorService;

    @PostMapping("/api/v1/buildings/{buildingId}/floors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create floor / slab under building",
            description = "Creates a floor or level record under a building (e.g., Level -1 for Basement, Level 0 for Stilt, Level 1..18)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Floor created successfully",
                    content = @Content(schema = @Schema(implementation = FloorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Building not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Floor number already exists in this building")
    })
    public ResponseEntity<ApiResponse<FloorResponse>> createFloor(
            @PathVariable Long buildingId,
            @Valid @RequestBody CreateFloorRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        FloorResponse created = floorService.createFloor(buildingId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Floor created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/buildings/{buildingId}/floors")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all floors of a building",
            description = "Retrieves all levels/floors belonging to a building, ordered by floor number ascending."
    )
    public ResponseEntity<ApiResponse<List<FloorResponse>>> getFloorsByBuilding(@PathVariable Long buildingId) {
        List<FloorResponse> floors = floorService.getFloorsByBuildingId(buildingId);
        return ResponseEntity.ok(ApiResponse.success(floors, "Floors retrieved successfully"));
    }

    @GetMapping("/api/v1/buildings/{buildingId}/floors/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List floors of a building with pagination",
            description = "Paginated list of floors under a building."
    )
    public ResponseEntity<ApiResponse<PageResponse<FloorResponse>>> getFloorsByBuildingPaginated(
            @PathVariable Long buildingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<FloorResponse> floors = floorService.getFloorsByBuildingIdPaginated(buildingId, page, size);
        return ResponseEntity.ok(ApiResponse.success(floors, "Paginated floors retrieved successfully"));
    }

    @GetMapping("/api/v1/floors/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get floor details by ID",
            description = "Retrieves floor information by its ID."
    )
    public ResponseEntity<ApiResponse<FloorResponse>> getFloorById(@PathVariable Long id) {
        FloorResponse floor = floorService.getFloorById(id);
        return ResponseEntity.ok(ApiResponse.success(floor, "Floor retrieved successfully"));
    }

    @PutMapping("/api/v1/floors/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Update floor details",
            description = "Modifies floor name, number, type, or area."
    )
    public ResponseEntity<ApiResponse<FloorResponse>> updateFloor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFloorRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        FloorResponse updated = floorService.updateFloor(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Floor updated successfully"));
    }

    @PatchMapping("/api/v1/floors/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update floor status",
            description = "Updates floor construction status (PLANNED, IN_PROGRESS, COMPLETED)."
    )
    public ResponseEntity<ApiResponse<FloorResponse>> updateFloorStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFloorStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        FloorResponse updated = floorService.updateFloorStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Floor status updated successfully"));
    }

    @DeleteMapping("/api/v1/floors/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete floor",
            description = "Deletes floor and cascades deletion to all zones on this floor."
    )
    public ResponseEntity<ApiResponse<Void>> deleteFloor(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        floorService.deleteFloor(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Floor deleted successfully"));
    }
}

package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.zone.CreateZoneRequest;
import com.example.BuildTwin._0.dto.zone.UpdateZoneRequest;
import com.example.BuildTwin._0.dto.zone.UpdateZoneStatusRequest;
import com.example.BuildTwin._0.dto.zone.ZoneResponse;
import com.example.BuildTwin._0.service.ZoneService;
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
@Tag(name = "2. Project & Site Master Setup", description = "Zone / Unit Management, Flats, Common Areas & Work Fronts")
@SecurityRequirement(name = "BearerAuth")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping("/api/v1/floors/{floorId}/zones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Create zone / unit under floor",
            description = "Creates a zone, flat, unit, room, or work front on a specific floor."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Zone created successfully",
                    content = @Content(schema = @Schema(implementation = ZoneResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Floor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Zone code already exists on this floor")
    })
    public ResponseEntity<ApiResponse<ZoneResponse>> createZone(
            @PathVariable Long floorId,
            @Valid @RequestBody CreateZoneRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ZoneResponse created = zoneService.createZone(floorId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Zone created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/floors/{floorId}/zones")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List all zones of a floor",
            description = "Retrieves all zones, units, or flats configured on a floor."
    )
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> getZonesByFloor(@PathVariable Long floorId) {
        List<ZoneResponse> zones = zoneService.getZonesByFloorId(floorId);
        return ResponseEntity.ok(ApiResponse.success(zones, "Zones retrieved successfully"));
    }

    @GetMapping("/api/v1/floors/{floorId}/zones/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List zones of a floor with pagination",
            description = "Paginated list of zones under a floor."
    )
    public ResponseEntity<ApiResponse<PageResponse<ZoneResponse>>> getZonesByFloorPaginated(
            @PathVariable Long floorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ZoneResponse> zones = zoneService.getZonesByFloorIdPaginated(floorId, page, size);
        return ResponseEntity.ok(ApiResponse.success(zones, "Paginated zones retrieved successfully"));
    }

    @GetMapping("/api/v1/zones/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get zone details by ID",
            description = "Retrieves information of a specific zone/unit by its ID."
    )
    public ResponseEntity<ApiResponse<ZoneResponse>> getZoneById(@PathVariable Long id) {
        ZoneResponse zone = zoneService.getZoneById(id);
        return ResponseEntity.ok(ApiResponse.success(zone, "Zone retrieved successfully"));
    }

    @PutMapping("/api/v1/zones/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER')")
    @Operation(
            summary = "Update zone details",
            description = "Updates zone name, code, type, or area in sq.ft."
    )
    public ResponseEntity<ApiResponse<ZoneResponse>> updateZone(
            @PathVariable Long id,
            @Valid @RequestBody UpdateZoneRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ZoneResponse updated = zoneService.updateZone(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Zone updated successfully"));
    }

    @PatchMapping("/api/v1/zones/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR')")
    @Operation(
            summary = "Update zone status",
            description = "Updates zone construction status (PLANNED, IN_PROGRESS, COMPLETED)."
    )
    public ResponseEntity<ApiResponse<ZoneResponse>> updateZoneStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateZoneStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ZoneResponse updated = zoneService.updateZoneStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Zone status updated successfully"));
    }

    @DeleteMapping("/api/v1/zones/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete zone",
            description = "Permanently removes a zone/unit."
    )
    public ResponseEntity<ApiResponse<Void>> deleteZone(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        zoneService.deleteZone(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Zone deleted successfully"));
    }
}

package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.floor.CreateFloorRequest;
import com.example.BuildTwin._0.dto.floor.FloorResponse;
import com.example.BuildTwin._0.dto.floor.UpdateFloorRequest;
import com.example.BuildTwin._0.dto.floor.UpdateFloorStatusRequest;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Building;
import com.example.BuildTwin._0.model.Floor;
import com.example.BuildTwin._0.model.Site;
import com.example.BuildTwin._0.repository.BuildingRepository;
import com.example.BuildTwin._0.repository.FloorRepository;
import com.example.BuildTwin._0.repository.ZoneRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.FloorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloorServiceImpl implements FloorService {

    private static final Set<String> ALLOWED_FLOOR_STATUSES = Set.of(
            "PLANNED", "IN_PROGRESS", "COMPLETED"
    );

    private static final Set<String> ALLOWED_FLOOR_TYPES = Set.of(
            "BASEMENT", "STILT", "PODIUM", "TYPICAL", "REFUGE", "TERRACE", "GROUND"
    );

    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;
    private final ZoneRepository zoneRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public FloorResponse createFloor(Long buildingId, CreateFloorRequest request, String performedBy) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", buildingId));

        if (floorRepository.existsByBuildingIdAndFloorNumber(buildingId, request.getFloorNumber())) {
            throw new DuplicateResourceException("Floor", "floorNumber", request.getFloorNumber() + " in Building " + building.getName());
        }

        String status = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "PLANNED";
        String floorType = request.getFloorType() != null ? validateAndNormalizeType(request.getFloorType()) : "TYPICAL";

        Floor floor = Floor.builder()
                .building(building)
                .floorNumber(request.getFloorNumber())
                .floorName(request.getFloorName().trim())
                .floorType(floorType)
                .builtUpAreaSqFt(request.getBuiltUpAreaSqFt())
                .status(status)
                .build();

        Floor saved = floorRepository.save(floor);

        auditService.logAction(
                performedBy,
                "CREATE_FLOOR",
                "FLOOR",
                String.valueOf(saved.getId()),
                "Created floor: " + saved.getFloorName() + " (Level " + saved.getFloorNumber() + ") under building: " + building.getName(),
                null
        );

        return mapToFloorResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FloorResponse> getFloorsByBuildingId(Long buildingId) {
        if (!buildingRepository.existsById(buildingId)) {
            throw new ResourceNotFoundException("Building", "id", buildingId);
        }
        return floorRepository.findByBuildingIdOrderByFloorNumberAsc(buildingId).stream()
                .map(this::mapToFloorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FloorResponse> getFloorsByBuildingIdPaginated(Long buildingId, int page, int size) {
        if (!buildingRepository.existsById(buildingId)) {
            throw new ResourceNotFoundException("Building", "id", buildingId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("floorNumber").ascending());
        Page<Floor> floorPage = floorRepository.findByBuildingId(buildingId, pageable);

        List<FloorResponse> content = floorPage.getContent().stream()
                .map(this::mapToFloorResponse)
                .collect(Collectors.toList());

        return PageResponse.<FloorResponse>builder()
                .content(content)
                .pageNumber(floorPage.getNumber())
                .pageSize(floorPage.getSize())
                .totalElements(floorPage.getTotalElements())
                .totalPages(floorPage.getTotalPages())
                .isFirst(floorPage.isFirst())
                .isLast(floorPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FloorResponse getFloorById(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));
        return mapToFloorResponse(floor);
    }

    @Override
    @Transactional
    public FloorResponse updateFloor(Long id, UpdateFloorRequest request, String performedBy) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));

        if (!floor.getFloorNumber().equals(request.getFloorNumber()) &&
                floorRepository.existsByBuildingIdAndFloorNumberAndIdNot(floor.getBuilding().getId(), request.getFloorNumber(), id)) {
            throw new DuplicateResourceException("Floor", "floorNumber", request.getFloorNumber() + " in Building " + floor.getBuilding().getName());
        }

        floor.setFloorNumber(request.getFloorNumber());
        floor.setFloorName(request.getFloorName().trim());
        if (request.getFloorType() != null) floor.setFloorType(validateAndNormalizeType(request.getFloorType()));
        floor.setBuiltUpAreaSqFt(request.getBuiltUpAreaSqFt());
        if (request.getStatus() != null) floor.setStatus(validateAndNormalizeStatus(request.getStatus()));

        Floor updated = floorRepository.save(floor);

        auditService.logAction(
                performedBy,
                "UPDATE_FLOOR",
                "FLOOR",
                String.valueOf(updated.getId()),
                "Updated floor: " + updated.getFloorName() + " (Level " + updated.getFloorNumber() + ")",
                null
        );

        return mapToFloorResponse(updated);
    }

    @Override
    @Transactional
    public FloorResponse updateFloorStatus(Long id, UpdateFloorStatusRequest request, String performedBy) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));

        String oldStatus = floor.getStatus();
        String validatedStatus = validateAndNormalizeStatus(request.getStatus());

        floor.setStatus(validatedStatus);
        Floor updated = floorRepository.save(floor);

        auditService.logAction(
                performedBy,
                "UPDATE_FLOOR_STATUS",
                "FLOOR",
                String.valueOf(id),
                "Changed status of floor '" + floor.getFloorName() + "' from " + oldStatus + " to " + updated.getStatus(),
                null
        );

        return mapToFloorResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFloor(Long id, String performedBy) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", id));

        String floorName = floor.getFloorName();
        floorRepository.delete(floor);

        auditService.logAction(
                performedBy,
                "DELETE_FLOOR",
                "FLOOR",
                String.valueOf(id),
                "Deleted floor: " + floorName,
                null
        );
    }

    private String validateAndNormalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status cannot be blank. Allowed statuses: " + ALLOWED_FLOOR_STATUSES);
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_FLOOR_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid floor status: '" + status + "'. Allowed statuses are: " + ALLOWED_FLOOR_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new BadRequestException("Floor type cannot be blank. Allowed types: " + ALLOWED_FLOOR_TYPES);
        }
        String normalized = type.trim().toUpperCase();
        if (!ALLOWED_FLOOR_TYPES.contains(normalized)) {
            throw new BadRequestException("Invalid floor type: '" + type + "'. Allowed types are: " + ALLOWED_FLOOR_TYPES);
        }
        return normalized;
    }

    private FloorResponse mapToFloorResponse(Floor floor) {
        int zonesCount = (int) zoneRepository.countByFloorId(floor.getId());
        Building building = floor.getBuilding();
        Site site = building != null ? building.getSite() : null;

        return FloorResponse.builder()
                .id(floor.getId())
                .buildingId(building != null ? building.getId() : null)
                .buildingName(building != null ? building.getName() : null)
                .siteId(site != null ? site.getId() : null)
                .projectId(site != null && site.getProject() != null ? site.getProject().getId() : null)
                .floorNumber(floor.getFloorNumber())
                .floorName(floor.getFloorName())
                .floorType(floor.getFloorType())
                .builtUpAreaSqFt(floor.getBuiltUpAreaSqFt())
                .status(floor.getStatus())
                .zonesCount(zonesCount)
                .createdAt(floor.getCreatedAt())
                .updatedAt(floor.getUpdatedAt())
                .build();
    }
}

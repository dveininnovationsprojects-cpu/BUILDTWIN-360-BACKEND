package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.zone.CreateZoneRequest;
import com.example.BuildTwin._0.dto.zone.UpdateZoneRequest;
import com.example.BuildTwin._0.dto.zone.UpdateZoneStatusRequest;
import com.example.BuildTwin._0.dto.zone.ZoneResponse;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Building;
import com.example.BuildTwin._0.model.Floor;
import com.example.BuildTwin._0.model.Site;
import com.example.BuildTwin._0.model.Zone;
import com.example.BuildTwin._0.repository.FloorRepository;
import com.example.BuildTwin._0.repository.ZoneRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.ZoneService;
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
public class ZoneServiceImpl implements ZoneService {

    private static final Set<String> ALLOWED_ZONE_STATUSES = Set.of(
            "PLANNED", "IN_PROGRESS", "COMPLETED"
    );

    private static final Set<String> ALLOWED_ZONE_TYPES = Set.of(
            "RESIDENTIAL_UNIT", "COMMON_AREA", "CORRIDOR", "ELECTRICAL_ROOM",
            "DUCT_SHAFT", "STAIRCASE", "LIFT_LOBBY", "BALCONY_AREA", "REFUGE_ZONE"
    );

    private final ZoneRepository zoneRepository;
    private final FloorRepository floorRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ZoneResponse createZone(Long floorId, CreateZoneRequest request, String performedBy) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", floorId));

        String code = request.getCode().trim().toUpperCase();
        if (zoneRepository.existsByFloorIdAndCode(floorId, code)) {
            throw new DuplicateResourceException("Zone", "code", code + " on Floor " + floor.getFloorName());
        }

        String status = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "PLANNED";
        String zoneType = request.getZoneType() != null ? validateAndNormalizeType(request.getZoneType()) : "RESIDENTIAL_UNIT";

        Zone zone = Zone.builder()
                .floor(floor)
                .code(code)
                .name(request.getName().trim())
                .zoneType(zoneType)
                .areaSqFt(request.getAreaSqFt())
                .status(status)
                .build();

        Zone saved = zoneRepository.save(zone);

        auditService.logAction(
                performedBy,
                "CREATE_ZONE",
                "ZONE",
                String.valueOf(saved.getId()),
                "Created zone: " + saved.getName() + " (" + saved.getCode() + ") on floor: " + floor.getFloorName(),
                null
        );

        return mapToZoneResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponse> getZonesByFloorId(Long floorId) {
        if (!floorRepository.existsById(floorId)) {
            throw new ResourceNotFoundException("Floor", "id", floorId);
        }
        return zoneRepository.findByFloorId(floorId).stream()
                .map(this::mapToZoneResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ZoneResponse> getZonesByFloorIdPaginated(Long floorId, int page, int size) {
        if (!floorRepository.existsById(floorId)) {
            throw new ResourceNotFoundException("Floor", "id", floorId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Zone> zonePage = zoneRepository.findByFloorId(floorId, pageable);

        List<ZoneResponse> content = zonePage.getContent().stream()
                .map(this::mapToZoneResponse)
                .collect(Collectors.toList());

        return PageResponse.<ZoneResponse>builder()
                .content(content)
                .pageNumber(zonePage.getNumber())
                .pageSize(zonePage.getSize())
                .totalElements(zonePage.getTotalElements())
                .totalPages(zonePage.getTotalPages())
                .isFirst(zonePage.isFirst())
                .isLast(zonePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneResponse getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));
        return mapToZoneResponse(zone);
    }

    @Override
    @Transactional
    public ZoneResponse updateZone(Long id, UpdateZoneRequest request, String performedBy) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!zone.getCode().equalsIgnoreCase(newCode) &&
                zoneRepository.existsByFloorIdAndCodeAndIdNot(zone.getFloor().getId(), newCode, id)) {
            throw new DuplicateResourceException("Zone", "code", newCode + " on Floor " + zone.getFloor().getFloorName());
        }

        zone.setCode(newCode);
        zone.setName(request.getName().trim());
        if (request.getZoneType() != null) zone.setZoneType(validateAndNormalizeType(request.getZoneType()));
        zone.setAreaSqFt(request.getAreaSqFt());
        if (request.getStatus() != null) zone.setStatus(validateAndNormalizeStatus(request.getStatus()));

        Zone updated = zoneRepository.save(zone);

        auditService.logAction(
                performedBy,
                "UPDATE_ZONE",
                "ZONE",
                String.valueOf(updated.getId()),
                "Updated zone: " + updated.getName() + " (" + updated.getCode() + ")",
                null
        );

        return mapToZoneResponse(updated);
    }

    @Override
    @Transactional
    public ZoneResponse updateZoneStatus(Long id, UpdateZoneStatusRequest request, String performedBy) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        String oldStatus = zone.getStatus();
        String validatedStatus = validateAndNormalizeStatus(request.getStatus());

        zone.setStatus(validatedStatus);
        Zone updated = zoneRepository.save(zone);

        auditService.logAction(
                performedBy,
                "UPDATE_ZONE_STATUS",
                "ZONE",
                String.valueOf(id),
                "Changed status of zone '" + zone.getName() + "' from " + oldStatus + " to " + updated.getStatus(),
                null
        );

        return mapToZoneResponse(updated);
    }

    @Override
    @Transactional
    public void deleteZone(Long id, String performedBy) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));

        String name = zone.getName();
        zoneRepository.delete(zone);

        auditService.logAction(
                performedBy,
                "DELETE_ZONE",
                "ZONE",
                String.valueOf(id),
                "Deleted zone: " + name,
                null
        );
    }

    private String validateAndNormalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status cannot be blank. Allowed statuses: " + ALLOWED_ZONE_STATUSES);
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_ZONE_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid zone status: '" + status + "'. Allowed statuses are: " + ALLOWED_ZONE_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new BadRequestException("Zone type cannot be blank. Allowed types: " + ALLOWED_ZONE_TYPES);
        }
        String normalized = type.trim().toUpperCase();
        if (!ALLOWED_ZONE_TYPES.contains(normalized)) {
            throw new BadRequestException("Invalid zone type: '" + type + "'. Allowed types are: " + ALLOWED_ZONE_TYPES);
        }
        return normalized;
    }

    private ZoneResponse mapToZoneResponse(Zone zone) {
        Floor floor = zone.getFloor();
        Building building = floor != null ? floor.getBuilding() : null;
        Site site = building != null ? building.getSite() : null;

        return ZoneResponse.builder()
                .id(zone.getId())
                .floorId(floor != null ? floor.getId() : null)
                .floorName(floor != null ? floor.getFloorName() : null)
                .buildingId(building != null ? building.getId() : null)
                .siteId(site != null ? site.getId() : null)
                .projectId(site != null && site.getProject() != null ? site.getProject().getId() : null)
                .code(zone.getCode())
                .name(zone.getName())
                .zoneType(zone.getZoneType())
                .areaSqFt(zone.getAreaSqFt())
                .status(zone.getStatus())
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .build();
    }
}

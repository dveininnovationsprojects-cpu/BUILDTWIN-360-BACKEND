package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.building.BuildingResponse;
import com.example.BuildTwin._0.dto.building.CreateBuildingRequest;
import com.example.BuildTwin._0.dto.building.UpdateBuildingRequest;
import com.example.BuildTwin._0.dto.building.UpdateBuildingStatusRequest;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Building;
import com.example.BuildTwin._0.model.Site;
import com.example.BuildTwin._0.repository.BuildingRepository;
import com.example.BuildTwin._0.repository.FloorRepository;
import com.example.BuildTwin._0.repository.SiteRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.BuildingService;
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
public class BuildingServiceImpl implements BuildingService {

    private static final Set<String> ALLOWED_BUILDING_STATUSES = Set.of(
            "PLANNED", "UNDER_CONSTRUCTION", "COMPLETED", "ON_HOLD"
    );

    private static final Set<String> ALLOWED_BUILDING_TYPES = Set.of(
            "RESIDENTIAL_TOWER", "COMMERCIAL_BLOCK", "CLUBHOUSE", "PODIUM", "PARKING_BLOCK", "UTILITY_BLOCK"
    );

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final SiteRepository siteRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public BuildingResponse createBuilding(Long siteId, CreateBuildingRequest request, String performedBy) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));

        String code = request.getCode().trim().toUpperCase();
        if (buildingRepository.existsBySiteIdAndCode(siteId, code)) {
            throw new DuplicateResourceException("Building", "code", code + " under Site " + site.getName());
        }

        String status = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "UNDER_CONSTRUCTION";
        String type = request.getBuildingType() != null ? validateAndNormalizeType(request.getBuildingType()) : "RESIDENTIAL_TOWER";

        Building building = Building.builder()
                .site(site)
                .code(code)
                .name(request.getName().trim())
                .buildingType(type)
                .totalFloors(request.getTotalFloors() != null ? request.getTotalFloors() : 1)
                .totalBuiltUpAreaSqFt(request.getTotalBuiltUpAreaSqFt())
                .status(status)
                .description(request.getDescription())
                .build();

        Building saved = buildingRepository.save(building);

        auditService.logAction(
                performedBy,
                "CREATE_BUILDING",
                "BUILDING",
                String.valueOf(saved.getId()),
                "Created building '" + saved.getName() + "' (" + saved.getCode() + ") under site: " + site.getName(),
                null
        );

        return mapToBuildingResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingResponse> getBuildingsBySiteId(Long siteId) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site", "id", siteId);
        }
        return buildingRepository.findBySiteId(siteId).stream()
                .map(this::mapToBuildingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BuildingResponse> getBuildingsBySiteIdPaginated(Long siteId, int page, int size) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site", "id", siteId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Building> buildingPage = buildingRepository.findBySiteId(siteId, pageable);

        List<BuildingResponse> content = buildingPage.getContent().stream()
                .map(this::mapToBuildingResponse)
                .collect(Collectors.toList());

        return PageResponse.<BuildingResponse>builder()
                .content(content)
                .pageNumber(buildingPage.getNumber())
                .pageSize(buildingPage.getSize())
                .totalElements(buildingPage.getTotalElements())
                .totalPages(buildingPage.getTotalPages())
                .isFirst(buildingPage.isFirst())
                .isLast(buildingPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingResponse getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        return mapToBuildingResponse(building);
    }

    @Override
    @Transactional
    public BuildingResponse updateBuilding(Long id, UpdateBuildingRequest request, String performedBy) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!building.getCode().equalsIgnoreCase(newCode) &&
                buildingRepository.existsBySiteIdAndCodeAndIdNot(building.getSite().getId(), newCode, id)) {
            throw new DuplicateResourceException("Building", "code", newCode + " under Site " + building.getSite().getName());
        }

        building.setCode(newCode);
        building.setName(request.getName().trim());
        if (request.getBuildingType() != null) building.setBuildingType(validateAndNormalizeType(request.getBuildingType()));
        if (request.getTotalFloors() != null) building.setTotalFloors(request.getTotalFloors());
        building.setTotalBuiltUpAreaSqFt(request.getTotalBuiltUpAreaSqFt());
        if (request.getStatus() != null) building.setStatus(validateAndNormalizeStatus(request.getStatus()));
        building.setDescription(request.getDescription());

        Building updated = buildingRepository.save(building);

        auditService.logAction(
                performedBy,
                "UPDATE_BUILDING",
                "BUILDING",
                String.valueOf(updated.getId()),
                "Updated building '" + updated.getName() + "' (" + updated.getCode() + ")",
                null
        );

        return mapToBuildingResponse(updated);
    }

    @Override
    @Transactional
    public BuildingResponse updateBuildingStatus(Long id, UpdateBuildingStatusRequest request, String performedBy) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        String oldStatus = building.getStatus();
        String validatedStatus = validateAndNormalizeStatus(request.getStatus());

        building.setStatus(validatedStatus);
        Building updated = buildingRepository.save(building);

        auditService.logAction(
                performedBy,
                "UPDATE_BUILDING_STATUS",
                "BUILDING",
                String.valueOf(id),
                "Changed status of building '" + building.getName() + "' from " + oldStatus + " to " + updated.getStatus(),
                null
        );

        return mapToBuildingResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBuilding(Long id, String performedBy) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        String name = building.getName();
        String code = building.getCode();
        buildingRepository.delete(building);

        auditService.logAction(
                performedBy,
                "DELETE_BUILDING",
                "BUILDING",
                String.valueOf(id),
                "Deleted building '" + name + "' (" + code + ")",
                null
        );
    }

    private String validateAndNormalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status cannot be blank. Allowed statuses: " + ALLOWED_BUILDING_STATUSES);
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_BUILDING_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid status: '" + status + "'. Allowed statuses are: " + ALLOWED_BUILDING_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new BadRequestException("Building type cannot be blank. Allowed types: " + ALLOWED_BUILDING_TYPES);
        }
        String normalized = type.trim().toUpperCase();
        if (!ALLOWED_BUILDING_TYPES.contains(normalized)) {
            throw new BadRequestException("Invalid building type: '" + type + "'. Allowed types are: " + ALLOWED_BUILDING_TYPES);
        }
        return normalized;
    }

    private BuildingResponse mapToBuildingResponse(Building building) {
        int floorsCount = (int) floorRepository.countByBuildingId(building.getId());

        return BuildingResponse.builder()
                .id(building.getId())
                .siteId(building.getSite() != null ? building.getSite().getId() : null)
                .siteName(building.getSite() != null ? building.getSite().getName() : null)
                .projectId(building.getSite() != null && building.getSite().getProject() != null ? building.getSite().getProject().getId() : null)
                .projectName(building.getSite() != null && building.getSite().getProject() != null ? building.getSite().getProject().getName() : null)
                .code(building.getCode())
                .name(building.getName())
                .buildingType(building.getBuildingType())
                .totalFloors(building.getTotalFloors())
                .totalBuiltUpAreaSqFt(building.getTotalBuiltUpAreaSqFt())
                .status(building.getStatus())
                .description(building.getDescription())
                .floorsCount(floorsCount)
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }
}

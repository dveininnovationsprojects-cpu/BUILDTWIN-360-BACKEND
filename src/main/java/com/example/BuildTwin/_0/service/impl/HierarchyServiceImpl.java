package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.hierarchy.HierarchyTreeResponse;
import com.example.BuildTwin._0.dto.hierarchy.HierarchyValidationResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.HierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HierarchyServiceImpl implements HierarchyService {

    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public HierarchyValidationResponse validateHierarchy(
            Long projectId, Long siteId, Long buildingId, Long floorId, Long zoneId) {

        List<String> errors = new ArrayList<>();
        Project project = null;
        Site site = null;
        Building building = null;
        Floor floor = null;
        Zone zone = null;

        if (projectId != null) {
            project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                errors.add("Project with ID " + projectId + " does not exist.");
            }
        }

        if (siteId != null) {
            site = siteRepository.findById(siteId).orElse(null);
            if (site == null) {
                errors.add("Site with ID " + siteId + " does not exist.");
            } else if (project != null && !site.getProject().getId().equals(project.getId())) {
                errors.add("Site '" + site.getName() + "' (ID: " + siteId + ") does NOT belong to Project '" + project.getName() + "' (ID: " + project.getId() + ").");
            }
        }

        if (buildingId != null) {
            building = buildingRepository.findById(buildingId).orElse(null);
            if (building == null) {
                errors.add("Building with ID " + buildingId + " does not exist.");
            } else if (site != null && !building.getSite().getId().equals(site.getId())) {
                errors.add("Building '" + building.getName() + "' (ID: " + buildingId + ") does NOT belong to Site '" + site.getName() + "' (ID: " + site.getId() + ").");
            }
        }

        if (floorId != null) {
            floor = floorRepository.findById(floorId).orElse(null);
            if (floor == null) {
                errors.add("Floor with ID " + floorId + " does not exist.");
            } else if (building != null && !floor.getBuilding().getId().equals(building.getId())) {
                errors.add("Floor '" + floor.getFloorName() + "' (ID: " + floorId + ") does NOT belong to Building '" + building.getName() + "' (ID: " + building.getId() + ").");
            }
        }

        if (zoneId != null) {
            zone = zoneRepository.findById(zoneId).orElse(null);
            if (zone == null) {
                errors.add("Zone with ID " + zoneId + " does not exist.");
            } else if (floor != null && !zone.getFloor().getId().equals(floor.getId())) {
                errors.add("Zone '" + zone.getName() + "' (ID: " + zoneId + ") does NOT belong to Floor '" + floor.getFloorName() + "' (ID: " + floor.getId() + ").");
            }
        }

        boolean isValid = errors.isEmpty();
        String message = isValid
                ? "Physical construction hierarchy lineage is valid and verified."
                : "Hierarchy validation failed due to broken parent-child linkage.";

        return HierarchyValidationResponse.builder()
                .valid(isValid)
                .message(message)
                .projectId(project != null ? project.getId() : null)
                .projectName(project != null ? project.getName() : null)
                .siteId(site != null ? site.getId() : null)
                .siteName(site != null ? site.getName() : null)
                .buildingId(building != null ? building.getId() : null)
                .buildingName(building != null ? building.getName() : null)
                .floorId(floor != null ? floor.getId() : null)
                .floorName(floor != null ? floor.getFloorName() : null)
                .zoneId(zone != null ? zone.getId() : null)
                .zoneName(zone != null ? zone.getName() : null)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HierarchyTreeResponse getProjectHierarchyTree(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<Site> sites = siteRepository.findByProjectId(projectId);
        List<HierarchyTreeResponse.SiteNode> siteNodes = new ArrayList<>();

        for (Site site : sites) {
            List<Building> buildings = buildingRepository.findBySiteId(site.getId());
            List<HierarchyTreeResponse.BuildingNode> buildingNodes = new ArrayList<>();

            for (Building bld : buildings) {
                List<Floor> floors = floorRepository.findByBuildingIdOrderByFloorNumberAsc(bld.getId());
                List<HierarchyTreeResponse.FloorNode> floorNodes = new ArrayList<>();

                for (Floor flr : floors) {
                    List<Zone> zones = zoneRepository.findByFloorId(flr.getId());
                    List<HierarchyTreeResponse.ZoneNode> zoneNodes = new ArrayList<>();

                    for (Zone zn : zones) {
                        zoneNodes.add(HierarchyTreeResponse.ZoneNode.builder()
                                .id(zn.getId())
                                .code(zn.getCode())
                                .name(zn.getName())
                                .zoneType(zn.getZoneType())
                                .areaSqFt(zn.getAreaSqFt())
                                .status(zn.getStatus())
                                .build());
                    }

                    floorNodes.add(HierarchyTreeResponse.FloorNode.builder()
                            .id(flr.getId())
                            .floorNumber(flr.getFloorNumber())
                            .floorName(flr.getFloorName())
                            .floorType(flr.getFloorType())
                            .status(flr.getStatus())
                            .zones(zoneNodes)
                            .build());
                }

                buildingNodes.add(HierarchyTreeResponse.BuildingNode.builder()
                        .id(bld.getId())
                        .name(bld.getName())
                        .code(bld.getCode())
                        .buildingType(bld.getBuildingType())
                        .totalFloors(bld.getTotalFloors())
                        .status(bld.getStatus())
                        .floors(floorNodes)
                        .build());
            }

            siteNodes.add(HierarchyTreeResponse.SiteNode.builder()
                    .id(site.getId())
                    .name(site.getName())
                    .code(site.getCode())
                    .siteType(site.getSiteType())
                    .status(site.getStatus())
                    .latitude(site.getLatitude())
                    .longitude(site.getLongitude())
                    .buildings(buildingNodes)
                    .build());
        }

        return HierarchyTreeResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .type("PROJECT")
                .status(project.getStatus())
                .sites(siteNodes)
                .build();
    }
}

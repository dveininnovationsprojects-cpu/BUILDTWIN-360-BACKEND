package com.example.BuildTwin._0.domain.projects.service;

import com.example.BuildTwin._0.domain.projects.model.*;
import com.example.BuildTwin._0.domain.projects.repository.*;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final BuildingRepository buildingRepository;
    private final LevelRepository levelRepository;
    private final ZoneRepository zoneRepository;

    @Override
    public Project createProject(Project project) {
        if (projectRepository.existsByCode(project.getCode())) {
            throw new DuplicateResourceException("Project with code " + project.getCode() + " already exists");
        }
        if (project.getStatus() == null) {
            project.setStatus("ACTIVE");
        }
        return projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    @Override
    public Building addBuilding(Building building) {
        return buildingRepository.save(building);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Building> getBuildingsByProject(Long projectId) {
        return buildingRepository.findByProjectId(projectId);
    }

    @Override
    public Level addLevel(Level level) {
        return levelRepository.save(level);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Level> getLevelsByBuilding(Long buildingId) {
        return levelRepository.findByBuildingId(buildingId);
    }

    @Override
    public Zone addZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Zone> getZonesByLevel(Long levelId) {
        return zoneRepository.findByLevelId(levelId);
    }
}

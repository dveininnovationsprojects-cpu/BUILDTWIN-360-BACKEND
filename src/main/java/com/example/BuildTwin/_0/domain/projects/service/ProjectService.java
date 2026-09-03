package com.example.BuildTwin._0.domain.projects.service;

import com.example.BuildTwin._0.domain.projects.model.*;

import java.util.List;

public interface ProjectService {
    Project createProject(Project project);
    List<Project> getAllProjects();
    Project getProjectById(Long id);
    Building addBuilding(Building building);
    List<Building> getBuildingsByProject(Long projectId);
    Level addLevel(Level level);
    List<Level> getLevelsByBuilding(Long buildingId);
    Zone addZone(Zone zone);
    List<Zone> getZonesByLevel(Long levelId);
}

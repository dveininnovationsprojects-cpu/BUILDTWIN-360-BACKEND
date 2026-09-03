package com.example.BuildTwin._0.domain.projects.repository;

import com.example.BuildTwin._0.domain.projects.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByProjectId(Long projectId);
}

package com.example.BuildTwin._0.domain.projects.repository;

import com.example.BuildTwin._0.domain.projects.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findByBuildingId(Long buildingId);
}

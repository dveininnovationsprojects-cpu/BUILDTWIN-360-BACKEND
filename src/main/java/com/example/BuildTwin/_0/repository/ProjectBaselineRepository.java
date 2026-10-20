package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.ProjectBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectBaselineRepository extends JpaRepository<ProjectBaseline, Long> {

    List<ProjectBaseline> findByProjectIdOrderByVersionDesc(Long projectId);

    Optional<ProjectBaseline> findByProjectIdAndIsActiveTrue(Long projectId);

    Optional<ProjectBaseline> findByProjectIdAndVersion(Long projectId, Integer version);

    Optional<ProjectBaseline> findTopByProjectIdOrderByVersionDesc(Long projectId);

    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);
}

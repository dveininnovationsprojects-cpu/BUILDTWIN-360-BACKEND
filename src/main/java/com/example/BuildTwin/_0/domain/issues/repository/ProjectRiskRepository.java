package com.example.BuildTwin._0.domain.issues.repository;

import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRiskRepository extends JpaRepository<ProjectRisk, Long> {
    List<ProjectRisk> findByProjectId(Long projectId);
    List<ProjectRisk> findByProjectIdAndStatus(Long projectId, String status);
    List<ProjectRisk> findByProjectIdAndSeverity(Long projectId, String severity);

    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, String status);
    long countByProjectIdAndSeverity(Long projectId, String severity);
}

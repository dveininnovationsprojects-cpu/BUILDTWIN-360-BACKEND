package com.example.BuildTwin._0.domain.quality.repository;

import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityIssueRepository extends JpaRepository<QualityIssue, Long> {
    List<QualityIssue> findByProjectId(Long projectId);
    List<QualityIssue> findByProjectIdAndStatus(Long projectId, String status);
}

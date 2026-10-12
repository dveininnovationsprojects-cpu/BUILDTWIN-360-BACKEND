package com.example.BuildTwin._0.domain.issues.repository;

import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueBlockerRepository extends JpaRepository<IssueBlocker, Long> {
    List<IssueBlocker> findByProjectId(Long projectId);
    List<IssueBlocker> findByProjectIdAndIssueType(Long projectId, String issueType);
    List<IssueBlocker> findByProjectIdAndStatus(Long projectId, String status);
    List<IssueBlocker> findByProjectIdAndSeverity(Long projectId, String severity);

    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, String status);
    long countByProjectIdAndIssueType(Long projectId, String issueType);
    long countByProjectIdAndSeverity(Long projectId, String severity);
}

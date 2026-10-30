package com.example.BuildTwin._0.domain.issues.repository;

import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;
import com.example.BuildTwin._0.domain.issues.model.ProjectIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectIssueRepository extends JpaRepository<ProjectIssue, Long> {

    List<ProjectIssue> findByProjectId(Long projectId);

    Optional<ProjectIssue> findByTicketNumber(String ticketNumber);

    List<ProjectIssue> findByProjectIdAndStatus(Long projectId, IssueStatus status);

    List<ProjectIssue> findByProjectIdAndIssueType(Long projectId, IssueType issueType);

    List<ProjectIssue> findByProjectIdAndSeverity(Long projectId, IssueSeverity severity);

    List<ProjectIssue> findByProjectIdAndAssignedTo(Long projectId, String assignedTo);

    @Query("SELECT p FROM ProjectIssue p WHERE p.projectId = :projectId AND p.status != :closedStatus AND p.targetResolutionDate < :currentDate ORDER BY p.targetResolutionDate ASC")
    List<ProjectIssue> findOverdueIssuesByProject(@Param("projectId") Long projectId,
                                                  @Param("closedStatus") IssueStatus closedStatus,
                                                  @Param("currentDate") LocalDate currentDate);

    @Query("SELECT p FROM ProjectIssue p WHERE p.status != :closedStatus AND p.targetResolutionDate < :currentDate ORDER BY p.targetResolutionDate ASC")
    List<ProjectIssue> findAllOverdueIssues(@Param("closedStatus") IssueStatus closedStatus,
                                            @Param("currentDate") LocalDate currentDate);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, IssueStatus status);

    long countByProjectIdAndIssueType(Long projectId, IssueType issueType);

    long countByProjectIdAndSeverity(Long projectId, IssueSeverity severity);

    @Query("SELECT COUNT(p) FROM ProjectIssue p WHERE p.projectId = :projectId AND p.status != :closedStatus AND p.targetResolutionDate < :currentDate")
    long countOverdueIssuesByProject(@Param("projectId") Long projectId,
                                     @Param("closedStatus") IssueStatus closedStatus,
                                     @Param("currentDate") LocalDate currentDate);
}

package com.example.BuildTwin._0.domain.issues.repository;

import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueBlockerRepository extends JpaRepository<IssueBlocker, Long> {
    List<IssueBlocker> findByProjectId(Long projectId);
}

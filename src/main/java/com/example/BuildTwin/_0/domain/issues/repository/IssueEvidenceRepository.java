package com.example.BuildTwin._0.domain.issues.repository;

import com.example.BuildTwin._0.domain.issues.model.IssueEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueEvidenceRepository extends JpaRepository<IssueEvidence, Long> {

    List<IssueEvidence> findByIssueId(Long issueId);

    List<IssueEvidence> findByIssueIdAndStage(Long issueId, String stage);

    void deleteByIssueId(Long issueId);
}

package com.example.BuildTwin._0.domain.quality.repository;

import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityEvidenceRepository extends JpaRepository<QualityEvidence, Long> {
    List<QualityEvidence> findByQualityIssueId(Long qualityIssueId);
}

package com.example.BuildTwin._0.domain.documents.repository;

import com.example.BuildTwin._0.domain.documents.model.ProjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, Long> {
    List<ProjectDocument> findByProjectId(Long projectId);
    List<ProjectDocument> findByProjectIdAndCategory(Long projectId, String category);
}

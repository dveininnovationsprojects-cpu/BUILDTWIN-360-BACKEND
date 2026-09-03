package com.example.BuildTwin._0.domain.documents.service;

import com.example.BuildTwin._0.domain.documents.model.ProjectDocument;

import java.util.List;

public interface DocumentService {
    ProjectDocument uploadDocument(ProjectDocument document);
    List<ProjectDocument> getDocumentsByProject(Long projectId);
    List<ProjectDocument> getDocumentsByProjectAndCategory(Long projectId, String category);
}

package com.example.BuildTwin._0.domain.documents.service;

import com.example.BuildTwin._0.domain.documents.model.ProjectDocument;
import com.example.BuildTwin._0.domain.documents.repository.ProjectDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuildTwin._0.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final ProjectDocumentRepository documentRepository;

    @Override
    public ProjectDocument uploadDocument(ProjectDocument document) {
        if (document.getStatus() == null) {
            document.setStatus("ACTIVE");
        }
        if (document.getVersion() == null) {
            document.setVersion("v1.0");
        }
        return documentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectDocument not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDocument> getDocumentsByProject(Long projectId) {
        return documentRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDocument> getDocumentsByProjectAndCategory(Long projectId, String category) {
        return documentRepository.findByProjectIdAndCategory(projectId, category);
    }

    @Override
    public void deleteDocument(Long id) {
        ProjectDocument doc = getDocumentById(id);
        documentRepository.delete(doc);
    }
}

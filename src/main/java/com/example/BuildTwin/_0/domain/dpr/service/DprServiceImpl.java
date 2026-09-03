package com.example.BuildTwin._0.domain.dpr.service;

import com.example.BuildTwin._0.domain.dpr.model.*;
import com.example.BuildTwin._0.domain.dpr.repository.*;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DprServiceImpl implements DprService {

    private final DprHeaderRepository dprHeaderRepository;
    private final DprActivityProgressRepository progressRepository;
    private final DprPhotoRepository photoRepository;

    @Override
    public DprHeader createDpr(DprHeader dprHeader) {
        if (dprHeader.getStatus() == null) {
            dprHeader.setStatus("DRAFT");
        }
        return dprHeaderRepository.save(dprHeader);
    }

    @Override
    @Transactional(readOnly = true)
    public DprHeader getDprById(Long id) {
        return dprHeaderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DPR Header not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DprHeader> getDprsByProject(Long projectId) {
        return dprHeaderRepository.findByProjectId(projectId);
    }

    @Override
    public DprHeader submitDpr(Long id) {
        DprHeader dpr = getDprById(id);
        dpr.setStatus("SUBMITTED");
        return dprHeaderRepository.save(dpr);
    }

    @Override
    public DprHeader approveDpr(Long id) {
        DprHeader dpr = getDprById(id);
        dpr.setStatus("APPROVED");
        return dprHeaderRepository.save(dpr);
    }

    @Override
    public DprActivityProgress addActivityProgress(DprActivityProgress progress) {
        return progressRepository.save(progress);
    }

    @Override
    public DprPhoto addPhoto(DprPhoto photo) {
        return photoRepository.save(photo);
    }
}

package com.example.BuildTwin._0.domain.dpr.service;

import com.example.BuildTwin._0.domain.dpr.model.*;

import java.util.List;

public interface DprService {
    DprHeader createDpr(DprHeader dprHeader);
    DprHeader getDprById(Long id);
    List<DprHeader> getDprsByProject(Long projectId);
    DprHeader submitDpr(Long id);
    DprHeader approveDpr(Long id);
    DprActivityProgress addActivityProgress(DprActivityProgress progress);
    DprPhoto addPhoto(DprPhoto photo);
}

package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.revision.*;
import com.example.BuildTwin._0.model.ScheduleRevisionStatus;

import java.util.List;

public interface ScheduleRevisionService {

    ScheduleRevisionResponse createDraftRevision(Long projectId, CreateScheduleRevisionRequest request, String performedBy);

    List<ScheduleRevisionResponse> getRevisionsByProject(Long projectId, ScheduleRevisionStatus status);

    ScheduleRevisionResponse getRevisionById(Long id);

    ScheduleRevisionResponse updateRevision(Long id, UpdateScheduleRevisionRequest request, String performedBy);

    ScheduleRevisionResponse addOrUpdateRevisionItem(Long revisionId, CreateScheduleRevisionItemRequest request, String performedBy);

    ScheduleRevisionResponse removeRevisionItem(Long revisionId, Long itemId, String performedBy);

    ScheduleRevisionResponse submitRevision(Long id, String performedBy);

    ScheduleRevisionResponse approveRevision(Long id, boolean applyToActivities, String performedBy);

    ScheduleRevisionResponse rejectRevision(Long id, RejectScheduleRevisionRequest request, String performedBy);

    ScheduleRevisionImpactAnalysisResponse getImpactAnalysis(Long id);

    void deleteRevision(Long id, String performedBy);
}

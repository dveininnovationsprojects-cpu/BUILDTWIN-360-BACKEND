package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.revision.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.ScheduleRevisionItemRepository;
import com.example.BuildTwin._0.repository.ScheduleRevisionRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.ScheduleRevisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleRevisionServiceImpl implements ScheduleRevisionService {

    private final ProjectRepository projectRepository;
    private final ScheduleRevisionRepository scheduleRevisionRepository;
    private final ScheduleRevisionItemRepository scheduleRevisionItemRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ScheduleRevisionResponse createDraftRevision(Long projectId, CreateScheduleRevisionRequest request, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String code = request.getRevisionCode();
        if (code != null && !code.trim().isEmpty()) {
            code = code.trim().toUpperCase();
            if (scheduleRevisionRepository.existsByProjectIdAndRevisionCodeIgnoreCase(projectId, code)) {
                throw new DuplicateResourceException("ScheduleRevision", "revisionCode", code + " in Project " + project.getName());
            }
        } else {
            long count = scheduleRevisionRepository.countByProjectId(projectId);
            code = String.format("REV-%02d", count + 1);
        }

        LocalDate revDate = request.getRevisionDate() != null ? request.getRevisionDate() : LocalDate.now();

        ScheduleRevision revision = ScheduleRevision.builder()
                .project(project)
                .revisionCode(code)
                .revisionName(request.getRevisionName().trim())
                .revisionDate(revDate)
                .reasonForRevision(request.getReasonForRevision())
                .status(ScheduleRevisionStatus.DRAFT)
                .targetCompletionDate(request.getTargetCompletionDate())
                .timeExtensionDays(request.getTimeExtensionDays())
                .costImpact(request.getCostImpact())
                .preparedBy(performedBy)
                .requestedBy(performedBy)
                .build();

        ScheduleRevision savedRevision = scheduleRevisionRepository.save(revision);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<ScheduleRevisionItem> items = new ArrayList<>();
            for (CreateScheduleRevisionItemRequest itemReq : request.getItems()) {
                WbsActivity activity = wbsActivityRepository.findById(itemReq.getActivityId())
                        .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", itemReq.getActivityId()));

                if (!activity.getProject().getId().equals(projectId)) {
                    throw new BadRequestException("Activity " + activity.getCode() + " does not belong to Project ID " + projectId);
                }

                ScheduleRevisionItem item = buildRevisionItem(savedRevision, activity, itemReq);
                items.add(item);
            }
            scheduleRevisionItemRepository.saveAll(items);
            savedRevision.setItems(items);
        }

        auditService.logAction(
                performedBy,
                "CREATE_SCHEDULE_REVISION",
                "SCHEDULE_REVISION",
                String.valueOf(savedRevision.getId()),
                "Created draft schedule revision " + savedRevision.getRevisionCode() + " ('" + savedRevision.getRevisionName() + "') for Project " + project.getName(),
                null
        );

        return mapToResponse(savedRevision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleRevisionResponse> getRevisionsByProject(Long projectId, ScheduleRevisionStatus status) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        List<ScheduleRevision> revisions;
        if (status != null) {
            revisions = scheduleRevisionRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status);
        } else {
            revisions = scheduleRevisionRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }
        return revisions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleRevisionResponse getRevisionById(Long id) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));
        return mapToResponse(revision);
    }

    @Override
    @Transactional
    public ScheduleRevisionResponse updateRevision(Long id, UpdateScheduleRevisionRequest request, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));

        if (revision.getStatus() != ScheduleRevisionStatus.DRAFT) {
            throw new BadRequestException("Cannot update schedule revision: Only DRAFT revisions can be modified. Current status: " + revision.getStatus());
        }

        if (request.getRevisionName() != null && !request.getRevisionName().trim().isEmpty()) {
            revision.setRevisionName(request.getRevisionName().trim());
        }
        if (request.getRevisionDate() != null) {
            revision.setRevisionDate(request.getRevisionDate());
        }
        if (request.getReasonForRevision() != null) {
            revision.setReasonForRevision(request.getReasonForRevision());
        }
        if (request.getTargetCompletionDate() != null) {
            revision.setTargetCompletionDate(request.getTargetCompletionDate());
        }
        if (request.getTimeExtensionDays() != null) {
            revision.setTimeExtensionDays(request.getTimeExtensionDays());
        }
        if (request.getCostImpact() != null) {
            revision.setCostImpact(request.getCostImpact());
        }

        ScheduleRevision updated = scheduleRevisionRepository.save(revision);

        auditService.logAction(
                performedBy,
                "UPDATE_SCHEDULE_REVISION",
                "SCHEDULE_REVISION",
                String.valueOf(updated.getId()),
                "Updated details of draft schedule revision " + updated.getRevisionCode(),
                null
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ScheduleRevisionResponse addOrUpdateRevisionItem(Long revisionId, CreateScheduleRevisionItemRequest request, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", revisionId));

        if (revision.getStatus() != ScheduleRevisionStatus.DRAFT) {
            throw new BadRequestException("Cannot add/update activity: Only DRAFT schedule revisions can be modified. Current status: " + revision.getStatus());
        }

        WbsActivity activity = wbsActivityRepository.findById(request.getActivityId())
                        .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", request.getActivityId()));

        if (!activity.getProject().getId().equals(revision.getProject().getId())) {
            throw new BadRequestException("Activity " + activity.getCode() + " does not belong to Project " + revision.getProject().getName());
        }

        Optional<ScheduleRevisionItem> existingItem = scheduleRevisionItemRepository.findByScheduleRevisionIdAndActivityId(revisionId, request.getActivityId());

        ScheduleRevisionItem item;
        if (existingItem.isPresent()) {
            item = existingItem.get();
            updateRevisionItemFields(item, activity, request);
        } else {
            item = buildRevisionItem(revision, activity, request);
        }

        scheduleRevisionItemRepository.save(item);

        // Refresh revision items
        ScheduleRevision refreshed = scheduleRevisionRepository.findById(revisionId).orElse(revision);

        auditService.logAction(
                performedBy,
                "ADD_REVISION_ITEM",
                "SCHEDULE_REVISION_ITEM",
                String.valueOf(item.getId()),
                "Added/Updated activity " + activity.getCode() + " in revision " + revision.getRevisionCode(),
                null
        );

        return mapToResponse(refreshed);
    }

    @Override
    @Transactional
    public ScheduleRevisionResponse removeRevisionItem(Long revisionId, Long itemId, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", revisionId));

        if (revision.getStatus() != ScheduleRevisionStatus.DRAFT) {
            throw new BadRequestException("Cannot remove item: Only DRAFT schedule revisions can be modified.");
        }

        ScheduleRevisionItem item = scheduleRevisionItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevisionItem", "id", itemId));

        if (!item.getScheduleRevision().getId().equals(revisionId)) {
            throw new BadRequestException("Item " + itemId + " does not belong to Schedule Revision " + revisionId);
        }

        scheduleRevisionItemRepository.delete(item);

        ScheduleRevision refreshed = scheduleRevisionRepository.findById(revisionId).orElse(revision);

        auditService.logAction(
                performedBy,
                "REMOVE_REVISION_ITEM",
                "SCHEDULE_REVISION_ITEM",
                String.valueOf(itemId),
                "Removed activity change item from revision " + revision.getRevisionCode(),
                null
        );

        return mapToResponse(refreshed);
    }

    @Override
    @Transactional
    public ScheduleRevisionResponse submitRevision(Long id, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));

        if (revision.getStatus() != ScheduleRevisionStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT schedule revisions can be submitted for approval. Current status: " + revision.getStatus());
        }

        revision.setStatus(ScheduleRevisionStatus.SUBMITTED);
        revision.setReviewedBy(performedBy);

        ScheduleRevision saved = scheduleRevisionRepository.save(revision);

        auditService.logAction(
                performedBy,
                "SUBMIT_SCHEDULE_REVISION",
                "SCHEDULE_REVISION",
                String.valueOf(saved.getId()),
                "Submitted schedule revision " + saved.getRevisionCode() + " for formal approval",
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleRevisionResponse approveRevision(Long id, boolean applyToActivities, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));

        if (revision.getStatus() != ScheduleRevisionStatus.SUBMITTED && revision.getStatus() != ScheduleRevisionStatus.DRAFT) {
            throw new BadRequestException("Only SUBMITTED or DRAFT revisions can be approved. Current status: " + revision.getStatus());
        }

        revision.setStatus(applyToActivities ? ScheduleRevisionStatus.APPLIED : ScheduleRevisionStatus.APPROVED);
        revision.setApprovedBy(performedBy);
        revision.setApprovedAt(LocalDateTime.now());

        if (applyToActivities && revision.getItems() != null) {
            for (ScheduleRevisionItem item : revision.getItems()) {
                WbsActivity activity = item.getActivity();
                if (activity != null) {
                    if (item.getRevisedPlannedStartDate() != null) {
                        activity.setPlannedStartDate(item.getRevisedPlannedStartDate());
                    }
                    if (item.getRevisedPlannedEndDate() != null) {
                        activity.setPlannedEndDate(item.getRevisedPlannedEndDate());
                    }
                    wbsActivityRepository.save(activity);
                }
            }
        }

        ScheduleRevision saved = scheduleRevisionRepository.save(revision);

        auditService.logAction(
                performedBy,
                "APPROVE_SCHEDULE_REVISION",
                "SCHEDULE_REVISION",
                String.valueOf(saved.getId()),
                "Approved schedule revision " + saved.getRevisionCode() + (applyToActivities ? " and applied revised dates to WBS activities" : ""),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleRevisionResponse rejectRevision(Long id, RejectScheduleRevisionRequest request, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));

        if (revision.getStatus() != ScheduleRevisionStatus.SUBMITTED && revision.getStatus() != ScheduleRevisionStatus.DRAFT) {
            throw new BadRequestException("Cannot reject schedule revision with status: " + revision.getStatus());
        }

        revision.setStatus(ScheduleRevisionStatus.REJECTED);
        revision.setRejectionReason(request.getRejectionReason());

        ScheduleRevision saved = scheduleRevisionRepository.save(revision);

        auditService.logAction(
                performedBy,
                "REJECT_SCHEDULE_REVISION",
                "SCHEDULE_REVISION",
                String.valueOf(saved.getId()),
                "Rejected schedule revision " + saved.getRevisionCode() + ". Reason: " + request.getRejectionReason(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleRevisionImpactAnalysisResponse getImpactAnalysis(Long id) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));

        List<ScheduleRevisionItem> items = revision.getItems();
        int totalChanged = items != null ? items.size() : 0;

        long maxDelay = 0;
        if (items != null) {
            for (ScheduleRevisionItem item : items) {
                if (item.getVarianceDays() != null && item.getVarianceDays() > maxDelay) {
                    maxDelay = item.getVarianceDays();
                }
            }
        }

        Project project = revision.getProject();
        LocalDate originalEndDate = project.getPlannedEndDate();
        LocalDate proposedEndDate = revision.getTargetCompletionDate() != null
                ? revision.getTargetCompletionDate()
                : (originalEndDate != null && maxDelay > 0 ? originalEndDate.plusDays(maxDelay) : originalEndDate);

        return ScheduleRevisionImpactAnalysisResponse.builder()
                .revisionId(revision.getId())
                .revisionCode(revision.getRevisionCode())
                .revisionName(revision.getRevisionName())
                .projectId(project.getId())
                .projectName(project.getName())
                .status(revision.getStatus())
                .totalActivitiesChanged(totalChanged)
                .maxActivityDelayDays(maxDelay)
                .netProjectTimeExtensionDays(revision.getTimeExtensionDays())
                .originalProjectEndDate(originalEndDate)
                .proposedProjectEndDate(proposedEndDate)
                .totalCostImpact(revision.getCostImpact())
                .items(items != null ? items.stream().map(this::mapItemToResponse).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }

    @Override
    @Transactional
    public void deleteRevision(Long id, String performedBy) {
        ScheduleRevision revision = scheduleRevisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleRevision", "id", id));

        if (revision.getStatus() != ScheduleRevisionStatus.DRAFT && revision.getStatus() != ScheduleRevisionStatus.REJECTED) {
            throw new BadRequestException("Cannot delete schedule revision: Only DRAFT or REJECTED revisions can be deleted. Current status: " + revision.getStatus());
        }

        scheduleRevisionRepository.delete(revision);

        auditService.logAction(
                performedBy,
                "DELETE_SCHEDULE_REVISION",
                "SCHEDULE_REVISION",
                String.valueOf(id),
                "Deleted schedule revision " + revision.getRevisionCode(),
                null
        );
    }

    private ScheduleRevisionItem buildRevisionItem(ScheduleRevision revision, WbsActivity activity, CreateScheduleRevisionItemRequest req) {
        LocalDate origStart = activity.getPlannedStartDate();
        LocalDate origEnd = activity.getPlannedEndDate();
        LocalDate revStart = req.getRevisedPlannedStartDate() != null ? req.getRevisedPlannedStartDate() : origStart;
        LocalDate revEnd = req.getRevisedPlannedEndDate();

        int origDuration = 0;
        if (origStart != null && origEnd != null) {
            origDuration = (int) ChronoUnit.DAYS.between(origStart, origEnd);
        }

        int revDuration = 0;
        if (revStart != null && revEnd != null) {
            revDuration = (int) ChronoUnit.DAYS.between(revStart, revEnd);
        }

        long varianceDays = 0;
        if (origEnd != null && revEnd != null) {
            varianceDays = ChronoUnit.DAYS.between(origEnd, revEnd);
        }

        return ScheduleRevisionItem.builder()
                .scheduleRevision(revision)
                .activity(activity)
                .activityCode(activity.getCode())
                .activityName(activity.getName())
                .originalPlannedStartDate(origStart)
                .originalPlannedEndDate(origEnd)
                .revisedPlannedStartDate(revStart)
                .revisedPlannedEndDate(revEnd)
                .originalDurationDays(origDuration)
                .revisedDurationDays(revDuration)
                .varianceDays(varianceDays)
                .delayReason(req.getDelayReason())
                .mitigationAction(req.getMitigationAction())
                .build();
    }

    private void updateRevisionItemFields(ScheduleRevisionItem item, WbsActivity activity, CreateScheduleRevisionItemRequest req) {
        LocalDate origStart = item.getOriginalPlannedStartDate() != null ? item.getOriginalPlannedStartDate() : activity.getPlannedStartDate();
        LocalDate origEnd = item.getOriginalPlannedEndDate() != null ? item.getOriginalPlannedEndDate() : activity.getPlannedEndDate();
        LocalDate revStart = req.getRevisedPlannedStartDate() != null ? req.getRevisedPlannedStartDate() : origStart;
        LocalDate revEnd = req.getRevisedPlannedEndDate();

        int origDuration = 0;
        if (origStart != null && origEnd != null) {
            origDuration = (int) ChronoUnit.DAYS.between(origStart, origEnd);
        }

        int revDuration = 0;
        if (revStart != null && revEnd != null) {
            revDuration = (int) ChronoUnit.DAYS.between(revStart, revEnd);
        }

        long varianceDays = 0;
        if (origEnd != null && revEnd != null) {
            varianceDays = ChronoUnit.DAYS.between(origEnd, revEnd);
        }

        item.setRevisedPlannedStartDate(revStart);
        item.setRevisedPlannedEndDate(revEnd);
        item.setOriginalDurationDays(origDuration);
        item.setRevisedDurationDays(revDuration);
        item.setVarianceDays(varianceDays);
        if (req.getDelayReason() != null) {
            item.setDelayReason(req.getDelayReason());
        }
        if (req.getMitigationAction() != null) {
            item.setMitigationAction(req.getMitigationAction());
        }
    }

    private ScheduleRevisionResponse mapToResponse(ScheduleRevision revision) {
        List<ScheduleRevisionItemResponse> itemResponses = revision.getItems() != null
                ? revision.getItems().stream().map(this::mapItemToResponse).collect(Collectors.toList())
                : new ArrayList<>();

        long maxVariance = 0;
        for (ScheduleRevisionItemResponse item : itemResponses) {
            if (item.getVarianceDays() != null && item.getVarianceDays() > maxVariance) {
                maxVariance = item.getVarianceDays();
            }
        }

        return ScheduleRevisionResponse.builder()
                .id(revision.getId())
                .projectId(revision.getProject().getId())
                .projectName(revision.getProject().getName())
                .revisionCode(revision.getRevisionCode())
                .revisionName(revision.getRevisionName())
                .revisionDate(revision.getRevisionDate())
                .reasonForRevision(revision.getReasonForRevision())
                .status(revision.getStatus())
                .targetCompletionDate(revision.getTargetCompletionDate())
                .timeExtensionDays(revision.getTimeExtensionDays())
                .costImpact(revision.getCostImpact())
                .totalAffectedActivities(itemResponses.size())
                .maxVarianceDays(maxVariance)
                .preparedBy(revision.getPreparedBy())
                .requestedBy(revision.getRequestedBy())
                .reviewedBy(revision.getReviewedBy())
                .approvedBy(revision.getApprovedBy())
                .approvedAt(revision.getApprovedAt())
                .rejectionReason(revision.getRejectionReason())
                .items(itemResponses)
                .createdAt(revision.getCreatedAt())
                .updatedAt(revision.getUpdatedAt())
                .build();
    }

    private ScheduleRevisionItemResponse mapItemToResponse(ScheduleRevisionItem item) {
        return ScheduleRevisionItemResponse.builder()
                .id(item.getId())
                .scheduleRevisionId(item.getScheduleRevision() != null ? item.getScheduleRevision().getId() : null)
                .activityId(item.getActivity() != null ? item.getActivity().getId() : null)
                .activityCode(item.getActivityCode())
                .activityName(item.getActivityName())
                .originalPlannedStartDate(item.getOriginalPlannedStartDate())
                .originalPlannedEndDate(item.getOriginalPlannedEndDate())
                .revisedPlannedStartDate(item.getRevisedPlannedStartDate())
                .revisedPlannedEndDate(item.getRevisedPlannedEndDate())
                .originalDurationDays(item.getOriginalDurationDays())
                .revisedDurationDays(item.getRevisedDurationDays())
                .varianceDays(item.getVarianceDays())
                .delayReason(item.getDelayReason())
                .mitigationAction(item.getMitigationAction())
                .build();
    }
}

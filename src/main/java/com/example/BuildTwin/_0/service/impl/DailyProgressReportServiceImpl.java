package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.dpr.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.DailyProgressReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyProgressReportServiceImpl implements DailyProgressReportService {

    private final DailyProgressReportRepository dprRepository;
    private final ProjectRepository projectRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ActivityProgressLogRepository activityProgressLogRepository;
    private final DprActivityItemRepository dprActivityItemRepository;
    private final DprLaborItemRepository dprLaborItemRepository;
    private final DprEquipmentItemRepository dprEquipmentItemRepository;
    private final DprMaterialItemRepository dprMaterialItemRepository;
    private final DprPhotoAttachmentRepository dprPhotoAttachmentRepository;
    private final AuditService auditService;

    // =========================================================================
    // 1. CORE CRUD OPERATIONS
    // =========================================================================

    @Override
    @Transactional
    public DailyProgressReportResponse createDpr(Long projectId, CreateDprRequest request, String currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        WorkShift shift = request.getShift() != null ? request.getShift() : WorkShift.DAY_SHIFT;
        WeatherCondition weather = request.getWeatherCondition() != null ? request.getWeatherCondition() : WeatherCondition.SUNNY;

        if (dprRepository.existsByProjectIdAndReportDateAndShift(projectId, request.getReportDate(), shift)) {
            throw new DuplicateResourceException(
                    "A Daily Progress Report already exists for project ID " + projectId +
                            " on date " + request.getReportDate() + " for shift " + shift);
        }

        String dateStr = request.getReportDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        String shiftCode = shift.name().length() >= 3 ? shift.name().substring(0, 3) : shift.name();
        String baseCode = "DPR-" + project.getCode() + "-" + dateStr + "-" + shiftCode;
        String reportCode = baseCode;
        int counter = 1;
        while (dprRepository.findByReportCode(reportCode).isPresent()) {
            reportCode = baseCode + "-" + counter++;
        }

        DailyProgressReport dpr = DailyProgressReport.builder()
                .reportCode(reportCode)
                .project(project)
                .reportDate(request.getReportDate())
                .shift(shift)
                .weatherCondition(weather)
                .temperatureCelsius(request.getTemperatureCelsius())
                .status(DprStatus.DRAFT)
                .siteEngineer(request.getSiteEngineer() != null ? request.getSiteEngineer() : currentUser)
                .generalNotes(request.getGeneralNotes())
                .safetyIncidentsCount(request.getSafetyIncidentsCount() != null ? request.getSafetyIncidentsCount() : 0)
                .safetyNotes(request.getSafetyNotes())
                .qualityObservations(request.getQualityObservations())
                .siteHindrances(request.getSiteHindrances())
                .activityItems(new ArrayList<>())
                .laborItems(new ArrayList<>())
                .equipmentItems(new ArrayList<>())
                .materialItems(new ArrayList<>())
                .photoAttachments(new ArrayList<>())
                .build();

        // Populate initial activity items
        if (request.getActivityItems() != null && !request.getActivityItems().isEmpty()) {
            for (DprActivityItemRequest itemReq : request.getActivityItems()) {
                WbsActivity activity = wbsActivityRepository.findById(itemReq.getActivityId())
                        .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", itemReq.getActivityId()));
                if (!activity.getProject().getId().equals(projectId)) {
                    throw new BadRequestException("Activity ID " + itemReq.getActivityId() + " does not belong to project " + projectId);
                }
                double currentCompleted = activity.getCompletedQuantity() != null ? activity.getCompletedQuantity() : 0.0;
                double planned = activity.getPlannedQuantity() != null ? activity.getPlannedQuantity() : 0.0;
                double todayPct = planned > 0 ? (itemReq.getQuantityToday() / planned) * 100.0 : 0.0;
                double estCumulative = currentCompleted + itemReq.getQuantityToday();
                double estCumulativePct = planned > 0 ? (estCumulative / planned) * 100.0 : 0.0;

                boolean isBlocker = Boolean.TRUE.equals(itemReq.getIsBlocker());
                BlockerStatus blockerStatus = isBlocker ? BlockerStatus.OPEN : null;
                BlockerSeverity severity = itemReq.getBlockerSeverity() != null
                        ? itemReq.getBlockerSeverity()
                        : (isBlocker ? BlockerSeverity.MEDIUM : null);

                DprActivityItem item = DprActivityItem.builder()
                        .dpr(dpr)
                        .activity(activity)
                        .quantityToday(itemReq.getQuantityToday())
                        .cumulativeQuantity(estCumulative)
                        .progressPercentageToday(Math.round(todayPct * 100.0) / 100.0)
                        .cumulativeProgressPercentage(Math.round(estCumulativePct * 100.0) / 100.0)
                        .locationReference(itemReq.getLocationReference())
                        .remarks(itemReq.getRemarks())
                        .isBlocker(isBlocker)
                        .blockerCategory(itemReq.getBlockerCategory())
                        .blockerReason(itemReq.getBlockerReason())
                        .blockerSeverity(severity)
                        .blockerStatus(blockerStatus)
                        .impactHours(itemReq.getImpactHours())
                        .build();
                dpr.getActivityItems().add(item);
            }
        }

        // Populate initial labor items
        if (request.getLaborItems() != null && !request.getLaborItems().isEmpty()) {
            for (DprLaborItemRequest laborReq : request.getLaborItems()) {
                DprLaborItem laborItem = DprLaborItem.builder()
                        .dpr(dpr)
                        .tradeCategory(laborReq.getTradeCategory())
                        .headcount(laborReq.getHeadcount())
                        .hoursWorked(laborReq.getHoursWorked() != null ? laborReq.getHoursWorked() : 8.0)
                        .subcontractorName(laborReq.getSubcontractorName())
                        .remarks(laborReq.getRemarks())
                        .build();
                dpr.getLaborItems().add(laborItem);
            }
        }

        // Populate initial equipment items
        if (request.getEquipmentItems() != null && !request.getEquipmentItems().isEmpty()) {
            for (DprEquipmentItemRequest equipReq : request.getEquipmentItems()) {
                DprEquipmentItem equipItem = DprEquipmentItem.builder()
                        .dpr(dpr)
                        .equipmentName(equipReq.getEquipmentName())
                        .equipmentCode(equipReq.getEquipmentCode())
                        .quantity(equipReq.getQuantity() != null ? equipReq.getQuantity() : 1)
                        .operatingHours(equipReq.getOperatingHours() != null ? equipReq.getOperatingHours() : 0.0)
                        .idleHours(equipReq.getIdleHours() != null ? equipReq.getIdleHours() : 0.0)
                        .breakdownHours(equipReq.getBreakdownHours() != null ? equipReq.getBreakdownHours() : 0.0)
                        .fuelConsumedLiters(equipReq.getFuelConsumedLiters())
                        .remarks(equipReq.getRemarks())
                        .build();
                dpr.getEquipmentItems().add(equipItem);
            }
        }

        // Populate initial material items
        if (request.getMaterialItems() != null && !request.getMaterialItems().isEmpty()) {
            for (DprMaterialItemRequest matReq : request.getMaterialItems()) {
                DprMaterialItem matItem = DprMaterialItem.builder()
                        .dpr(dpr)
                        .materialName(matReq.getMaterialName())
                        .unitOfMeasure(matReq.getUnitOfMeasure())
                        .quantityReceived(matReq.getQuantityReceived() != null ? matReq.getQuantityReceived() : 0.0)
                        .quantityConsumed(matReq.getQuantityConsumed() != null ? matReq.getQuantityConsumed() : 0.0)
                        .supplierName(matReq.getSupplierName())
                        .challanOrInvoiceNumber(matReq.getChallanOrInvoiceNumber())
                        .remarks(matReq.getRemarks())
                        .build();
                dpr.getMaterialItems().add(matItem);
            }
        }

        // Populate initial photo attachments
        if (request.getPhotoAttachments() != null && !request.getPhotoAttachments().isEmpty()) {
            for (DprPhotoAttachmentRequest photoReq : request.getPhotoAttachments()) {
                DprPhotoAttachment photo = DprPhotoAttachment.builder()
                        .dpr(dpr)
                        .photoUrl(photoReq.getPhotoUrl())
                        .caption(photoReq.getCaption())
                        .category(photoReq.getCategory() != null ? photoReq.getCategory() : "PROGRESS")
                        .takenAt(photoReq.getTakenAt() != null ? photoReq.getTakenAt() : LocalDateTime.now())
                        .build();
                dpr.getPhotoAttachments().add(photo);
            }
        }

        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);

        try {
            auditService.logAction(
                    currentUser != null ? currentUser : "SYSTEM",
                    "CREATE_DPR",
                    "DailyProgressReport",
                    String.valueOf(saved.getId()),
                    "Created Draft DPR: " + saved.getReportCode() + " for project " + project.getName(),
                    null
            );
        } catch (Exception ex) {
            log.warn("Failed to log audit for DPR create: {}", ex.getMessage());
        }

        log.info("Created Draft Daily Progress Report {} (ID: {}) for Project ID {}", saved.getReportCode(), saved.getId(), projectId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyProgressReportResponse getDprById(Long dprId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        return mapToResponse(dpr);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyProgressReportResponse> getDprsByProject(
            Long projectId, LocalDate startDate, LocalDate endDate, DprStatus status) {

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<DailyProgressReport> reports;
        if (startDate != null && endDate != null) {
            reports = dprRepository.findByProjectIdAndReportDateBetweenOrderByReportDateAsc(projectId, startDate, endDate);
        } else if (status != null) {
            reports = dprRepository.findByProjectIdAndStatusOrderByReportDateDesc(projectId, status);
        } else {
            reports = dprRepository.findByProjectIdOrderByReportDateDesc(projectId);
        }

        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DailyProgressReportResponse updateDpr(Long dprId, UpdateDprRequest request, String currentUser) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        if (dpr.getStatus() == DprStatus.APPROVED) {
            throw new BadRequestException("Approved Daily Progress Reports are immutable and cannot be updated.");
        }
        if (dpr.getStatus() == DprStatus.SUBMITTED) {
            throw new BadRequestException("Submitted DPR is under management review. Please reject or withdraw before editing.");
        }

        if (request.getShift() != null) dpr.setShift(request.getShift());
        if (request.getWeatherCondition() != null) dpr.setWeatherCondition(request.getWeatherCondition());
        if (request.getTemperatureCelsius() != null) dpr.setTemperatureCelsius(request.getTemperatureCelsius());
        if (request.getSiteEngineer() != null) dpr.setSiteEngineer(request.getSiteEngineer());
        if (request.getGeneralNotes() != null) dpr.setGeneralNotes(request.getGeneralNotes());
        if (request.getSafetyIncidentsCount() != null) dpr.setSafetyIncidentsCount(request.getSafetyIncidentsCount());
        if (request.getSafetyNotes() != null) dpr.setSafetyNotes(request.getSafetyNotes());
        if (request.getQualityObservations() != null) dpr.setQualityObservations(request.getQualityObservations());
        if (request.getSiteHindrances() != null) dpr.setSiteHindrances(request.getSiteHindrances());

        // If previously REJECTED, returning to DRAFT upon modification
        if (dpr.getStatus() == DprStatus.REJECTED) {
            dpr.setStatus(DprStatus.DRAFT);
        }

        DailyProgressReport updated = dprRepository.save(dpr);

        try {
            auditService.logAction(
                    currentUser != null ? currentUser : "SYSTEM",
                    "UPDATE_DPR",
                    "DailyProgressReport",
                    String.valueOf(updated.getId()),
                    "Updated DPR: " + updated.getReportCode(),
                    null
            );
        } catch (Exception ex) {
            log.warn("Failed to log audit for DPR update: {}", ex.getMessage());
        }

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDpr(Long dprId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        if (dpr.getStatus() == DprStatus.APPROVED) {
            throw new BadRequestException("Approved Daily Progress Reports cannot be deleted because progress has already synced to WBS activities.");
        }

        dprRepository.delete(dpr);
        log.info("Deleted DPR ID: {}", dprId);
    }

    // =========================================================================
    // 2. DPR ACTIVITY ITEMS MANAGEMENT
    // =========================================================================

    @Override
    @Transactional
    public DailyProgressReportResponse addActivityItem(Long dprId, DprActivityItemRequest request) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        WbsActivity activity = wbsActivityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", request.getActivityId()));

        if (!activity.getProject().getId().equals(dpr.getProject().getId())) {
            throw new BadRequestException("Activity ID " + request.getActivityId() + " does not belong to project " + dpr.getProject().getId());
        }

        double currentCompleted = activity.getCompletedQuantity() != null ? activity.getCompletedQuantity() : 0.0;
        double planned = activity.getPlannedQuantity() != null ? activity.getPlannedQuantity() : 0.0;
        double todayPct = planned > 0 ? (request.getQuantityToday() / planned) * 100.0 : 0.0;
        double estCumulative = currentCompleted + request.getQuantityToday();
        double estCumulativePct = planned > 0 ? (estCumulative / planned) * 100.0 : 0.0;

        boolean isBlocker = Boolean.TRUE.equals(request.getIsBlocker());
        BlockerStatus blockerStatus = isBlocker ? BlockerStatus.OPEN : null;
        BlockerSeverity severity = request.getBlockerSeverity() != null
                ? request.getBlockerSeverity()
                : (isBlocker ? BlockerSeverity.MEDIUM : null);

        DprActivityItem item = DprActivityItem.builder()
                .dpr(dpr)
                .activity(activity)
                .quantityToday(request.getQuantityToday())
                .cumulativeQuantity(estCumulative)
                .progressPercentageToday(Math.round(todayPct * 100.0) / 100.0)
                .cumulativeProgressPercentage(Math.round(estCumulativePct * 100.0) / 100.0)
                .locationReference(request.getLocationReference())
                .remarks(request.getRemarks())
                .isBlocker(isBlocker)
                .blockerCategory(request.getBlockerCategory())
                .blockerReason(request.getBlockerReason())
                .blockerSeverity(severity)
                .blockerStatus(blockerStatus)
                .impactHours(request.getImpactHours())
                .build();

        dpr.getActivityItems().add(item);
        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse removeActivityItem(Long dprId, Long itemId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        boolean removed = dpr.getActivityItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("DprActivityItem", "id", itemId);
        }
        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DprActivityItemResponse> getActivityItems(Long dprId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        return (dpr.getActivityItems() != null)
                ? dpr.getActivityItems().stream().map(this::mapActivityItem).collect(Collectors.toList())
                : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public DprActivityItemResponse getActivityItemById(Long dprId, Long itemId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        DprActivityItem item = dpr.getActivityItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DprActivityItem", "id", itemId));
        return mapActivityItem(item);
    }

    @Override
    @Transactional
    public DprActivityItemResponse updateActivityItem(Long dprId, Long itemId, UpdateDprActivityItemRequest request) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        DprActivityItem item = dpr.getActivityItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DprActivityItem", "id", itemId));

        if (request.getQuantityToday() != null) {
            WbsActivity activity = item.getActivity();
            double currentCompleted = activity.getCompletedQuantity() != null ? activity.getCompletedQuantity() : 0.0;
            double planned = activity.getPlannedQuantity() != null ? activity.getPlannedQuantity() : 0.0;
            double todayPct = planned > 0 ? (request.getQuantityToday() / planned) * 100.0 : 0.0;
            double estCumulative = currentCompleted + request.getQuantityToday();
            double estCumulativePct = planned > 0 ? (estCumulative / planned) * 100.0 : 0.0;

            item.setQuantityToday(request.getQuantityToday());
            item.setCumulativeQuantity(estCumulative);
            item.setProgressPercentageToday(Math.round(todayPct * 100.0) / 100.0);
            item.setCumulativeProgressPercentage(Math.round(estCumulativePct * 100.0) / 100.0);
        }

        if (request.getLocationReference() != null) {
            item.setLocationReference(request.getLocationReference());
        }
        if (request.getRemarks() != null) {
            item.setRemarks(request.getRemarks());
        }
        if (request.getIsBlocker() != null) {
            item.setIsBlocker(request.getIsBlocker());
            if (Boolean.TRUE.equals(request.getIsBlocker()) && item.getBlockerStatus() == null) {
                item.setBlockerStatus(BlockerStatus.OPEN);
            }
        }
        if (request.getBlockerCategory() != null) {
            item.setBlockerCategory(request.getBlockerCategory());
        }
        if (request.getBlockerReason() != null) {
            item.setBlockerReason(request.getBlockerReason());
        }
        if (request.getBlockerSeverity() != null) {
            item.setBlockerSeverity(request.getBlockerSeverity());
        }
        if (request.getBlockerStatus() != null) {
            item.setBlockerStatus(request.getBlockerStatus());
            if (request.getBlockerStatus() == BlockerStatus.RESOLVED && item.getBlockerResolvedAt() == null) {
                item.setBlockerResolvedAt(LocalDateTime.now());
            }
        }
        if (request.getImpactHours() != null) {
            item.setImpactHours(request.getImpactHours());
        }
        if (request.getBlockerResolutionNotes() != null) {
            item.setBlockerResolutionNotes(request.getBlockerResolutionNotes());
        }

        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        DprActivityItem updatedItem = saved.getActivityItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(item);

        return mapActivityItem(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DprActivityItemResponse> getActivityDprHistory(Long activityId) {
        if (!wbsActivityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("WbsActivity", "id", activityId);
        }
        List<DprActivityItem> items = dprActivityItemRepository.findByActivityId(activityId);
        return items.stream().map(this::mapActivityItem).collect(Collectors.toList());
    }

    // =========================================================================
    // 2B. BLOCKER & REMARKS SUPPORT OPERATIONS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<DprBlockerItemResponse> getDprBlockers(Long dprId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        return (dpr.getActivityItems() != null)
                ? dpr.getActivityItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsBlocker()))
                .map(this::mapToBlockerResponse)
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DprBlockerItemResponse> getProjectDprBlockers(
            Long projectId, BlockerStatus status, BlockerCategory category, BlockerSeverity severity, Long activityId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<DprActivityItem> items = dprActivityItemRepository.findProjectBlockersWithFilters(
                projectId, status, category, severity, activityId);

        return items.stream()
                .map(this::mapToBlockerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DprBlockerSummaryResponse getProjectBlockerSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<DprActivityItem> allBlockers = dprActivityItemRepository.findByDprProjectIdAndIsBlockerTrueOrderByCreatedAtDesc(projectId);

        int total = allBlockers.size();
        int open = (int) allBlockers.stream().filter(b -> b.getBlockerStatus() == BlockerStatus.OPEN || b.getBlockerStatus() == null).count();
        int inProgress = (int) allBlockers.stream().filter(b -> b.getBlockerStatus() == BlockerStatus.IN_PROGRESS).count();
        int resolved = (int) allBlockers.stream().filter(b -> b.getBlockerStatus() == BlockerStatus.RESOLVED || b.getBlockerStatus() == BlockerStatus.WAIVED).count();
        int critical = (int) allBlockers.stream().filter(b -> b.getBlockerSeverity() == BlockerSeverity.CRITICAL).count();
        int high = (int) allBlockers.stream().filter(b -> b.getBlockerSeverity() == BlockerSeverity.HIGH).count();

        double totalImpact = allBlockers.stream()
                .mapToDouble(b -> b.getImpactHours() != null ? b.getImpactHours() : 0.0)
                .sum();
        totalImpact = Math.round(totalImpact * 100.0) / 100.0;

        Map<String, Long> categoryBreakdown = allBlockers.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBlockerCategory() != null ? b.getBlockerCategory().name() : "UNSPECIFIED",
                        Collectors.counting()
                ));

        Map<String, Long> severityBreakdown = allBlockers.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBlockerSeverity() != null ? b.getBlockerSeverity().name() : "MEDIUM",
                        Collectors.counting()
                ));

        Map<String, Long> statusBreakdown = allBlockers.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBlockerStatus() != null ? b.getBlockerStatus().name() : "OPEN",
                        Collectors.counting()
                ));

        List<DprBlockerItemResponse> recent = allBlockers.stream()
                .limit(10)
                .map(this::mapToBlockerResponse)
                .collect(Collectors.toList());

        return DprBlockerSummaryResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .totalBlockersCount(total)
                .openBlockersCount(open)
                .inProgressBlockersCount(inProgress)
                .resolvedBlockersCount(resolved)
                .criticalBlockersCount(critical)
                .highBlockersCount(high)
                .totalImpactHours(totalImpact)
                .categoryBreakdown(categoryBreakdown)
                .severityBreakdown(severityBreakdown)
                .statusBreakdown(statusBreakdown)
                .recentBlockers(recent)
                .build();
    }

    @Override
    @Transactional
    public DprBlockerItemResponse resolveBlocker(Long dprId, Long itemId, ResolveDprBlockerRequest request, String resolver) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        DprActivityItem item = dpr.getActivityItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DprActivityItem", "id", itemId));

        if (!Boolean.TRUE.equals(item.getIsBlocker())) {
            throw new BadRequestException("Activity item ID " + itemId + " is not flagged as a blocker.");
        }

        BlockerStatus targetStatus = request.getStatus() != null ? request.getStatus() : BlockerStatus.RESOLVED;
        item.setBlockerStatus(targetStatus);
        item.setBlockerResolutionNotes(request.getResolutionNotes());
        item.setBlockerResolvedBy(resolver != null ? resolver : "SYSTEM");
        item.setBlockerResolvedAt(LocalDateTime.now());
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            item.setRemarks(request.getRemarks());
        }

        dpr.recalculateTotals();
        dprRepository.save(dpr);

        log.info("Blocker on Activity Item ID: {} (DPR: {}) updated to status {} by {}",
                itemId, dpr.getReportCode(), targetStatus, resolver);

        return mapToBlockerResponse(item);
    }

    // =========================================================================
    // 3. ACTIVITY QUANTITY TRACKING
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DprQuantityTrackingSummaryResponse getDprQuantityTracking(Long dprId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        List<ActivityQuantityTrackingResponse> trackingItems = new ArrayList<>();
        double totalPlanned = 0.0;
        double totalToday = 0.0;
        double totalCum = 0.0;
        double totalBal = 0.0;
        int overExecutedCount = 0;

        if (dpr.getActivityItems() != null) {
            for (DprActivityItem item : dpr.getActivityItems()) {
                ActivityQuantityTrackingResponse tracking = buildActivityQuantityTrackingFromItem(item, dpr.getReportDate());
                trackingItems.add(tracking);

                totalPlanned += (tracking.getPlannedQuantity() != null ? tracking.getPlannedQuantity() : 0.0);
                totalToday += (tracking.getQuantityToday() != null ? tracking.getQuantityToday() : 0.0);
                totalCum += (tracking.getCumulativeQuantity() != null ? tracking.getCumulativeQuantity() : 0.0);
                totalBal += (tracking.getBalanceQuantity() != null ? tracking.getBalanceQuantity() : 0.0);
                if (Boolean.TRUE.equals(tracking.getIsOverExecuted())) {
                    overExecutedCount++;
                }
            }
        }

        double overallPct = totalPlanned > 0.0
                ? Math.min(100.0, Math.round((totalCum / totalPlanned) * 10000.0) / 100.0)
                : (trackingItems.isEmpty() ? 0.0 : 100.0);

        return DprQuantityTrackingSummaryResponse.builder()
                .dprId(dpr.getId())
                .reportCode(dpr.getReportCode())
                .projectId(dpr.getProject() != null ? dpr.getProject().getId() : null)
                .projectName(dpr.getProject() != null ? dpr.getProject().getName() : null)
                .reportDate(dpr.getReportDate())
                .shift(dpr.getShift())
                .status(dpr.getStatus())
                .totalActivitiesLogged(trackingItems.size())
                .totalPlannedVolume(Math.round(totalPlanned * 100.0) / 100.0)
                .totalQuantityToday(Math.round(totalToday * 100.0) / 100.0)
                .totalCumulativeQuantity(Math.round(totalCum * 100.0) / 100.0)
                .totalBalanceQuantity(Math.round(totalBal * 100.0) / 100.0)
                .overallProgressPercentage(overallPct)
                .overExecutedActivitiesCount(overExecutedCount)
                .activityQuantities(trackingItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityQuantityTrackingResponse getActivityQuantityTracking(Long activityId) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        LocalDate today = LocalDate.now();
        double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
        double cum = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
        double bal = Math.max(0.0, planned - cum);
        boolean isOver = cum > planned && planned > 0.0;
        double overQty = isOver ? Math.round((cum - planned) * 100.0) / 100.0 : 0.0;
        double progressPct = planned > 0.0
                ? Math.min(100.0, Math.round((cum / planned) * 10000.0) / 100.0)
                : (cum > 0.0 ? 100.0 : 0.0);

        Long daysRemaining = null;
        if (act.getPlannedEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(today, act.getPlannedEndDate());
        }

        Double requiredDailyRunRate = null;
        if (daysRemaining != null && daysRemaining > 0 && bal > 0.0) {
            requiredDailyRunRate = Math.round((bal / daysRemaining) * 100.0) / 100.0;
        } else if (bal == 0.0) {
            requiredDailyRunRate = 0.0;
        }

        Double actualDailyAverageRate = null;
        LocalDate startDate = act.getActualStartDate() != null ? act.getActualStartDate() : act.getPlannedStartDate();
        if (startDate != null && !today.isBefore(startDate)) {
            long daysElapsed = Math.max(1, ChronoUnit.DAYS.between(startDate, today) + 1);
            actualDailyAverageRate = Math.round((cum / daysElapsed) * 100.0) / 100.0;
        }

        String execStatus = computeExecutionStatus(act.getStatus(), progressPct, isOver, daysRemaining, requiredDailyRunRate, actualDailyAverageRate);

        List<DprActivityItem> historyItems = dprActivityItemRepository.findByActivityId(activityId);
        List<DprActivityItemResponse> historyResponses = historyItems.stream()
                .map(this::mapActivityItem)
                .collect(Collectors.toList());

        return ActivityQuantityTrackingResponse.builder()
                .activityId(act.getId())
                .activityCode(act.getCode())
                .activityName(act.getName())
                .discipline(act.getDiscipline())
                .uom(act.getUom())
                .plannedQuantity(planned)
                .previousQuantity(cum)
                .quantityToday(0.0)
                .cumulativeQuantity(cum)
                .balanceQuantity(Math.round(bal * 100.0) / 100.0)
                .progressPercentage(progressPct)
                .isOverExecuted(isOver)
                .overExecutionQuantity(overQty)
                .status(act.getStatus())
                .executionStatus(execStatus)
                .plannedStartDate(act.getPlannedStartDate())
                .plannedEndDate(act.getPlannedEndDate())
                .actualStartDate(act.getActualStartDate())
                .actualEndDate(act.getActualEndDate())
                .daysRemaining(daysRemaining)
                .requiredDailyRunRate(requiredDailyRunRate)
                .actualDailyAverageRate(actualDailyAverageRate)
                .assignedContractor(act.getAssignedContractor())
                .progressHistory(historyResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectQuantityTrackingResponse getProjectQuantityTracking(Long projectId, String discipline, String status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);

        if (discipline != null && !discipline.isBlank()) {
            activities = activities.stream()
                    .filter(a -> discipline.equalsIgnoreCase(a.getDiscipline()))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isBlank()) {
            activities = activities.stream()
                    .filter(a -> status.equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.toList());
        }

        LocalDate today = LocalDate.now();
        List<ActivityQuantityTrackingResponse> activityTrackings = new ArrayList<>();
        int completedCount = 0;
        int inProgressCount = 0;
        int plannedCount = 0;
        int overExecutedCount = 0;
        double totalPlanned = 0.0;
        double totalCompleted = 0.0;
        double totalBalance = 0.0;

        Map<String, List<WbsActivity>> byDiscipline = activities.stream()
                .collect(Collectors.groupingBy(a -> a.getDiscipline() != null ? a.getDiscipline().toUpperCase() : "GENERAL"));

        List<QuantityDisciplineRollup> disciplineRollups = new ArrayList<>();
        for (Map.Entry<String, List<WbsActivity>> entry : byDiscipline.entrySet()) {
            String discName = entry.getKey();
            List<WbsActivity> discActs = entry.getValue();

            int dCompleted = 0;
            int dInProgress = 0;
            double dPlanned = 0.0;
            double dCompletedQty = 0.0;
            double dBalance = 0.0;

            for (WbsActivity act : discActs) {
                double p = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
                double c = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
                double b = Math.max(0.0, p - c);

                dPlanned += p;
                dCompletedQty += c;
                dBalance += b;

                if ("COMPLETED".equalsIgnoreCase(act.getStatus()) || (p > 0.0 && c >= p)) {
                    dCompleted++;
                } else if ("IN_PROGRESS".equalsIgnoreCase(act.getStatus()) || c > 0.0) {
                    dInProgress++;
                }
            }

            double dPct = dPlanned > 0.0
                    ? Math.min(100.0, Math.round((dCompletedQty / dPlanned) * 10000.0) / 100.0)
                    : (discActs.isEmpty() ? 0.0 : 100.0);

            disciplineRollups.add(QuantityDisciplineRollup.builder()
                    .discipline(discName)
                    .activityCount(discActs.size())
                    .completedCount(dCompleted)
                    .inProgressCount(dInProgress)
                    .totalPlannedQuantity(Math.round(dPlanned * 100.0) / 100.0)
                    .totalCompletedQuantity(Math.round(dCompletedQty * 100.0) / 100.0)
                    .totalBalanceQuantity(Math.round(dBalance * 100.0) / 100.0)
                    .overallProgressPercentage(dPct)
                    .build());
        }

        // Sort discipline rollups alphabetically
        disciplineRollups.sort(Comparator.comparing(QuantityDisciplineRollup::getDiscipline));

        for (WbsActivity act : activities) {
            double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;
            double cum = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
            double bal = Math.max(0.0, planned - cum);
            boolean isOver = cum > planned && planned > 0.0;
            double overQty = isOver ? Math.round((cum - planned) * 100.0) / 100.0 : 0.0;
            double progressPct = planned > 0.0
                    ? Math.min(100.0, Math.round((cum / planned) * 10000.0) / 100.0)
                    : (cum > 0.0 ? 100.0 : 0.0);

            if ("COMPLETED".equalsIgnoreCase(act.getStatus()) || (planned > 0.0 && cum >= planned)) {
                completedCount++;
            } else if ("IN_PROGRESS".equalsIgnoreCase(act.getStatus()) || cum > 0.0) {
                inProgressCount++;
            } else {
                plannedCount++;
            }

            if (isOver) {
                overExecutedCount++;
            }

            totalPlanned += planned;
            totalCompleted += cum;
            totalBalance += bal;

            Long daysRemaining = null;
            if (act.getPlannedEndDate() != null) {
                daysRemaining = ChronoUnit.DAYS.between(today, act.getPlannedEndDate());
            }

            Double requiredDailyRunRate = null;
            if (daysRemaining != null && daysRemaining > 0 && bal > 0.0) {
                requiredDailyRunRate = Math.round((bal / daysRemaining) * 100.0) / 100.0;
            }

            Double actualDailyAverageRate = null;
            LocalDate startDate = act.getActualStartDate() != null ? act.getActualStartDate() : act.getPlannedStartDate();
            if (startDate != null && !today.isBefore(startDate)) {
                long daysElapsed = Math.max(1, ChronoUnit.DAYS.between(startDate, today) + 1);
                actualDailyAverageRate = Math.round((cum / daysElapsed) * 100.0) / 100.0;
            }

            String execStatus = computeExecutionStatus(act.getStatus(), progressPct, isOver, daysRemaining, requiredDailyRunRate, actualDailyAverageRate);

            activityTrackings.add(ActivityQuantityTrackingResponse.builder()
                    .activityId(act.getId())
                    .activityCode(act.getCode())
                    .activityName(act.getName())
                    .discipline(act.getDiscipline())
                    .uom(act.getUom())
                    .plannedQuantity(planned)
                    .previousQuantity(cum)
                    .quantityToday(0.0)
                    .cumulativeQuantity(cum)
                    .balanceQuantity(Math.round(bal * 100.0) / 100.0)
                    .progressPercentage(progressPct)
                    .isOverExecuted(isOver)
                    .overExecutionQuantity(overQty)
                    .status(act.getStatus())
                    .executionStatus(execStatus)
                    .plannedStartDate(act.getPlannedStartDate())
                    .plannedEndDate(act.getPlannedEndDate())
                    .actualStartDate(act.getActualStartDate())
                    .actualEndDate(act.getActualEndDate())
                    .daysRemaining(daysRemaining)
                    .requiredDailyRunRate(requiredDailyRunRate)
                    .actualDailyAverageRate(actualDailyAverageRate)
                    .assignedContractor(act.getAssignedContractor())
                    .build());
        }

        double overallProjectPct = totalPlanned > 0.0
                ? Math.min(100.0, Math.round((totalCompleted / totalPlanned) * 10000.0) / 100.0)
                : (activities.isEmpty() ? 0.0 : 100.0);

        return ProjectQuantityTrackingResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .totalActivities(activities.size())
                .completedActivities(completedCount)
                .inProgressActivities(inProgressCount)
                .plannedActivities(plannedCount)
                .overExecutedActivities(overExecutedCount)
                .totalPlannedQuantity(Math.round(totalPlanned * 100.0) / 100.0)
                .totalCompletedQuantity(Math.round(totalCompleted * 100.0) / 100.0)
                .totalBalanceQuantity(Math.round(totalBalance * 100.0) / 100.0)
                .overallQuantityProgressPercentage(overallProjectPct)
                .disciplineRollups(disciplineRollups)
                .activities(activityTrackings)
                .build();
    }

    private ActivityQuantityTrackingResponse buildActivityQuantityTrackingFromItem(DprActivityItem item, LocalDate reportDate) {
        WbsActivity act = item.getActivity();
        double planned = (act != null && act.getPlannedQuantity() != null) ? act.getPlannedQuantity() : 0.0;
        double today = item.getQuantityToday() != null ? item.getQuantityToday() : 0.0;
        double cum = item.getCumulativeQuantity() != null ? item.getCumulativeQuantity() : today;
        double prev = Math.max(0.0, cum - today);
        double bal = Math.max(0.0, planned - cum);
        boolean isOver = cum > planned && planned > 0.0;
        double overQty = isOver ? Math.round((cum - planned) * 100.0) / 100.0 : 0.0;
        double progressPct = planned > 0.0
                ? Math.min(100.0, Math.round((cum / planned) * 10000.0) / 100.0)
                : 100.0;

        Long daysRemaining = null;
        if (act != null && act.getPlannedEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(reportDate != null ? reportDate : LocalDate.now(), act.getPlannedEndDate());
        }

        Double requiredDailyRunRate = null;
        if (daysRemaining != null && daysRemaining > 0 && bal > 0.0) {
            requiredDailyRunRate = Math.round((bal / daysRemaining) * 100.0) / 100.0;
        }

        Double actualDailyAverageRate = null;
        LocalDate startDate = act != null && act.getActualStartDate() != null ? act.getActualStartDate() : (act != null ? act.getPlannedStartDate() : null);
        if (startDate != null && reportDate != null && !reportDate.isBefore(startDate)) {
            long daysElapsed = Math.max(1, ChronoUnit.DAYS.between(startDate, reportDate) + 1);
            actualDailyAverageRate = Math.round((cum / daysElapsed) * 100.0) / 100.0;
        }

        String execStatus = computeExecutionStatus(
                act != null ? act.getStatus() : "IN_PROGRESS",
                progressPct, isOver, daysRemaining, requiredDailyRunRate, actualDailyAverageRate);

        return ActivityQuantityTrackingResponse.builder()
                .activityId(act != null ? act.getId() : null)
                .activityCode(act != null ? act.getCode() : null)
                .activityName(act != null ? act.getName() : null)
                .discipline(act != null ? act.getDiscipline() : null)
                .uom(act != null ? act.getUom() : null)
                .plannedQuantity(planned)
                .previousQuantity(Math.round(prev * 100.0) / 100.0)
                .quantityToday(today)
                .cumulativeQuantity(cum)
                .balanceQuantity(Math.round(bal * 100.0) / 100.0)
                .progressPercentage(progressPct)
                .isOverExecuted(isOver)
                .overExecutionQuantity(overQty)
                .status(act != null ? act.getStatus() : null)
                .executionStatus(execStatus)
                .plannedStartDate(act != null ? act.getPlannedStartDate() : null)
                .plannedEndDate(act != null ? act.getPlannedEndDate() : null)
                .actualStartDate(act != null ? act.getActualStartDate() : null)
                .actualEndDate(act != null ? act.getActualEndDate() : null)
                .daysRemaining(daysRemaining)
                .requiredDailyRunRate(requiredDailyRunRate)
                .actualDailyAverageRate(actualDailyAverageRate)
                .assignedContractor(act != null ? act.getAssignedContractor() : null)
                .locationReference(item.getLocationReference())
                .build();
    }

    private String computeExecutionStatus(
            String status, double progressPct, boolean isOverExecuted,
            Long daysRemaining, Double requiredDailyRunRate, Double actualDailyAverageRate) {
        if ("COMPLETED".equalsIgnoreCase(status) || progressPct >= 100.0) {
            return "COMPLETED";
        }
        if (isOverExecuted) {
            return "OVER_EXECUTED";
        }
        if (daysRemaining != null && daysRemaining <= 0) {
            return "BEHIND";
        }
        if (requiredDailyRunRate != null && actualDailyAverageRate != null && requiredDailyRunRate > 0) {
            if (actualDailyAverageRate < requiredDailyRunRate * 0.8) {
                return "BEHIND";
            }
            if (actualDailyAverageRate >= requiredDailyRunRate * 1.1) {
                return "AHEAD";
            }
        }
        return "ON_TRACK";
    }

    // =========================================================================
    // 4. OTHER LINE ITEMS MANAGEMENT
    // =========================================================================

    @Override
    @Transactional
    public DailyProgressReportResponse addLaborItem(Long dprId, DprLaborItemRequest request) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        DprLaborItem item = DprLaborItem.builder()
                .dpr(dpr)
                .tradeCategory(request.getTradeCategory())
                .headcount(request.getHeadcount())
                .hoursWorked(request.getHoursWorked() != null ? request.getHoursWorked() : 8.0)
                .subcontractorName(request.getSubcontractorName())
                .remarks(request.getRemarks())
                .build();

        dpr.getLaborItems().add(item);
        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse removeLaborItem(Long dprId, Long itemId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        boolean removed = dpr.getLaborItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("DprLaborItem", "id", itemId);
        }
        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse addEquipmentItem(Long dprId, DprEquipmentItemRequest request) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        DprEquipmentItem item = DprEquipmentItem.builder()
                .dpr(dpr)
                .equipmentName(request.getEquipmentName())
                .equipmentCode(request.getEquipmentCode())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .operatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : 0.0)
                .idleHours(request.getIdleHours() != null ? request.getIdleHours() : 0.0)
                .breakdownHours(request.getBreakdownHours() != null ? request.getBreakdownHours() : 0.0)
                .fuelConsumedLiters(request.getFuelConsumedLiters())
                .remarks(request.getRemarks())
                .build();

        dpr.getEquipmentItems().add(item);
        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse removeEquipmentItem(Long dprId, Long itemId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        boolean removed = dpr.getEquipmentItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("DprEquipmentItem", "id", itemId);
        }
        dpr.recalculateTotals();
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse addMaterialItem(Long dprId, DprMaterialItemRequest request) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        DprMaterialItem item = DprMaterialItem.builder()
                .dpr(dpr)
                .materialName(request.getMaterialName())
                .unitOfMeasure(request.getUnitOfMeasure())
                .quantityReceived(request.getQuantityReceived() != null ? request.getQuantityReceived() : 0.0)
                .quantityConsumed(request.getQuantityConsumed() != null ? request.getQuantityConsumed() : 0.0)
                .supplierName(request.getSupplierName())
                .challanOrInvoiceNumber(request.getChallanOrInvoiceNumber())
                .remarks(request.getRemarks())
                .build();

        dpr.getMaterialItems().add(item);
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse removeMaterialItem(Long dprId, Long itemId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        boolean removed = dpr.getMaterialItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("DprMaterialItem", "id", itemId);
        }
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse addPhotoAttachment(Long dprId, DprPhotoAttachmentRequest request) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        DprPhotoAttachment photo = DprPhotoAttachment.builder()
                .dpr(dpr)
                .photoUrl(request.getPhotoUrl())
                .caption(request.getCaption())
                .category(request.getCategory() != null ? request.getCategory() : "PROGRESS")
                .takenAt(request.getTakenAt() != null ? request.getTakenAt() : LocalDateTime.now())
                .build();

        dpr.getPhotoAttachments().add(photo);
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse removePhotoAttachment(Long dprId, Long attachmentId) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));
        assertEditable(dpr);

        boolean removed = dpr.getPhotoAttachments().removeIf(item -> item.getId().equals(attachmentId));
        if (!removed) {
            throw new ResourceNotFoundException("DprPhotoAttachment", "id", attachmentId);
        }
        DailyProgressReport saved = dprRepository.save(dpr);
        return mapToResponse(saved);
    }

    // =========================================================================
    // 5. WORKFLOW TRANSITIONS (DRAFT, SUBMIT, WITHDRAW, APPROVE, REJECT)
    // =========================================================================

    @Override
    @Transactional
    public DailyProgressReportResponse submitDpr(Long dprId, String submitter) {
        return submitDpr(dprId, null, submitter);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse submitDpr(Long dprId, SubmitDprRequest request, String submitter) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        if (dpr.getStatus() == DprStatus.APPROVED) {
            throw new BadRequestException("DPR is already approved.");
        }
        if (dpr.getStatus() == DprStatus.SUBMITTED) {
            throw new BadRequestException("DPR is already in SUBMITTED state waiting for review.");
        }

        dpr.setStatus(DprStatus.SUBMITTED);
        dpr.setSubmittedBy(submitter != null && !submitter.isBlank() ? submitter : (dpr.getSiteEngineer() != null ? dpr.getSiteEngineer() : "SITE_ENGINEER"));
        dpr.setSubmissionDate(LocalDateTime.now());
        if (request != null && request.getSubmissionNotes() != null) {
            dpr.setSubmissionNotes(request.getSubmissionNotes());
        }
        // Clear prior rejection status upon fresh re-submission
        dpr.setRejectionReason(null);
        dpr.setRejectedBy(null);
        dpr.setRejectionDate(null);

        if (submitter != null && !submitter.isBlank() && dpr.getSiteEngineer() == null) {
            dpr.setSiteEngineer(submitter);
        }
        dpr.recalculateTotals();

        DailyProgressReport saved = dprRepository.save(dpr);

        try {
            auditService.logAction(
                    submitter != null ? submitter : "SYSTEM",
                    "SUBMIT_DPR",
                    "DailyProgressReport",
                    String.valueOf(saved.getId()),
                    "Submitted DPR " + saved.getReportCode() + " for approval",
                    null
            );
        } catch (Exception ex) {
            log.warn("Failed to log audit for DPR submit: {}", ex.getMessage());
        }

        log.info("DPR {} (ID: {}) submitted for managerial approval by {}", saved.getReportCode(), saved.getId(), submitter);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse withdrawDpr(Long dprId, String user) {
        return withdrawDpr(dprId, null, user);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse withdrawDpr(Long dprId, WithdrawDprRequest request, String user) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        if (dpr.getStatus() != DprStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED DPRs can be withdrawn back to DRAFT. Current status: " + dpr.getStatus());
        }

        dpr.setStatus(DprStatus.DRAFT);
        if (request != null && request.getWithdrawalReason() != null) {
            String note = " [Withdrawn on " + LocalDate.now() + " by " + user + ": " + request.getWithdrawalReason() + "]";
            dpr.setGeneralNotes(dpr.getGeneralNotes() != null ? dpr.getGeneralNotes() + note : note);
        }
        dpr.recalculateTotals();

        DailyProgressReport saved = dprRepository.save(dpr);

        try {
            auditService.logAction(
                    user != null ? user : "SYSTEM",
                    "WITHDRAW_DPR",
                    "DailyProgressReport",
                    String.valueOf(saved.getId()),
                    "Withdrew DPR " + saved.getReportCode() + " back to DRAFT",
                    null
            );
        } catch (Exception ex) {
            log.warn("Failed to log audit for DPR withdraw: {}", ex.getMessage());
        }

        log.info("DPR {} (ID: {}) withdrawn back to DRAFT by {}", saved.getReportCode(), saved.getId(), user);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse approveDpr(Long dprId, String approver) {
        return approveDpr(dprId, null, approver);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse approveDpr(Long dprId, ApproveDprRequest request, String approver) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        if (dpr.getStatus() == DprStatus.APPROVED) {
            throw new BadRequestException("DPR is already approved.");
        }
        if (dpr.getStatus() == DprStatus.REJECTED) {
            throw new BadRequestException("Cannot approve a REJECTED DPR. Please resubmit the DPR for review first.");
        }

        dpr.setStatus(DprStatus.APPROVED);
        dpr.setApprovedBy(approver != null ? approver : "PROJECT_MANAGER");
        dpr.setApprovalDate(LocalDateTime.now());
        if (request != null && request.getApprovalNotes() != null) {
            dpr.setApprovalNotes(request.getApprovalNotes());
        }
        dpr.setRejectionReason(null);
        dpr.setRejectedBy(null);
        dpr.setRejectionDate(null);
        dpr.recalculateTotals();

        // CASCADE PROGRESS TO WBS ACTIVITIES & GENERATE ACTIVITY PROGRESS LOGS
        if (dpr.getActivityItems() != null && !dpr.getActivityItems().isEmpty()) {
            for (DprActivityItem item : dpr.getActivityItems()) {
                WbsActivity act = item.getActivity();
                double currentCompleted = act.getCompletedQuantity() != null ? act.getCompletedQuantity() : 0.0;
                double newCompleted = currentCompleted + item.getQuantityToday();
                double planned = act.getPlannedQuantity() != null ? act.getPlannedQuantity() : 0.0;

                double newProgressPct = planned > 0.0
                        ? Math.min(100.0, Math.round((newCompleted / planned) * 10000.0) / 100.0)
                        : 100.0;
                double todayPct = planned > 0.0
                        ? Math.round((item.getQuantityToday() / planned) * 10000.0) / 100.0
                        : 0.0;

                item.setCumulativeQuantity(newCompleted);
                item.setProgressPercentageToday(todayPct);
                item.setCumulativeProgressPercentage(newProgressPct);

                act.setCompletedQuantity(newCompleted);
                act.setProgressPercentage(newProgressPct);

                if (act.getActualStartDate() == null) {
                    act.setActualStartDate(dpr.getReportDate());
                }

                if (newProgressPct >= 100.0) {
                    act.setStatus("COMPLETED");
                    if (act.getActualEndDate() == null) {
                        act.setActualEndDate(dpr.getReportDate());
                    }
                } else if ("PLANNED".equalsIgnoreCase(act.getStatus())) {
                    act.setStatus("IN_PROGRESS");
                }

                wbsActivityRepository.save(act);

                String equipmentSummary = (dpr.getEquipmentItems() != null && !dpr.getEquipmentItems().isEmpty())
                        ? dpr.getEquipmentItems().stream()
                        .map(e -> e.getEquipmentName() + " (" + e.getOperatingHours() + "h)")
                        .collect(Collectors.joining(", "))
                        : null;

                ActivityProgressLog progressLog = ActivityProgressLog.builder()
                        .activity(act)
                        .logDate(dpr.getReportDate())
                        .quantityCompletedToday(item.getQuantityToday())
                        .cumulativeQuantityCompleted(newCompleted)
                        .cumulativeProgressPercentage(newProgressPct)
                        .manpowerCount(dpr.getTotalManpowerCount())
                        .equipmentUsed(equipmentSummary)
                        .siteHindranceNotes(dpr.getSiteHindrances())
                        .weatherCondition(dpr.getWeatherCondition() != null ? dpr.getWeatherCondition().name() : null)
                        .recordedBy(approver != null ? approver : (dpr.getSiteEngineer() != null ? dpr.getSiteEngineer() : "SYSTEM"))
                        .isBlocker(item.getIsBlocker())
                        .blockerCategory(item.getBlockerCategory() != null ? item.getBlockerCategory().name() : null)
                        .blockerReason(item.getBlockerReason())
                        .remarks(item.getRemarks())
                        .source("DPR")
                        .dprId(dpr.getId())
                        .dprReportCode(dpr.getReportCode())
                        .build();

                activityProgressLogRepository.save(progressLog);
            }
        }

        DailyProgressReport saved = dprRepository.save(dpr);

        try {
            auditService.logAction(
                    approver != null ? approver : "SYSTEM",
                    "APPROVE_DPR",
                    "DailyProgressReport",
                    String.valueOf(saved.getId()),
                    "Approved DPR " + saved.getReportCode() + " and cascaded quantities to WBS activities",
                    null
            );
        } catch (Exception ex) {
            log.warn("Failed to log audit for DPR approve: {}", ex.getMessage());
        }

        log.info("DPR {} (ID: {}) approved by {}. Physical quantities cascaded to WBS.",
                saved.getReportCode(), saved.getId(), approver);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyProgressReportResponse rejectDpr(Long dprId, RejectDprRequest request, String reviewer) {
        DailyProgressReport dpr = dprRepository.findById(dprId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyProgressReport", "id", dprId));

        if (dpr.getStatus() == DprStatus.APPROVED) {
            throw new BadRequestException("Approved DPR cannot be rejected. Please reverse through change management.");
        }
        if (dpr.getStatus() == DprStatus.DRAFT) {
            throw new BadRequestException("Draft DPR has not been submitted yet and cannot be rejected.");
        }
        if (dpr.getStatus() == DprStatus.REJECTED) {
            throw new BadRequestException("DPR is already in REJECTED status.");
        }

        dpr.setStatus(DprStatus.REJECTED);
        dpr.setRejectionReason(request.getRejectionReason());
        dpr.setRejectedBy(reviewer != null ? reviewer : "PROJECT_MANAGER");
        dpr.setRejectionDate(LocalDateTime.now());

        DailyProgressReport saved = dprRepository.save(dpr);

        try {
            auditService.logAction(
                    reviewer != null ? reviewer : "SYSTEM",
                    "REJECT_DPR",
                    "DailyProgressReport",
                    String.valueOf(saved.getId()),
                    "Rejected DPR " + saved.getReportCode() + ". Reason: " + request.getRejectionReason(),
                    null
            );
        } catch (Exception ex) {
            log.warn("Failed to log audit for DPR reject: {}", ex.getMessage());
        }

        log.info("DPR {} (ID: {}) rejected by {}. Reason: {}", saved.getReportCode(), saved.getId(), reviewer, request.getRejectionReason());
        return mapToResponse(saved);
    }

    // =========================================================================
    // 6. EXECUTIVE SUMMARY & KPIS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DprSummaryResponse getDprSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Long totalReports = dprRepository.countByProjectId(projectId);
        Long draftReports = dprRepository.countByProjectIdAndStatus(projectId, DprStatus.DRAFT);
        Long submittedReports = dprRepository.countByProjectIdAndStatus(projectId, DprStatus.SUBMITTED);
        Long approvedReports = dprRepository.countByProjectIdAndStatus(projectId, DprStatus.APPROVED);
        Long rejectedReports = dprRepository.countByProjectIdAndStatus(projectId, DprStatus.REJECTED);

        Long cumulativeManpower = dprRepository.sumManpowerByProjectId(projectId);
        Long totalSafetyIncidents = dprRepository.sumSafetyIncidentsByProjectId(projectId);

        DailyProgressReport latest = dprRepository.findTopByProjectIdOrderByReportDateDesc(projectId).orElse(null);
        Long totalBlockers = dprActivityItemRepository.countByDprProjectIdAndIsBlockerTrue(projectId);
        Long openBlockers = dprActivityItemRepository.countOpenBlockersByProjectId(projectId);

        return DprSummaryResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalReports(totalReports != null ? totalReports : 0L)
                .draftReports(draftReports != null ? draftReports : 0L)
                .submittedReports(submittedReports != null ? submittedReports : 0L)
                .approvedReports(approvedReports != null ? approvedReports : 0L)
                .rejectedReports(rejectedReports != null ? rejectedReports : 0L)
                .cumulativeManpowerLogged(cumulativeManpower != null ? cumulativeManpower : 0L)
                .totalSafetyIncidents(totalSafetyIncidents != null ? totalSafetyIncidents : 0L)
                .totalBlockersLogged(totalBlockers != null ? totalBlockers : 0L)
                .openBlockersCount(openBlockers != null ? openBlockers : 0L)
                .latestReportDate(latest != null ? latest.getReportDate() : null)
                .latestReportStatus(latest != null && latest.getStatus() != null ? latest.getStatus().name() : null)
                .build();
    }

    // =========================================================================
    // HELPER METHODS & MAPPERS
    // =========================================================================

    private void assertEditable(DailyProgressReport dpr) {
        if (dpr.getStatus() == DprStatus.APPROVED) {
            throw new BadRequestException("Cannot modify items on an APPROVED Daily Progress Report.");
        }
        if (dpr.getStatus() == DprStatus.SUBMITTED) {
            throw new BadRequestException("Cannot modify items on a SUBMITTED DPR pending review.");
        }
    }

    private DailyProgressReportResponse mapToResponse(DailyProgressReport dpr) {
        List<DprActivityItemResponse> actResponses = (dpr.getActivityItems() != null)
                ? dpr.getActivityItems().stream().map(this::mapActivityItem).collect(Collectors.toList())
                : new ArrayList<>();

        List<DprLaborItemResponse> laborResponses = (dpr.getLaborItems() != null)
                ? dpr.getLaborItems().stream().map(this::mapLaborItem).collect(Collectors.toList())
                : new ArrayList<>();

        List<DprEquipmentItemResponse> equipResponses = (dpr.getEquipmentItems() != null)
                ? dpr.getEquipmentItems().stream().map(this::mapEquipmentItem).collect(Collectors.toList())
                : new ArrayList<>();

        List<DprMaterialItemResponse> matResponses = (dpr.getMaterialItems() != null)
                ? dpr.getMaterialItems().stream().map(this::mapMaterialItem).collect(Collectors.toList())
                : new ArrayList<>();

        List<DprPhotoAttachmentResponse> photoResponses = (dpr.getPhotoAttachments() != null)
                ? dpr.getPhotoAttachments().stream().map(this::mapPhotoAttachment).collect(Collectors.toList())
                : new ArrayList<>();

        return DailyProgressReportResponse.builder()
                .id(dpr.getId())
                .reportCode(dpr.getReportCode())
                .projectId(dpr.getProject() != null ? dpr.getProject().getId() : null)
                .projectName(dpr.getProject() != null ? dpr.getProject().getName() : null)
                .projectCode(dpr.getProject() != null ? dpr.getProject().getCode() : null)
                .reportDate(dpr.getReportDate())
                .shift(dpr.getShift())
                .weatherCondition(dpr.getWeatherCondition())
                .temperatureCelsius(dpr.getTemperatureCelsius())
                .status(dpr.getStatus())
                .siteEngineer(dpr.getSiteEngineer())
                .submittedBy(dpr.getSubmittedBy())
                .submissionDate(dpr.getSubmissionDate())
                .submissionNotes(dpr.getSubmissionNotes())
                .approvedBy(dpr.getApprovedBy())
                .approvalDate(dpr.getApprovalDate())
                .approvalNotes(dpr.getApprovalNotes())
                .rejectedBy(dpr.getRejectedBy())
                .rejectionDate(dpr.getRejectionDate())
                .rejectionReason(dpr.getRejectionReason())
                .generalNotes(dpr.getGeneralNotes())
                .safetyIncidentsCount(dpr.getSafetyIncidentsCount())
                .safetyNotes(dpr.getSafetyNotes())
                .qualityObservations(dpr.getQualityObservations())
                .siteHindrances(dpr.getSiteHindrances())
                .totalManpowerCount(dpr.getTotalManpowerCount())
                .totalEquipmentCount(dpr.getTotalEquipmentCount())
                .totalActivitiesLogged(dpr.getTotalActivitiesLogged())
                .totalBlockersCount(dpr.getTotalBlockersCount())
                .openBlockersCount(dpr.getOpenBlockersCount())
                .createdAt(dpr.getCreatedAt())
                .updatedAt(dpr.getUpdatedAt())
                .activityItems(actResponses)
                .laborItems(laborResponses)
                .equipmentItems(equipResponses)
                .materialItems(matResponses)
                .photoAttachments(photoResponses)
                .build();
    }

    private DprActivityItemResponse mapActivityItem(DprActivityItem item) {
        WbsActivity act = item.getActivity();
        double planned = (act != null && act.getPlannedQuantity() != null) ? act.getPlannedQuantity() : 0.0;
        double today = item.getQuantityToday() != null ? item.getQuantityToday() : 0.0;
        double cum = item.getCumulativeQuantity() != null ? item.getCumulativeQuantity() : today;
        double prev = Math.max(0.0, cum - today);
        double bal = Math.max(0.0, planned - cum);
        boolean isOver = cum > planned && planned > 0.0;

        return DprActivityItemResponse.builder()
                .id(item.getId())
                .activityId(act != null ? act.getId() : null)
                .activityCode(act != null ? act.getCode() : null)
                .activityName(act != null ? act.getName() : null)
                .discipline(act != null ? act.getDiscipline() : null)
                .uom(act != null ? act.getUom() : null)
                .plannedQuantity(planned)
                .previousQuantity(Math.round(prev * 100.0) / 100.0)
                .quantityToday(today)
                .cumulativeQuantity(cum)
                .balanceQuantity(Math.round(bal * 100.0) / 100.0)
                .progressPercentageToday(item.getProgressPercentageToday())
                .cumulativeProgressPercentage(item.getCumulativeProgressPercentage())
                .isOverExecuted(isOver)
                .locationReference(item.getLocationReference())
                .remarks(item.getRemarks())
                .isBlocker(item.getIsBlocker())
                .blockerCategory(item.getBlockerCategory())
                .blockerReason(item.getBlockerReason())
                .blockerSeverity(item.getBlockerSeverity())
                .blockerStatus(item.getBlockerStatus())
                .impactHours(item.getImpactHours())
                .blockerResolutionNotes(item.getBlockerResolutionNotes())
                .blockerResolvedBy(item.getBlockerResolvedBy())
                .blockerResolvedAt(item.getBlockerResolvedAt())
                .build();
    }

    private DprBlockerItemResponse mapToBlockerResponse(DprActivityItem item) {
        DailyProgressReport dpr = item.getDpr();
        WbsActivity act = item.getActivity();

        return DprBlockerItemResponse.builder()
                .itemId(item.getId())
                .dprId(dpr != null ? dpr.getId() : null)
                .reportCode(dpr != null ? dpr.getReportCode() : null)
                .reportDate(dpr != null ? dpr.getReportDate() : null)
                .shift(dpr != null ? dpr.getShift() : null)
                .dprStatus(dpr != null ? dpr.getStatus() : null)
                .siteEngineer(dpr != null ? dpr.getSiteEngineer() : null)
                .activityId(act != null ? act.getId() : null)
                .activityCode(act != null ? act.getCode() : null)
                .activityName(act != null ? act.getName() : null)
                .discipline(act != null ? act.getDiscipline() : null)
                .locationReference(item.getLocationReference())
                .isBlocker(item.getIsBlocker())
                .blockerCategory(item.getBlockerCategory())
                .blockerSeverity(item.getBlockerSeverity())
                .blockerStatus(item.getBlockerStatus())
                .blockerReason(item.getBlockerReason())
                .impactHours(item.getImpactHours())
                .remarks(item.getRemarks())
                .blockerResolutionNotes(item.getBlockerResolutionNotes())
                .blockerResolvedBy(item.getBlockerResolvedBy())
                .blockerResolvedAt(item.getBlockerResolvedAt())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private DprLaborItemResponse mapLaborItem(DprLaborItem item) {
        return DprLaborItemResponse.builder()
                .id(item.getId())
                .tradeCategory(item.getTradeCategory())
                .headcount(item.getHeadcount())
                .hoursWorked(item.getHoursWorked())
                .subcontractorName(item.getSubcontractorName())
                .remarks(item.getRemarks())
                .build();
    }

    private DprEquipmentItemResponse mapEquipmentItem(DprEquipmentItem item) {
        return DprEquipmentItemResponse.builder()
                .id(item.getId())
                .equipmentName(item.getEquipmentName())
                .equipmentCode(item.getEquipmentCode())
                .quantity(item.getQuantity())
                .operatingHours(item.getOperatingHours())
                .idleHours(item.getIdleHours())
                .breakdownHours(item.getBreakdownHours())
                .fuelConsumedLiters(item.getFuelConsumedLiters())
                .remarks(item.getRemarks())
                .build();
    }

    private DprMaterialItemResponse mapMaterialItem(DprMaterialItem item) {
        return DprMaterialItemResponse.builder()
                .id(item.getId())
                .materialName(item.getMaterialName())
                .unitOfMeasure(item.getUnitOfMeasure())
                .quantityReceived(item.getQuantityReceived())
                .quantityConsumed(item.getQuantityConsumed())
                .supplierName(item.getSupplierName())
                .challanOrInvoiceNumber(item.getChallanOrInvoiceNumber())
                .remarks(item.getRemarks())
                .build();
    }

    private DprPhotoAttachmentResponse mapPhotoAttachment(DprPhotoAttachment item) {
        return DprPhotoAttachmentResponse.builder()
                .id(item.getId())
                .photoUrl(item.getPhotoUrl())
                .caption(item.getCaption())
                .category(item.getCategory())
                .takenAt(item.getTakenAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}

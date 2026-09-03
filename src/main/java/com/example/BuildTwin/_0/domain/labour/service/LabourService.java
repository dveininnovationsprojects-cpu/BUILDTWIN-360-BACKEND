package com.example.BuildTwin._0.domain.labour.service;

import com.example.BuildTwin._0.domain.labour.dto.*;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.model.LabourAllocation;
import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourAllocationRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabourService {

    private final LabourDailyRepository labourDailyRepository;
    private final ContractorRepository contractorRepository;
    private final LabourAllocationRepository labourAllocationRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public LabourDaily recordDailyLabour(LabourDailyRecordDto dto) {
        Contractor contractor = contractorRepository.findById(dto.getContractorId())
                .orElseThrow(() -> new ResourceNotFoundException("Contractor", "id", dto.getContractorId()));

        LabourDaily record = LabourDaily.builder()
                .recordDate(dto.getRecordDate())
                .projectId(dto.getProjectId())
                .siteId(dto.getSiteId())
                .contractor(contractor)
                .tradeCategory(dto.getTradeCategory())
                .headcount(dto.getHeadcount())
                .standardHours(dto.getStandardHours())
                .overtimeHours(dto.getOvertimeHours())
                .remarks(dto.getRemarks())
                .allocations(new ArrayList<>())
                .build();

        if (dto.getAllocations() != null && !dto.getAllocations().isEmpty()) {
            dto.getAllocations().forEach(allocDto -> {
                LabourAllocation allocation = LabourAllocation.builder()
                        .labourDaily(record)
                        .wbsActivityId(allocDto.getActivityId())
                        .hoursAllocated(allocDto.getHoursAllocated())
                        .activityDescription(allocDto.getActivityDescription())
                        .build();
                record.getAllocations().add(allocation);
            });
        }

        return labourDailyRepository.save(record);
    }

    @Transactional(readOnly = true)
    public LabourDaily getLabourRecordById(Long id) {
        return labourDailyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LabourDailyRecord", "id", id));
    }

    @Transactional
    public LabourDaily updateDailyLabour(Long id, LabourDailyRecordDto dto) {
        LabourDaily existing = getLabourRecordById(id);
        Contractor contractor = contractorRepository.findById(dto.getContractorId())
                .orElseThrow(() -> new ResourceNotFoundException("Contractor", "id", dto.getContractorId()));

        existing.setRecordDate(dto.getRecordDate());
        existing.setProjectId(dto.getProjectId());
        existing.setSiteId(dto.getSiteId());
        existing.setContractor(contractor);
        existing.setTradeCategory(dto.getTradeCategory());
        existing.setHeadcount(dto.getHeadcount());
        existing.setStandardHours(dto.getStandardHours());
        existing.setOvertimeHours(dto.getOvertimeHours());
        existing.setRemarks(dto.getRemarks());

        existing.getAllocations().clear();
        if (dto.getAllocations() != null && !dto.getAllocations().isEmpty()) {
            dto.getAllocations().forEach(allocDto -> {
                LabourAllocation allocation = LabourAllocation.builder()
                        .labourDaily(existing)
                        .wbsActivityId(allocDto.getActivityId())
                        .hoursAllocated(allocDto.getHoursAllocated())
                        .activityDescription(allocDto.getActivityDescription())
                        .build();
                existing.getAllocations().add(allocation);
            });
        }

        return labourDailyRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<LabourDaily> getLabourRecordsByProject(Long projectId) {
        return labourDailyRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<LabourDaily> getLabourRecordsByContractor(Long contractorId) {
        return labourDailyRepository.findByContractorId(contractorId);
    }

    @Transactional(readOnly = true)
    public List<LabourDaily> getDailyRecordsByProjectAndDate(Long projectId, LocalDate date) {
        return labourDailyRepository.findByProjectIdAndRecordDate(projectId, date);
    }

    @Transactional(readOnly = true)
    public List<LabourAllocation> getLabourAllocationsByActivity(Long activityId) {
        return labourAllocationRepository.findByWbsActivityId(activityId);
    }

    @Transactional(readOnly = true)
    public Integer getTotalHeadcount(Long projectId, LocalDate date) {
        Integer total = labourDailyRepository.getTotalHeadcountForProjectAndDate(projectId, date);
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public LabourHourSummaryDto getLabourHourSummary(Long projectId, Long contractorId, LocalDate startDate, LocalDate endDate) {
        List<LabourDaily> records;
        if (contractorId != null && startDate != null && endDate != null) {
            records = labourDailyRepository.findByContractorIdAndRecordDateBetween(contractorId, startDate, endDate);
        } else if (projectId != null && startDate != null && endDate != null) {
            records = labourDailyRepository.findByProjectIdAndRecordDateBetween(projectId, startDate, endDate);
        } else if (contractorId != null) {
            records = labourDailyRepository.findByContractorId(contractorId);
        } else if (projectId != null) {
            records = labourDailyRepository.findByProjectId(projectId);
        } else {
            records = labourDailyRepository.findAll();
        }

        int totalHeadcount = records.stream().mapToInt(r -> r.getHeadcount() != null ? r.getHeadcount() : 0).sum();
        BigDecimal stdHours = records.stream()
                .map(r -> r.getStandardHours() != null ? r.getStandardHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal otHours = records.stream()
                .map(r -> r.getOvertimeHours() != null ? r.getOvertimeHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHours = stdHours.add(otHours);

        return LabourHourSummaryDto.builder()
                .projectId(projectId)
                .contractorId(contractorId)
                .totalHeadcount(totalHeadcount)
                .totalStandardHours(stdHours)
                .totalOvertimeHours(otHours)
                .totalLabourHours(totalHours)
                .totalRecordsLogged(records.size())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductivityResponseDto calculateActivityProductivity(Long activityId, BigDecimal overrideCompletedQty) {
        Activity activity = activityRepository.findById(activityId)
                .orElse(null);

        String activityName = activity != null ? activity.getName() : "WBS Activity #" + activityId;
        String unit = activity != null && activity.getUnit() != null ? activity.getUnit() : "unit";
        BigDecimal completedQty = overrideCompletedQty != null ? overrideCompletedQty
                : (activity != null && activity.getPlannedQty() != null ? activity.getPlannedQty() : BigDecimal.valueOf(100));

        List<LabourAllocation> allocations = labourAllocationRepository.findByWbsActivityId(activityId);
        BigDecimal totalHours = allocations.stream()
                .map(LabourAllocation::getHoursAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHours.compareTo(BigDecimal.ZERO) == 0) {
            totalHours = BigDecimal.valueOf(1.0); // avoid division by zero default
        }

        BigDecimal outputPerLabourHour = completedQty.divide(totalHours, 4, RoundingMode.HALF_UP);
        BigDecimal manHoursPerUnit = completedQty.compareTo(BigDecimal.ZERO) > 0
                ? totalHours.divide(completedQty, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String status = outputPerLabourHour.compareTo(BigDecimal.valueOf(0.1)) > 0 ? "OPTIMAL" : "NEEDS_IMPROVEMENT";

        return ProductivityResponseDto.builder()
                .activityId(activityId)
                .activityName(activityName)
                .unit(unit)
                .completedQuantity(completedQty)
                .totalLabourHours(totalHours)
                .outputPerLabourHour(outputPerLabourHour)
                .manHoursPerUnit(manHoursPerUnit)
                .productivityStatus(status)
                .build();
    }

    @Transactional(readOnly = true)
    public ProductivityResponseDto calculateCustomProductivity(ProductivityRequestDto request) {
        BigDecimal completedQty = request.getCompletedQuantity();
        BigDecimal hours = request.getLabourHours();
        String unit = request.getUnit() != null ? request.getUnit() : "unit";

        BigDecimal outputPerLabourHour = completedQty.divide(hours, 4, RoundingMode.HALF_UP);
        BigDecimal manHoursPerUnit = hours.divide(completedQty, 4, RoundingMode.HALF_UP);

        String status = outputPerLabourHour.compareTo(BigDecimal.valueOf(0.1)) > 0 ? "OPTIMAL" : "NEEDS_IMPROVEMENT";

        return ProductivityResponseDto.builder()
                .activityId(request.getActivityId())
                .activityName(request.getActivityId() != null ? "Activity #" + request.getActivityId() : "Custom Activity Task")
                .unit(unit)
                .completedQuantity(completedQty)
                .totalLabourHours(hours)
                .outputPerLabourHour(outputPerLabourHour)
                .manHoursPerUnit(manHoursPerUnit)
                .productivityStatus(status)
                .build();
    }
}

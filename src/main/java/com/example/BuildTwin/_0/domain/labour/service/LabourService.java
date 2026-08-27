package com.example.BuildTwin._0.domain.labour.service;

import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.labour.dto.LabourDailyRecordDto;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabourService {

    private final LabourDailyRepository labourDailyRepository;
    private final ContractorRepository contractorRepository;

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
                .build();

        return labourDailyRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<LabourDaily> getDailyRecordsByProjectAndDate(Long projectId, LocalDate date) {
        return labourDailyRepository.findByProjectIdAndRecordDate(projectId, date);
    }

    @Transactional(readOnly = true)
    public Integer getTotalHeadcount(Long projectId, LocalDate date) {
        Integer total = labourDailyRepository.getTotalHeadcountForProjectAndDate(projectId, date);
        return total != null ? total : 0;
    }
}

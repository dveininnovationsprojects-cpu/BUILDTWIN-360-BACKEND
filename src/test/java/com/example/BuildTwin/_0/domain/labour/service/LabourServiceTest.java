package com.example.BuildTwin._0.domain.labour.service;

import com.example.BuildTwin._0.domain.labour.dto.*;
import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.model.LabourAllocation;
import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourAllocationRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabourServiceTest {

    @Mock
    private LabourDailyRepository labourDailyRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private LabourAllocationRepository labourAllocationRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private LabourService labourService;

    private Contractor contractor;
    private LabourDaily labourDaily;
    private LabourDailyRecordDto recordDto;

    @BeforeEach
    void setUp() {
        contractor = Contractor.builder()
                .id(1L)
                .contractorCode("CON-101")
                .name("Ramesh Kumar")
                .companyName("Ramesh Electricals")
                .tradeSpecialization(TradeCategory.ELECTRICIAN)
                .build();

        LabourAllocationDto allocDto = LabourAllocationDto.builder()
                .activityId(101L)
                .hoursAllocated(BigDecimal.valueOf(8.0))
                .activityDescription("Wiring conduit laying")
                .build();

        recordDto = LabourDailyRecordDto.builder()
                .recordDate(LocalDate.now())
                .projectId(1L)
                .siteId(10L)
                .contractorId(1L)
                .tradeCategory(TradeCategory.ELECTRICIAN)
                .headcount(5)
                .standardHours(BigDecimal.valueOf(40.0))
                .overtimeHours(BigDecimal.valueOf(5.0))
                .remarks("Normal working day")
                .allocations(List.of(allocDto))
                .build();

        labourDaily = LabourDaily.builder()
                .id(1L)
                .recordDate(LocalDate.now())
                .projectId(1L)
                .siteId(10L)
                .contractor(contractor)
                .tradeCategory(TradeCategory.ELECTRICIAN)
                .headcount(5)
                .standardHours(BigDecimal.valueOf(40.0))
                .overtimeHours(BigDecimal.valueOf(5.0))
                .allocations(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Record Daily Labour - Success")
    void recordDailyLabour_Success() {
        when(contractorRepository.findById(1L)).thenReturn(Optional.of(contractor));
        when(labourDailyRepository.save(any(LabourDaily.class))).thenReturn(labourDaily);

        LabourDaily created = labourService.recordDailyLabour(recordDto);

        assertThat(created).isNotNull();
        assertThat(created.getHeadcount()).isEqualTo(5);
        verify(labourDailyRepository, times(1)).save(any(LabourDaily.class));
    }

    @Test
    @DisplayName("Get Labour Hour Summary - Success")
    void getLabourHourSummary_Success() {
        when(labourDailyRepository.findByProjectId(1L)).thenReturn(List.of(labourDaily));

        LabourHourSummaryDto summary = labourService.getLabourHourSummary(1L, null, null, null);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalHeadcount()).isEqualTo(5);
        assertThat(summary.getTotalStandardHours()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
        assertThat(summary.getTotalOvertimeHours()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }

    @Test
    @DisplayName("Calculate Activity Productivity - Success")
    void calculateActivityProductivity_Success() {
        Activity activity = Activity.builder()
                .id(101L)
                .name("Concreting")
                .unit("m3")
                .plannedQty(BigDecimal.valueOf(100.0))
                .build();

        LabourAllocation alloc = LabourAllocation.builder()
                .wbsActivityId(101L)
                .hoursAllocated(BigDecimal.valueOf(50.0))
                .build();

        when(activityRepository.findById(101L)).thenReturn(Optional.of(activity));
        when(labourAllocationRepository.findByWbsActivityId(101L)).thenReturn(List.of(alloc));

        ProductivityResponseDto prod = labourService.calculateActivityProductivity(101L, null);

        assertThat(prod).isNotNull();
        assertThat(prod.getActivityId()).isEqualTo(101L);
        assertThat(prod.getUnit()).isEqualTo("m3");
        assertThat(prod.getOutputPerLabourHour()).isEqualByComparingTo(BigDecimal.valueOf(2.0000));
        assertThat(prod.getManHoursPerUnit()).isEqualByComparingTo(BigDecimal.valueOf(0.5000));
    }

    @Test
    @DisplayName("Calculate Custom Productivity - Success")
    void calculateCustomProductivity_Success() {
        ProductivityRequestDto req = ProductivityRequestDto.builder()
                .activityId(101L)
                .unit("sqm")
                .completedQuantity(BigDecimal.valueOf(200.0))
                .labourHours(BigDecimal.valueOf(50.0))
                .build();

        ProductivityResponseDto prod = labourService.calculateCustomProductivity(req);

        assertThat(prod).isNotNull();
        assertThat(prod.getOutputPerLabourHour()).isEqualByComparingTo(BigDecimal.valueOf(4.0000));
        assertThat(prod.getManHoursPerUnit()).isEqualByComparingTo(BigDecimal.valueOf(0.2500));
    }
}

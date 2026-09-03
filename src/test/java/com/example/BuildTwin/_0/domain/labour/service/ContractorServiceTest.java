package com.example.BuildTwin._0.domain.labour.service;

import com.example.BuildTwin._0.domain.labour.dto.ContractorPerformanceSummaryDto;
import com.example.BuildTwin._0.domain.labour.dto.ContractorRequestDto;
import com.example.BuildTwin._0.domain.labour.dto.TradeDto;
import com.example.BuildTwin._0.domain.labour.enums.ContractorType;
import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractorServiceTest {

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private LabourDailyRepository labourDailyRepository;

    @InjectMocks
    private ContractorService contractorService;

    private Contractor contractor;
    private ContractorRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ContractorRequestDto.builder()
                .contractorCode("CON-101")
                .name("Ramesh Kumar")
                .companyName("Ramesh Electricals")
                .tradeSpecialization(TradeCategory.ELECTRICIAN)
                .contactNumber("+919876543210")
                .email("ramesh@electricals.com")
                .address("Padur, OMR, Chennai")
                .contractorType(ContractorType.MAIN_CONTRACTOR)
                .status("ACTIVE")
                .build();

        contractor = Contractor.builder()
                .id(1L)
                .contractorCode("CON-101")
                .name("Ramesh Kumar")
                .companyName("Ramesh Electricals")
                .tradeSpecialization(TradeCategory.ELECTRICIAN)
                .contactNumber("+919876543210")
                .email("ramesh@electricals.com")
                .address("Padur, OMR, Chennai")
                .contractorType(ContractorType.MAIN_CONTRACTOR)
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Create Contractor - Success")
    void createContractor_Success() {
        when(contractorRepository.existsByContractorCode("CON-101")).thenReturn(false);
        when(contractorRepository.save(any(Contractor.class))).thenReturn(contractor);

        Contractor created = contractorService.createContractor(requestDto);

        assertThat(created).isNotNull();
        assertThat(created.getContractorCode()).isEqualTo("CON-101");
        verify(contractorRepository, times(1)).save(any(Contractor.class));
    }

    @Test
    @DisplayName("Create Contractor - Duplicate Code Throws Exception")
    void createContractor_DuplicateCode_ThrowsException() {
        when(contractorRepository.existsByContractorCode("CON-101")).thenReturn(true);

        assertThatThrownBy(() -> contractorService.createContractor(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Contractor already exists with code: CON-101");
    }

    @Test
    @DisplayName("Get Contractor By ID - Success")
    void getContractorById_Success() {
        when(contractorRepository.findById(1L)).thenReturn(Optional.of(contractor));

        Contractor found = contractorService.getContractorById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Get Contractor By ID - Not Found Throws Exception")
    void getContractorById_NotFound_ThrowsException() {
        when(contractorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractorService.getContractorById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get Subcontractors - Success")
    void getSubcontractors_Success() {
        Contractor sub = Contractor.builder()
                .id(2L)
                .contractorCode("SUB-201")
                .name("Suresh Plumbing")
                .companyName("Suresh Subcontracting")
                .tradeSpecialization(TradeCategory.PLUMBER)
                .contractorType(ContractorType.SUBCONTRACTOR)
                .parentContractorId(1L)
                .status("ACTIVE")
                .build();

        when(contractorRepository.findByContractorType(ContractorType.SUBCONTRACTOR))
                .thenReturn(List.of(sub));

        List<Contractor> subs = contractorService.getSubcontractors();

        assertThat(subs).hasSize(1);
        assertThat(subs.get(0).getContractorType()).isEqualTo(ContractorType.SUBCONTRACTOR);
    }

    @Test
    @DisplayName("Get Trade Categories - Returns All Trades")
    void getTradeCategories_ReturnsAllTrades() {
        List<TradeDto> trades = contractorService.getTradeCategories();

        assertThat(trades).isNotEmpty();
        assertThat(trades.size()).isEqualTo(TradeCategory.values().length);
    }

    @Test
    @DisplayName("Get Contractor Performance Summary - Success")
    void getContractorPerformanceSummary_Success() {
        when(contractorRepository.findById(1L)).thenReturn(Optional.of(contractor));
        when(labourDailyRepository.findByContractorId(1L)).thenReturn(Collections.emptyList());

        ContractorPerformanceSummaryDto summary = contractorService.getContractorPerformanceSummary(1L);

        assertThat(summary).isNotNull();
        assertThat(summary.getContractorId()).isEqualTo(1L);
        assertThat(summary.getContractorCode()).isEqualTo("CON-101");
        assertThat(summary.getQualityPassRatePercentage()).isNotNull();
    }
}

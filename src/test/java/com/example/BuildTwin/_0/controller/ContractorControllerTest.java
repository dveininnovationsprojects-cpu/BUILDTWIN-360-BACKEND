package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.labour.dto.ContractorPerformanceSummaryDto;
import com.example.BuildTwin._0.domain.labour.enums.ContractorType;
import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.service.ContractorService;
import com.example.BuildTwin._0.security.CustomUserDetailsService;
import com.example.BuildTwin._0.security.JwtAuthenticationEntryPoint;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContractorController.class)
class ContractorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContractorService contractorService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/contractors - List All Contractors Success")
    void getAllContractors_Success() throws Exception {
        Contractor c = Contractor.builder()
                .id(1L)
                .contractorCode("CON-101")
                .name("Ramesh")
                .companyName("Ramesh Civil")
                .tradeSpecialization(TradeCategory.MASON)
                .status("ACTIVE")
                .build();

        when(contractorService.getAllContractors()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/v1/contractors").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].contractorCode").value("CON-101"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/contractors/subcontractors - List Subcontractors Success")
    void getSubcontractors_Success() throws Exception {
        Contractor sub = Contractor.builder()
                .id(2L)
                .contractorCode("SUB-101")
                .name("Suresh")
                .companyName("Suresh Sub")
                .tradeSpecialization(TradeCategory.PLUMBER)
                .contractorType(ContractorType.SUBCONTRACTOR)
                .status("ACTIVE")
                .build();

        when(contractorService.getSubcontractors()).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/v1/contractors/subcontractors").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].contractorCode").value("SUB-101"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/contractors/1/performance-summary - Performance Summary Success")
    void getContractorPerformanceSummary_Success() throws Exception {
        ContractorPerformanceSummaryDto dto = ContractorPerformanceSummaryDto.builder()
                .contractorId(1L)
                .contractorCode("CON-101")
                .contractorName("Ramesh")
                .qualityPassRatePercentage(BigDecimal.valueOf(95.5))
                .overallPerformanceRating("HIGH_PERFORMER")
                .build();

        when(contractorService.getContractorPerformanceSummary(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/contractors/1/performance-summary").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractorCode").value("CON-101"))
                .andExpect(jsonPath("$.data.overallPerformanceRating").value("HIGH_PERFORMER"));
    }
}

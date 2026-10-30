package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.labour.dto.ContractorPerformanceSummaryDto;
import com.example.BuildTwin._0.domain.labour.dto.ContractorRequestDto;
import com.example.BuildTwin._0.domain.labour.dto.TradeDto;
import com.example.BuildTwin._0.domain.labour.enums.ContractorType;
import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.service.ContractorService;
import com.example.BuildTwin._0.security.CustomUserDetailsService;
import com.example.BuildTwin._0.security.JwtAuthenticationEntryPoint;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContractorController.class)
class ContractorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/v1/contractors - Create Contractor Profile Success")
    void createContractor_Success() throws Exception {
        ContractorRequestDto request = ContractorRequestDto.builder()
                .contractorCode("CON-101")
                .name("Ramesh Kumar")
                .companyName("Ramesh Civil Works")
                .tradeSpecialization(TradeCategory.MASON)
                .contactNumber("+919876543210")
                .email("ramesh@civilworks.com")
                .address("Padur, OMR, Chennai")
                .contractorType(ContractorType.MAIN_CONTRACTOR)
                .status("ACTIVE")
                .build();

        Contractor created = Contractor.builder()
                .id(1L)
                .contractorCode(request.getContractorCode())
                .name(request.getName())
                .companyName(request.getCompanyName())
                .tradeSpecialization(request.getTradeSpecialization())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .contractorType(request.getContractorType())
                .status("ACTIVE")
                .build();

        when(contractorService.createContractor(any(ContractorRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/contractors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.contractorCode").value("CON-101"));
    }

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
    @DisplayName("GET /api/v1/contractors/{id} - Get Contractor By ID Success")
    void getContractorById_Success() throws Exception {
        Contractor c = Contractor.builder()
                .id(1L)
                .contractorCode("CON-101")
                .name("Ramesh")
                .companyName("Ramesh Civil")
                .tradeSpecialization(TradeCategory.MASON)
                .status("ACTIVE")
                .build();

        when(contractorService.getContractorById(1L)).thenReturn(c);

        mockMvc.perform(get("/api/v1/contractors/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.contractorCode").value("CON-101"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/contractors/code/{code} - Get Contractor By Code Success")
    void getContractorByCode_Success() throws Exception {
        Contractor c = Contractor.builder()
                .id(1L)
                .contractorCode("CON-101")
                .name("Ramesh")
                .companyName("Ramesh Civil")
                .tradeSpecialization(TradeCategory.MASON)
                .status("ACTIVE")
                .build();

        when(contractorService.getContractorByCode("CON-101")).thenReturn(c);

        mockMvc.perform(get("/api/v1/contractors/code/CON-101").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractorCode").value("CON-101"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("PUT /api/v1/contractors/{id} - Update Contractor Profile Success")
    void updateContractor_Success() throws Exception {
        ContractorRequestDto request = ContractorRequestDto.builder()
                .contractorCode("CON-101")
                .name("Ramesh Kumar Updated")
                .companyName("Ramesh Civil Works Pvt Ltd")
                .tradeSpecialization(TradeCategory.MASON)
                .contactNumber("+919876543210")
                .email("ramesh.updated@civilworks.com")
                .address("Padur, OMR, Chennai")
                .contractorType(ContractorType.MAIN_CONTRACTOR)
                .status("ACTIVE")
                .build();

        Contractor updated = Contractor.builder()
                .id(1L)
                .contractorCode(request.getContractorCode())
                .name(request.getName())
                .companyName(request.getCompanyName())
                .tradeSpecialization(request.getTradeSpecialization())
                .status("ACTIVE")
                .build();

        when(contractorService.updateContractor(eq(1L), any(ContractorRequestDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/contractors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ramesh Kumar Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/contractors/{id} - Deactivate Contractor Success")
    void deleteContractor_Success() throws Exception {
        doNothing().when(contractorService).deleteContractor(1L);

        mockMvc.perform(delete("/api/v1/contractors/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
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
    @DisplayName("GET /api/v1/contractors/{id}/subcontractors - Get Subcontractors By Parent Success")
    void getSubcontractorsByParent_Success() throws Exception {
        Contractor sub = Contractor.builder()
                .id(2L)
                .contractorCode("SUB-101")
                .name("Suresh")
                .companyName("Suresh Sub")
                .parentContractorId(1L)
                .tradeSpecialization(TradeCategory.PLUMBER)
                .contractorType(ContractorType.SUBCONTRACTOR)
                .status("ACTIVE")
                .build();

        when(contractorService.getSubcontractorsByParent(1L)).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/v1/contractors/1/subcontractors").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].contractorCode").value("SUB-101"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/contractors/trades - Get Available Trade Categories Success")
    void getTradeCategories_Success() throws Exception {
        TradeDto trade = TradeDto.builder()
                .trade(TradeCategory.MASON)
                .displayName("Masonry & Brickwork")
                .description("Blockwork, plastering, screed")
                .build();

        when(contractorService.getTradeCategories()).thenReturn(List.of(trade));

        mockMvc.perform(get("/api/v1/contractors/trades").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].trade").value("MASON"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/contractors/trade/{trade} - Get Contractors By Trade Success")
    void getContractorsByTrade_Success() throws Exception {
        Contractor c = Contractor.builder()
                .id(1L)
                .contractorCode("CON-101")
                .name("Ramesh")
                .companyName("Ramesh Civil")
                .tradeSpecialization(TradeCategory.MASON)
                .status("ACTIVE")
                .build();

        when(contractorService.getContractorsByTrade(TradeCategory.MASON)).thenReturn(List.of(c));

        mockMvc.perform(get("/api/v1/contractors/trade/MASON").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tradeSpecialization").value("MASON"));
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

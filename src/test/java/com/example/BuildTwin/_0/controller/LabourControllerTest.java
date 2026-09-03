package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.labour.dto.LabourHourSummaryDto;
import com.example.BuildTwin._0.domain.labour.dto.ProductivityResponseDto;
import com.example.BuildTwin._0.domain.labour.service.LabourService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LabourController.class)
class LabourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabourService labourService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/labour/hours-summary - Hours Tracking Summary Success")
    void getLabourHourSummary_Success() throws Exception {
        LabourHourSummaryDto dto = LabourHourSummaryDto.builder()
                .projectId(1L)
                .totalHeadcount(25)
                .totalStandardHours(BigDecimal.valueOf(200.0))
                .totalOvertimeHours(BigDecimal.valueOf(30.0))
                .totalLabourHours(BigDecimal.valueOf(230.0))
                .build();

        when(labourService.getLabourHourSummary(1L, null, null, null)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/labour/hours-summary")
                        .param("projectId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalHeadcount").value(25))
                .andExpect(jsonPath("$.data.totalLabourHours").value(230.0));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/labour/productivity/activity/101 - Productivity Calculation Success")
    void getProductivityForActivity_Success() throws Exception {
        ProductivityResponseDto dto = ProductivityResponseDto.builder()
                .activityId(101L)
                .activityName("RCC Slab")
                .unit("m3")
                .completedQuantity(BigDecimal.valueOf(100.0))
                .totalLabourHours(BigDecimal.valueOf(50.0))
                .outputPerLabourHour(BigDecimal.valueOf(2.0))
                .manHoursPerUnit(BigDecimal.valueOf(0.5))
                .productivityStatus("OPTIMAL")
                .build();

        when(labourService.calculateActivityProductivity(101L, null)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/labour/productivity/activity/101").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(101))
                .andExpect(jsonPath("$.data.outputPerLabourHour").value(2.0))
                .andExpect(jsonPath("$.data.productivityStatus").value("OPTIMAL"));
    }
}

package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.reports.dto.*;
import com.example.BuildTwin._0.domain.reports.service.ReportService;
import com.example.BuildTwin._0.security.CustomUserDetailsService;
import com.example.BuildTwin._0.security.JwtAuthenticationEntryPoint;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = "DIRECTOR")
    @DisplayName("GET /api/v1/reports/weekly/{projectId} - Success")
    void getWeeklyProjectReport_Success() throws Exception {
        WeeklyProjectReportDto dto = WeeklyProjectReportDto.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur - Padur, Chennai")
                .projectCode("PADUR-AG-01")
                .weekNumber(10)
                .generatedAt(LocalDateTime.now())
                .plannedProgressPercentage(new BigDecimal("85.00"))
                .actualProgressPercentage(new BigDecimal("82.50"))
                .scheduleStatus("ON_SCHEDULE")
                .totalHeadcount(45)
                .totalStandardHours(new BigDecimal("360.00"))
                .totalOvertimeHours(new BigDecimal("40.00"))
                .totalLabourCost(new BigDecimal("85000.00"))
                .materialsTrackedCount(15)
                .lowStockAlertsCount(1)
                .totalMaterialConsumptionCost(new BigDecimal("1250000.00"))
                .totalPurchaseOrdersCount(6)
                .committedProcurementCost(new BigDecimal("3200000.00"))
                .incurredGrnCost(new BigDecimal("1800000.00"))
                .outstandingLiability(new BigDecimal("1400000.00"))
                .baselineBudget(new BigDecimal("45000000.00"))
                .actualExpense(new BigDecimal("12500000.00"))
                .costVariance(new BigDecimal("2500000.00"))
                .costPerformanceIndex(new BigDecimal("1.08"))
                .schedulePerformanceIndex(new BigDecimal("1.02"))
                .estimateAtCompletion(new BigDecimal("41666666.67"))
                .costHealthStatus("UNDER_BUDGET")
                .openSnagsCount(2L)
                .activeNcrsCount(1L)
                .criticalBlockersCount(0L)
                .totalSiteDelayDays(0)
                .unreadAlertsCount(3L)
                .build();

        when(reportService.generateWeeklyProjectReport(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reports/weekly/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectName").value("Ashok Grandeur - Padur, Chennai"))
                .andExpect(jsonPath("$.data.weekNumber").value(10))
                .andExpect(jsonPath("$.data.scheduleStatus").value("ON_SCHEDULE"))
                .andExpect(jsonPath("$.data.totalHeadcount").value(45));
    }

    @Test
    @WithMockUser(roles = "PROCUREMENT_STORE")
    @DisplayName("GET /api/v1/reports/inventory/{projectId} - Success")
    void getInventoryReport_Success() throws Exception {
        InventoryReportDto dto = InventoryReportDto.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur")
                .generatedAt(LocalDateTime.now())
                .totalMaterialsCataloged(25)
                .totalInventoryValuation(new BigDecimal("5400000.00"))
                .lowStockItemsCount(2)
                .totalConsumedQuantity(new BigDecimal("1200.00"))
                .totalConsumptionCost(new BigDecimal("850000.00"))
                .criticalLowStockItems(List.of(
                        InventoryReportDto.LowStockItemDto.builder()
                                .materialId(1L)
                                .materialCode("MAT-CEM-01")
                                .materialName("UltraTech Cement 53")
                                .unit("BAGS")
                                .currentStock(new BigDecimal("250.00"))
                                .reorderLevel(new BigDecimal("500.00"))
                                .standardRate(new BigDecimal("380.00"))
                                .stockDeficit(new BigDecimal("250.00"))
                                .build()
                ))
                .build();

        when(reportService.generateInventoryReport(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reports/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMaterialsCataloged").value(25))
                .andExpect(jsonPath("$.data.lowStockItemsCount").value(2))
                .andExpect(jsonPath("$.data.criticalLowStockItems[0].materialCode").value("MAT-CEM-01"));
    }

    @Test
    @WithMockUser(roles = "QUANTITY_COST_COORDINATOR")
    @DisplayName("GET /api/v1/reports/procurement-labour/{projectId} - Success")
    void getProcurementLabourReport_Success() throws Exception {
        ProcurementLabourReportDto dto = ProcurementLabourReportDto.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur")
                .generatedAt(LocalDateTime.now())
                .totalPurchaseOrders(12)
                .openCommitmentOrdersCount(4)
                .totalCommittedAmount(new BigDecimal("4500000.00"))
                .totalIncurredGrnCost(new BigDecimal("2800000.00"))
                .outstandingLiability(new BigDecimal("1700000.00"))
                .totalGrnReceiptsLogged(18)
                .activeContractorsCount(5)
                .totalHeadcountDeployed(68)
                .totalStandardHours(new BigDecimal("544.00"))
                .totalOvertimeHours(new BigDecimal("60.00"))
                .totalLabourWageCost(new BigDecimal("145000.00"))
                .totalDailyAttendanceLogs(35)
                .build();

        when(reportService.generateProcurementLabourReport(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reports/procurement-labour/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPurchaseOrders").value(12))
                .andExpect(jsonPath("$.data.totalHeadcountDeployed").value(68));
    }

    @Test
    @WithMockUser(roles = "QUALITY_ENGINEER")
    @DisplayName("GET /api/v1/reports/cost-quality/{projectId} - Success")
    void getCostQualityReport_Success() throws Exception {
        CostQualityReportDto dto = CostQualityReportDto.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur")
                .generatedAt(LocalDateTime.now())
                .baselineBudget(new BigDecimal("45000000.00"))
                .revisedBudget(new BigDecimal("45000000.00"))
                .actualCostIncurred(new BigDecimal("12000000.00"))
                .committedCost(new BigDecimal("3000000.00"))
                .projectedTotalCost(new BigDecimal("15000000.00"))
                .netBudgetVariance(new BigDecimal("30000000.00"))
                .budgetStatus("UNDER_BUDGET")
                .activeBudgetOverrunsCount(0)
                .totalOverrunAmount(BigDecimal.ZERO)
                .costPerformanceIndex(new BigDecimal("1.05"))
                .schedulePerformanceIndex(new BigDecimal("1.00"))
                .estimateAtCompletion(new BigDecimal("42857142.86"))
                .varianceAtCompletion(new BigDecimal("2142857.14"))
                .totalIssuesReported(8L)
                .openSnagsCount(2L)
                .activeNcrsCount(1L)
                .criticalBlockersCount(0L)
                .overdueIssuesCount(0L)
                .qualityResolutionPercentage(75.0)
                .totalSiteDelayDays(0)
                .build();

        when(reportService.generateCostQualityReport(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reports/cost-quality/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.budgetStatus").value("UNDER_BUDGET"))
                .andExpect(jsonPath("$.data.totalIssuesReported").value(8));
    }

    @Test
    @WithMockUser(roles = "DATA_ANALYST")
    @DisplayName("GET /api/v1/reports/analytics/{projectId} - Success")
    void getAnalyticsAggregation_Success() throws Exception {
        AnalyticsAggregationDto dto = AnalyticsAggregationDto.builder()
                .projectId(1L)
                .projectName("Ashok Grandeur")
                .projectCode("PADUR-AG-01")
                .generatedAt(LocalDateTime.now())
                .overallHealthStatus("OPTIMAL")
                .compositeHealthScore(92.5)
                .budgetHealth("ON_BUDGET")
                .scheduleHealth("ON_SCHEDULE")
                .inventoryHealth("SUFFICIENT")
                .qualityHealth("PASSING")
                .cpi(new BigDecimal("1.05"))
                .spi(new BigDecimal("1.00"))
                .qualityResolutionRate(85.0)
                .activeBlockersCount(0)
                .unreadCriticalAlertsCount(0)
                .build();

        when(reportService.generateAnalyticsAggregation(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reports/analytics/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallHealthStatus").value("OPTIMAL"))
                .andExpect(jsonPath("$.data.compositeHealthScore").value(92.5));
    }

    @Test
    @DisplayName("GET /api/v1/reports/weekly/1 - Unauthenticated Returns 401")
    void unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/reports/weekly/1"))
                .andExpect(status().isUnauthorized());
    }
}

package com.example.BuildTwin._0.security;

import com.example.BuildTwin._0.controller.CostController;
import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CostController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class Epic4CostRbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CostService costService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ==========================================
    // 1. Unauthenticated Checks (401)
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request to Cost Budget endpoint should return 401 Unauthorized")
    void testUnauthenticatedBudgetAccess() throws Exception {
        mockMvc.perform(get("/api/v1/cost/budgets/project/100"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==========================================
    // 2. All 10 Roles Parameterized Read Access (200 OK)
    // ==========================================

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "DIRECTOR",
            "PROJECT_MANAGER",
            "SITE_ENGINEER",
            "SITE_SUPERVISOR",
            "PROCUREMENT_STORE",
            "QUANTITY_COST_COORDINATOR",
            "QUALITY_ENGINEER",
            "DATA_ANALYST",
            "AUDITOR"
    })
    @DisplayName("All 10 System Roles should successfully view project budgets")
    void testAllTenRolesCanViewProjectBudgets(String role) throws Exception {
        when(costService.getBudgetsByProject(100L)).thenReturn(List.of(
                Budget.builder().id(1L).projectId(100L).costCode("CIV_01").baselineAmount(new BigDecimal("500000.00")).build()
        ));

        mockMvc.perform(get("/api/v1/cost/budgets/project/100")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "DIRECTOR",
            "PROJECT_MANAGER",
            "SITE_ENGINEER",
            "SITE_SUPERVISOR",
            "PROCUREMENT_STORE",
            "QUANTITY_COST_COORDINATOR",
            "QUALITY_ENGINEER",
            "DATA_ANALYST",
            "AUDITOR"
    })
    @DisplayName("All 10 System Roles should successfully view Cost Tracking & EVM Analytics")
    void testAllTenRolesCanViewCostTrackingAndEvm(String role) throws Exception {
        when(costService.getProjectCostTracking(100L)).thenReturn(
                ProjectCostTrackingReportDto.builder().projectId(100L).totalApprovedBudget(new BigDecimal("1000000.00")).build()
        );
        when(costService.calculateEvmMetricsWithProgress(eq(100L), any(), any())).thenReturn(
                EvmMetricsDto.builder().projectId(100L).budgetAtCompletion(new BigDecimal("1000000.00")).spiStatus("ON_SCHEDULE").build()
        );

        mockMvc.perform(get("/api/v1/cost/tracking/project/100")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/cost/evm/project/100")
                        .with(user("testUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 3. Write Permission Enforcements (201 / 200 vs 403)
    // ==========================================

    @Test
    @WithMockUser(roles = "AUDITOR")
    @DisplayName("AUDITOR role should be Forbidden (403) from creating budgets")
    void testAuditorCannotCreateBudget() throws Exception {
        BudgetRequestDto dto = BudgetRequestDto.builder()
                .projectId(100L).costCode("TEST_01").baselineAmount(new BigDecimal("100000.00")).build();

        mockMvc.perform(post("/api/v1/cost/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "SITE_SUPERVISOR")
    @DisplayName("SITE_SUPERVISOR role should be Forbidden (403) from authorizing budget revisions")
    void testSiteSupervisorCannotAuthorizeBudgetRevision() throws Exception {
        BudgetRevisionDto revisionDto = BudgetRevisionDto.builder()
                .revisedAmount(new BigDecimal("1200000.00")).revisionReason("Unauthorized change").build();

        mockMvc.perform(put("/api/v1/cost/budgets/1/revision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revisionDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "QUANTITY_COST_COORDINATOR")
    @DisplayName("QUANTITY_COST_COORDINATOR can create budget successfully (201)")
    void testQuantityCoordinatorCanCreateBudget() throws Exception {
        BudgetRequestDto dto = BudgetRequestDto.builder()
                .projectId(100L).costCode("CIV_RCC").costHead(CostHead.MATERIAL).baselineAmount(new BigDecimal("500000.00")).build();
        Budget created = Budget.builder().id(1L).projectId(100L).costCode("CIV_RCC").baselineAmount(new BigDecimal("500000.00")).build();

        when(costService.createOrUpdateBudget(any(BudgetRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/cost/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "DIRECTOR")
    @DisplayName("DIRECTOR can authorize budget revisions successfully (200)")
    void testDirectorCanAuthorizeRevision() throws Exception {
        BudgetRevisionDto revisionDto = BudgetRevisionDto.builder()
                .revisedAmount(new BigDecimal("600000.00")).revisionReason("Approved by Director").build();
        Budget revised = Budget.builder().id(1L).revisedAmount(new BigDecimal("600000.00")).status("APPROVED_REVISED").build();

        when(costService.authorizeBudgetRevision(eq(1L), any(BudgetRevisionDto.class))).thenReturn(revised);

        mockMvc.perform(put("/api/v1/cost/budgets/1/revision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revisionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("PROJECT_MANAGER can record cost transactions successfully (201)")
    void testProjectManagerCanRecordTransaction() throws Exception {
        CostTransactionRequestDto dto = CostTransactionRequestDto.builder()
                .projectId(100L).costCode("LAB_01").costHead(CostHead.LABOUR).sourceType(CostSourceType.LABOUR_WAGES)
                .amount(new BigDecimal("45000.00")).transactionDate(LocalDate.now()).build();
        CostTransaction saved = CostTransaction.builder().id(5L).projectId(100L).costCode("LAB_01").amount(new BigDecimal("45000.00")).build();

        when(costService.recordCostTransaction(any(CostTransactionRequestDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/cost/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}

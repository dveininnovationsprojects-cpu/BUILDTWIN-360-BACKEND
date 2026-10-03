package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.materials.dto.StockReconciliationDto;
import com.example.BuildTwin._0.domain.materials.dto.StockReconciliationResultDto;
import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.service.StockLedgerService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StockLedgerController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockLedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockLedgerService stockLedgerService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/stock-ledger/transaction - Success")
    void testRecordTransaction() throws Exception {
        StockTransactionDto dto = new StockTransactionDto();
        dto.setProjectId(1L);
        dto.setSiteId(1L);
        dto.setMaterialId(10L);
        dto.setQuantity(new BigDecimal("100.00"));
        dto.setTransactionType(StockTransactionType.RECEIPT);

        StockLedger ledger = StockLedger.builder()
                .id(1L)
                .projectId(1L)
                .siteId(1L)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("100.00"))
                .material(Material.builder().id(10L).name("Cement").build())
                .build();

        when(stockLedgerService.recordTransaction(any(StockTransactionDto.class))).thenReturn(ledger);

        mockMvc.perform(post("/api/v1/stock-ledger/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.transactionType").value("RECEIPT"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-ledger/receipt - Success")
    void testRecordReceiptEndpoint() throws Exception {
        StockTransactionDto dto = new StockTransactionDto();
        dto.setProjectId(1L);
        dto.setMaterialId(10L);
        dto.setQuantity(new BigDecimal("100.00"));
        dto.setRemarks("Opening Balance");

        StockLedger ledger = StockLedger.builder()
                .id(10L)
                .projectId(1L)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("100.00"))
                .remarks("Opening Balance")
                .material(Material.builder().id(10L).name("Cement").build())
                .build();

        when(stockLedgerService.recordReceipt(any(StockTransactionDto.class))).thenReturn(ledger);

        mockMvc.perform(post("/api/v1/stock-ledger/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.transactionType").value("RECEIPT"))
                .andExpect(jsonPath("$.data.remarks").value("Opening Balance"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-ledger/issue - Success")
    void testIssueMaterial() throws Exception {
        StockTransactionDto dto = new StockTransactionDto();
        dto.setProjectId(1L);
        dto.setSiteId(1L);
        dto.setMaterialId(10L);
        dto.setQuantity(new BigDecimal("25.00"));

        StockLedger ledger = StockLedger.builder()
                .id(2L)
                .projectId(1L)
                .siteId(1L)
                .transactionType(StockTransactionType.ISSUE)
                .quantity(new BigDecimal("25.00"))
                .material(Material.builder().id(10L).name("Cement").build())
                .build();

        when(stockLedgerService.issueMaterial(any(StockTransactionDto.class))).thenReturn(ledger);

        mockMvc.perform(post("/api/v1/stock-ledger/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("ISSUE"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-ledger/return - Success")
    void testReturnMaterial() throws Exception {
        StockTransactionDto dto = new StockTransactionDto();
        dto.setProjectId(1L);
        dto.setContractorId(5L);
        dto.setMaterialId(10L);
        dto.setQuantity(new BigDecimal("10.00"));
        dto.setRemarks("Surplus returned from contractor");

        StockLedger ledger = StockLedger.builder()
                .id(5L)
                .projectId(1L)
                .contractorId(5L)
                .transactionType(StockTransactionType.RETURN)
                .quantity(new BigDecimal("10.00"))
                .material(Material.builder().id(10L).name("Cement").build())
                .remarks("Surplus returned from contractor")
                .build();

        when(stockLedgerService.returnMaterial(any(StockTransactionDto.class))).thenReturn(ledger);

        mockMvc.perform(post("/api/v1/stock-ledger/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("RETURN"))
                .andExpect(jsonPath("$.data.quantity").value(10.00));
    }

    @Test
    @DisplayName("POST /api/v1/stock-ledger/consumption - Success")
    void testRecordConsumption() throws Exception {
        StockTransactionDto dto = new StockTransactionDto();
        dto.setProjectId(1L);
        dto.setSiteId(1L);
        dto.setMaterialId(10L);
        dto.setQuantity(new BigDecimal("15.00"));

        StockLedger ledger = StockLedger.builder()
                .id(3L)
                .projectId(1L)
                .siteId(1L)
                .transactionType(StockTransactionType.CONSUMPTION)
                .quantity(new BigDecimal("15.00"))
                .material(Material.builder().id(10L).name("Cement").build())
                .build();

        when(stockLedgerService.recordConsumption(any(StockTransactionDto.class))).thenReturn(ledger);

        mockMvc.perform(post("/api/v1/stock-ledger/consumption")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("CONSUMPTION"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-ledger/wastage - Success")
    void testRecordWastage() throws Exception {
        StockTransactionDto dto = new StockTransactionDto();
        dto.setProjectId(1L);
        dto.setSiteId(1L);
        dto.setMaterialId(10L);
        dto.setQuantity(new BigDecimal("2.00"));

        StockLedger ledger = StockLedger.builder()
                .id(4L)
                .projectId(1L)
                .siteId(1L)
                .transactionType(StockTransactionType.WASTAGE)
                .quantity(new BigDecimal("2.00"))
                .material(Material.builder().id(10L).name("Cement").build())
                .build();

        when(stockLedgerService.recordWastage(any(StockTransactionDto.class))).thenReturn(ledger);

        mockMvc.perform(post("/api/v1/stock-ledger/wastage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("WASTAGE"));
    }

    @Test
    @DisplayName("POST /api/v1/stock-ledger/reconcile - Success")
    void testReconcileStock() throws Exception {
        StockReconciliationDto dto = new StockReconciliationDto();
        dto.setProjectId(1L);
        dto.setSiteId(1L);
        dto.setMaterialId(10L);
        dto.setPhysicalQty(new BigDecimal("95.00"));
        dto.setAuditedBy("lead_auditor");

        StockReconciliationResultDto result = StockReconciliationResultDto.builder()
                .materialId(10L)
                .materialCode("MAT-CEM-01")
                .materialName("Cement")
                .systemStock(new BigDecimal("100.00"))
                .physicalStock(new BigDecimal("95.00"))
                .variance(new BigDecimal("-5.00"))
                .adjustmentTransactionId(101L)
                .auditedBy("lead_auditor")
                .reconciledAt(LocalDateTime.now())
                .build();

        when(stockLedgerService.reconcileStock(any(StockReconciliationDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/stock-ledger/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.variance").value(-5.00))
                .andExpect(jsonPath("$.data.auditedBy").value("lead_auditor"));
    }

    @Test
    @DisplayName("GET /api/v1/stock-ledger/material/{materialId} - Success without filters")
    void testGetLedgerByMaterial() throws Exception {
        StockLedger entry = StockLedger.builder()
                .id(1L)
                .projectId(1L)
                .siteId(1L)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("50.00"))
                .build();

        when(stockLedgerService.getLedgerEntriesByMaterial(eq(10L), any(), any(), any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/stock-ledger/material/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/stock-ledger/material/{materialId} - Success with date range and projectId filters")
    void testGetLedgerByMaterialWithFilters() throws Exception {
        StockLedger entry = StockLedger.builder()
                .id(2L)
                .projectId(100L)
                .siteId(1L)
                .transactionType(StockTransactionType.ISSUE)
                .quantity(new BigDecimal("30.00"))
                .timestamp(LocalDateTime.parse("2026-09-28T10:00:00"))
                .build();

        when(stockLedgerService.getLedgerEntriesByMaterial(eq(10L), eq(100L), any(), any()))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/stock-ledger/material/10")
                        .param("projectId", "100")
                        .param("startDate", "2026-09-01T00:00:00")
                        .param("endDate", "2026-09-28T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(2L))
                .andExpect(jsonPath("$.data[0].projectId").value(100L))
                .andExpect(jsonPath("$.data[0].transactionType").value("ISSUE"));
    }
}

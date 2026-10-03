package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto;
import com.example.BuildTwin._0.domain.procurement.dto.PoFulfillmentDto;
import com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderApprovalDto;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.model.MaterialRequest;
import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import com.example.BuildTwin._0.domain.procurement.service.ProcurementService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcurementController.class)
class ProcurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcurementService procurementService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("POST /api/v1/procurement/requests - Create Material Request Success")
    void createMaterialRequest_Success() throws Exception {
        MaterialRequest req = MaterialRequest.builder()
                .projectId(1L)
                .materialId(10L)
                .requiredQty(new BigDecimal("250.00"))
                .requiredDate(LocalDate.now().plusDays(5))
                .status("PENDING")
                .build();

        MaterialRequest saved = MaterialRequest.builder()
                .id(1L)
                .projectId(1L)
                .materialId(10L)
                .requiredQty(new BigDecimal("250.00"))
                .status("PENDING")
                .build();

        when(procurementService.createMaterialRequest(any(MaterialRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/procurement/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "SITE_ENGINEER")
    @DisplayName("PUT /api/v1/procurement/requests/1 - Update Material Request (e.g. Typo correction) Success")
    void updateMaterialRequest_Success() throws Exception {
        MaterialRequest updatePayload = MaterialRequest.builder()
                .requiredQty(new BigDecimal("250.00"))
                .remarks("Corrected quantity typo from 2500 to 250")
                .build();

        MaterialRequest updated = MaterialRequest.builder()
                .id(1L)
                .projectId(1L)
                .materialId(10L)
                .requiredQty(new BigDecimal("250.00"))
                .remarks("Corrected quantity typo from 2500 to 250")
                .status("PENDING")
                .build();

        when(procurementService.updateMaterialRequest(eq(1L), any(MaterialRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/procurement/requests/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiredQty").value(250.00))
                .andExpect(jsonPath("$.data.remarks").value("Corrected quantity typo from 2500 to 250"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("PUT /api/v1/procurement/requests/1/approval - Approve Material Request Success")
    void updateMaterialRequestApproval_Success() throws Exception {
        MaterialRequestApprovalDto dto = MaterialRequestApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("PM_Selvamani")
                .remarks("Approved for slab casting")
                .build();

        MaterialRequest approved = MaterialRequest.builder()
                .id(1L)
                .status("APPROVED")
                .approvedBy("PM_Selvamani")
                .build();

        when(procurementService.updateMaterialRequestStatus(eq(1L), any(MaterialRequestApprovalDto.class))).thenReturn(approved);

        mockMvc.perform(put("/api/v1/procurement/requests/1/approval")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("GET /api/v1/procurement/requests/project/1/projected-shortage - Projected Shortage Detection Success")
    void getProjectedShortages_Success() throws Exception {
        ProjectedShortageDto shortage = ProjectedShortageDto.builder()
                .materialId(10L)
                .materialCode("MAT-CEM-001")
                .materialName("Cement 50kg")
                .category("CEMENT")
                .unit(MaterialUnit.BAGS)
                .currentStock(new BigDecimal("100.00"))
                .reorderLevel(new BigDecimal("50.00"))
                .totalRequestedQty(new BigDecimal("300.00"))
                .projectedShortage(new BigDecimal("200.00"))
                .status("SHORTAGE")
                .build();

        when(procurementService.detectProjectedShortage(1L)).thenReturn(List.of(shortage));

        mockMvc.perform(get("/api/v1/procurement/requests/project/1/projected-shortage").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("SHORTAGE"))
                .andExpect(jsonPath("$.data[0].projectedShortage").value(200.00));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("POST /api/v1/procurement/purchase-orders - Create Purchase Order Success")
    void createPurchaseOrder_Success() throws Exception {
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber("PO-2026-001")
                .supplierId(3L)
                .projectId(1L)
                .materialId(10L)
                .orderQty(new BigDecimal("500.00"))
                .unitRate(new BigDecimal("350.00"))
                .amount(new BigDecimal("175000.00"))
                .deliveryDate(LocalDate.now().plusDays(10))
                .build();

        PurchaseOrder saved = PurchaseOrder.builder()
                .id(50L)
                .poNumber("PO-2026-001")
                .supplierId(3L)
                .projectId(1L)
                .materialId(10L)
                .orderQty(new BigDecimal("500.00"))
                .amount(new BigDecimal("175000.00"))
                .status("PENDING_APPROVAL")
                .build();

        when(procurementService.createPurchaseOrder(any(PurchaseOrder.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/procurement/purchase-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(po)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(50))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }

    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    @DisplayName("PUT /api/v1/procurement/purchase-orders/50/approval - Financial Approval for PO Success")
    void updatePurchaseOrderApproval_Success() throws Exception {
        PurchaseOrderApprovalDto approvalDto = PurchaseOrderApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("Director_Murugan")
                .remarks("Approved with 30 days credit terms")
                .build();

        PurchaseOrder approvedPo = PurchaseOrder.builder()
                .id(50L)
                .status("APPROVED")
                .approvedBy("Director_Murugan")
                .build();

        when(procurementService.updatePurchaseOrderApproval(eq(50L), any(PurchaseOrderApprovalDto.class))).thenReturn(approvedPo);

        mockMvc.perform(put("/api/v1/procurement/purchase-orders/50/approval")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "SITE_ENGINEER")
    @DisplayName("GET /api/v1/procurement/purchase-orders/50/fulfillment - Get PO Fulfillment Tracking Success")
    void getPoFulfillmentStatus_Success() throws Exception {
        PoFulfillmentDto dto = PoFulfillmentDto.builder()
                .poId(50L)
                .poNumber("PO-2026-001")
                .orderQty(new BigDecimal("500.00"))
                .totalReceivedQty(new BigDecimal("500.00"))
                .totalAcceptedQty(new BigDecimal("480.00"))
                .totalRejectedQty(new BigDecimal("20.00"))
                .remainingQty(new BigDecimal("20.00"))
                .status("PARTIALLY_DELIVERED")
                .fulfillmentPercentage("96.00%")
                .build();

        when(procurementService.getPoFulfillmentStatus(50L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/procurement/purchase-orders/50/fulfillment").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.poNumber").value("PO-2026-001"))
                .andExpect(jsonPath("$.data.fulfillmentPercentage").value("96.00%"))
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_DELIVERED"));
    }

    @Test
    @WithMockUser(roles = "SITE_ENGINEER")
    @DisplayName("POST /api/v1/procurement/grn - Record Delivery Receipt GRN Success")
    void createGrn_Success() throws Exception {
        Grn grn = Grn.builder()
                .poId(50L)
                .projectId(1L)
                .materialId(10L)
                .receivedQty(new BigDecimal("500.00"))
                .acceptedQty(new BigDecimal("480.00"))
                .rejectedQty(new BigDecimal("20.00"))
                .rejectionReason("Torn bags in transit")
                .build();

        Grn saved = Grn.builder()
                .id(101L)
                .poId(50L)
                .materialId(10L)
                .acceptedQty(new BigDecimal("480.00"))
                .rejectedQty(new BigDecimal("20.00"))
                .build();

        when(procurementService.createGrn(any(Grn.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/procurement/grn")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101));
    }
}

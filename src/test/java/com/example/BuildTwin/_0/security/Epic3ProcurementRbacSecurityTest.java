package com.example.BuildTwin._0.security;

import com.example.BuildTwin._0.controller.ProcurementController;
import com.example.BuildTwin._0.controller.SupplierController;
import com.example.BuildTwin._0.domain.materials.dto.SupplierCreateDto;
import com.example.BuildTwin._0.domain.materials.model.Supplier;
import com.example.BuildTwin._0.domain.materials.service.SupplierService;
import com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto;
import com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderApprovalDto;
import com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderStatusDto;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.model.MaterialRequest;
import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import com.example.BuildTwin._0.domain.procurement.service.ProcurementService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SupplierController.class, ProcurementController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class Epic3ProcurementRbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private ProcurementService procurementService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ==========================================
    // 1. UNAUTHENTICATED TESTS (401 Unauthorized)
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request to GET /api/v1/suppliers returns 401 Unauthorized")
    void unauthenticatedSuppliersReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated request to GET /api/v1/procurement/requests/1 returns 401 Unauthorized")
    void unauthenticatedProcurementReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/procurement/requests/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // =========================================================================
    // 2. READ ACCESS: ALL 10 ROLES MUST HAVE READ PERMISSIONS ON SUPPLIERS & MR
    // 10 Roles: DIRECTOR, PROJECT_MANAGER, SITE_ENGINEER, SITE_SUPERVISOR,
    //           PROCUREMENT_STORE, QUANTITY_COST_COORDINATOR, QUALITY_ENGINEER,
    //           DATA_ANALYST, ADMIN, AUDITOR
    // =========================================================================

    @ParameterizedTest(name = "Role {0} should have read access to suppliers directory")
    @ValueSource(strings = {
            "DIRECTOR", "PROJECT_MANAGER", "SITE_ENGINEER", "SITE_SUPERVISOR",
            "PROCUREMENT_STORE", "QUANTITY_COST_COORDINATOR", "QUALITY_ENGINEER",
            "DATA_ANALYST", "ADMIN", "AUDITOR"
    })
    void testAll10RolesHaveReadAccessToSuppliers(String roleName) throws Exception {
        when(supplierService.getAllSuppliers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/suppliers")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("user_" + roleName).roles(roleName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest(name = "Role {0} should have read access to material requests by project")
    @ValueSource(strings = {
            "DIRECTOR", "PROJECT_MANAGER", "SITE_ENGINEER", "SITE_SUPERVISOR",
            "PROCUREMENT_STORE", "QUANTITY_COST_COORDINATOR", "QUALITY_ENGINEER",
            "DATA_ANALYST", "ADMIN", "AUDITOR"
    })
    void testAll10RolesHaveReadAccessToMaterialRequests(String roleName) throws Exception {
        when(procurementService.getMaterialRequestsByProject(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/procurement/requests/project/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("user_" + roleName).roles(roleName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // 3. SUPPLIER MASTER RBAC: WRITE ACCESS
    // Allowed: ADMIN, DIRECTOR, PROJECT_MANAGER, PROCUREMENT_STORE
    // Forbidden: SITE_ENGINEER, SITE_SUPERVISOR, QUANTITY_COST_COORDINATOR, QUALITY_ENGINEER, DATA_ANALYST, AUDITOR
    // =========================================================================

    @Test
    @WithMockUser(username = "store_mgr", roles = {"PROCUREMENT_STORE"})
    @DisplayName("PROCUREMENT_STORE role is allowed to register new supplier")
    void testProcurementStoreCanCreateSupplier() throws Exception {
        SupplierCreateDto dto = SupplierCreateDto.builder()
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .phone("9876543210")
                .build();

        Supplier created = Supplier.builder().id(1L).supplierCode("SUP-101").name("UltraTech Cements").build();
        when(supplierService.createSupplier(any(SupplierCreateDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/suppliers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "site_eng", roles = {"SITE_ENGINEER"})
    @DisplayName("SITE_ENGINEER role is FORBIDDEN (403) from creating supplier")
    void testSiteEngineerForbiddenFromCreatingSupplier() throws Exception {
        SupplierCreateDto dto = SupplierCreateDto.builder()
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .phone("9876543210")
                .build();

        mockMvc.perform(post("/api/v1/suppliers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "auditor_user", roles = {"AUDITOR"})
    @DisplayName("AUDITOR role is FORBIDDEN (403) from deleting / blacklisting supplier")
    void testAuditorForbiddenFromDeletingSupplier() throws Exception {
        mockMvc.perform(delete("/api/v1/suppliers/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // =========================================================================
    // 4. MATERIAL REQUISITION (MR) RBAC
    // Raise MR Allowed: ADMIN, PROJECT_MANAGER, SITE_ENGINEER, SITE_SUPERVISOR, PROCUREMENT_STORE
    // Raise MR Forbidden: AUDITOR, DATA_ANALYST, QUALITY_ENGINEER, QUANTITY_COST_COORDINATOR
    // Approve MR Allowed: ADMIN, DIRECTOR, PROJECT_MANAGER, PROCUREMENT_STORE
    // Approve MR Forbidden: SITE_ENGINEER, SITE_SUPERVISOR, AUDITOR, DATA_ANALYST
    // =========================================================================

    @Test
    @WithMockUser(username = "supervisor_velu", roles = {"SITE_SUPERVISOR"})
    @DisplayName("SITE_SUPERVISOR is allowed to raise site material request")
    void testSiteSupervisorCanRaiseMaterialRequest() throws Exception {
        MaterialRequest req = MaterialRequest.builder()
                .projectId(1L)
                .materialId(10L)
                .requiredQty(new BigDecimal("100.00"))
                .build();

        when(procurementService.createMaterialRequest(any(MaterialRequest.class))).thenReturn(req);

        mockMvc.perform(post("/api/v1/procurement/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "auditor_user", roles = {"AUDITOR"})
    @DisplayName("AUDITOR is FORBIDDEN (403) from raising material request")
    void testAuditorForbiddenFromRaisingMaterialRequest() throws Exception {
        MaterialRequest req = MaterialRequest.builder()
                .projectId(1L)
                .materialId(10L)
                .requiredQty(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/v1/procurement/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "director_ramesh", roles = {"DIRECTOR"})
    @DisplayName("DIRECTOR is allowed to approve material request")
    void testDirectorCanApproveMaterialRequest() throws Exception {
        MaterialRequestApprovalDto dto = MaterialRequestApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("Director_Ramesh")
                .build();

        MaterialRequest approved = MaterialRequest.builder().id(1L).status("APPROVED").build();
        when(procurementService.updateMaterialRequestStatus(eq(1L), any(MaterialRequestApprovalDto.class)))
                .thenReturn(approved);

        mockMvc.perform(put("/api/v1/procurement/requests/1/approval")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "site_eng", roles = {"SITE_ENGINEER"})
    @DisplayName("SITE_ENGINEER is FORBIDDEN (403) from approving material request")
    void testSiteEngineerForbiddenFromApprovingMaterialRequest() throws Exception {
        MaterialRequestApprovalDto dto = MaterialRequestApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("Self")
                .build();

        mockMvc.perform(put("/api/v1/procurement/requests/1/approval")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // =========================================================================
    // 5. PURCHASE ORDER (PO) RBAC
    // Create PO Allowed: ADMIN, DIRECTOR, PROJECT_MANAGER, PROCUREMENT_STORE
    // Create PO Forbidden: SITE_ENGINEER, SITE_SUPERVISOR, AUDITOR, DATA_ANALYST
    // PO Financial Approval Allowed: ADMIN, DIRECTOR, PROJECT_MANAGER
    // PO Financial Approval Forbidden: PROCUREMENT_STORE, SITE_ENGINEER, AUDITOR
    // =========================================================================

    @Test
    @WithMockUser(username = "store_lead", roles = {"PROCUREMENT_STORE"})
    @DisplayName("PROCUREMENT_STORE is allowed to generate Purchase Order")
    void testProcurementStoreCanCreatePO() throws Exception {
        PurchaseOrder po = PurchaseOrder.builder()
                .supplierId(3L)
                .projectId(1L)
                .amount(new BigDecimal("50000.00"))
                .build();

        PurchaseOrder saved = PurchaseOrder.builder().id(50L).status("DRAFT").build();
        when(procurementService.createPurchaseOrder(any(PurchaseOrder.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/procurement/purchase-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(po)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "site_eng", roles = {"SITE_ENGINEER"})
    @DisplayName("SITE_ENGINEER is FORBIDDEN (403) from generating Purchase Order")
    void testSiteEngineerForbiddenFromCreatingPO() throws Exception {
        PurchaseOrder po = PurchaseOrder.builder()
                .supplierId(3L)
                .projectId(1L)
                .amount(new BigDecimal("50000.00"))
                .build();

        mockMvc.perform(post("/api/v1/procurement/purchase-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(po)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "director_murugan", roles = {"DIRECTOR"})
    @DisplayName("DIRECTOR is allowed to perform financial sign-off approval on PO")
    void testDirectorCanPerformPOFinancialApproval() throws Exception {
        PurchaseOrderApprovalDto dto = PurchaseOrderApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("Director_Murugan")
                .build();

        PurchaseOrder po = PurchaseOrder.builder().id(50L).status("APPROVED").build();
        when(procurementService.updatePurchaseOrderApproval(eq(50L), any(PurchaseOrderApprovalDto.class)))
                .thenReturn(po);

        mockMvc.perform(put("/api/v1/procurement/purchase-orders/50/approval")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "store_lead", roles = {"PROCUREMENT_STORE"})
    @DisplayName("PROCUREMENT_STORE is FORBIDDEN (403) from PO financial sign-off approval (Only PM/Director/Admin)")
    void testProcurementStoreForbiddenFromPOFinancialApproval() throws Exception {
        PurchaseOrderApprovalDto dto = PurchaseOrderApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("Store")
                .build();

        mockMvc.perform(put("/api/v1/procurement/purchase-orders/50/approval")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // =========================================================================
    // 6. GOODS RECEIPT NOTE (GRN) RBAC
    // Record GRN Allowed: ADMIN, PROJECT_MANAGER, SITE_ENGINEER, SITE_SUPERVISOR, PROCUREMENT_STORE
    // Record GRN Forbidden: DIRECTOR, AUDITOR, DATA_ANALYST, QUALITY_ENGINEER, QUANTITY_COST_COORDINATOR
    // Void GRN Allowed: ADMIN, DIRECTOR, PROJECT_MANAGER, PROCUREMENT_STORE
    // Void GRN Forbidden: SITE_ENGINEER, SITE_SUPERVISOR, AUDITOR
    // =========================================================================

    @Test
    @WithMockUser(username = "gate_supervisor", roles = {"SITE_SUPERVISOR"})
    @DisplayName("SITE_SUPERVISOR at site gate is allowed to record GRN delivery")
    void testSiteSupervisorCanRecordGRN() throws Exception {
        Grn grn = Grn.builder()
                .poId(50L)
                .materialId(10L)
                .dcNumber("DC-1234")
                .receivedQty(new BigDecimal("100.00"))
                .build();

        Grn saved = Grn.builder().id(101L).build();
        when(procurementService.createGrn(any(Grn.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/procurement/grn")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "auditor_user", roles = {"AUDITOR"})
    @DisplayName("AUDITOR is FORBIDDEN (403) from recording gate delivery GRN")
    void testAuditorForbiddenFromRecordingGRN() throws Exception {
        Grn grn = Grn.builder()
                .poId(50L)
                .materialId(10L)
                .receivedQty(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/v1/procurement/grn")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grn)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "site_engineer", roles = {"SITE_ENGINEER"})
    @DisplayName("SITE_ENGINEER is FORBIDDEN (403) from voiding GRN entry (Requires PM/Store/Director/Admin)")
    void testSiteEngineerForbiddenFromVoidingGRN() throws Exception {
        mockMvc.perform(delete("/api/v1/procurement/grn/101").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "pm_user", roles = {"PROJECT_MANAGER"})
    @DisplayName("PROJECT_MANAGER is allowed to void GRN entry")
    void testProjectManagerCanVoidGRN() throws Exception {
        mockMvc.perform(delete("/api/v1/procurement/grn/101").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("GRN entry voided successfully"));
    }
}

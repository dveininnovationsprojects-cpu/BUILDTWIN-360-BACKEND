package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.materials.dto.MaterialRequestDto;
import com.example.BuildTwin._0.domain.materials.dto.MaterialStockBalanceDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.service.MaterialService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MaterialController.class)
@AutoConfigureMockMvc(addFilters = false)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MaterialService materialService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testCreateMaterialSuccess() throws Exception {
        MaterialRequestDto dto = new MaterialRequestDto();
        dto.setMaterialCode("MAT-CEM-53");
        dto.setName("OPC 53 Grade Cement");
        dto.setCategory("CEMENT");
        dto.setUnit(MaterialUnit.BAGS);
        dto.setStandardRate(new BigDecimal("380.00"));
        dto.setReorderLevel(new BigDecimal("500.00"));

        Material saved = Material.builder()
                .id(1L)
                .materialCode("MAT-CEM-53")
                .name("OPC 53 Grade Cement")
                .category("CEMENT")
                .unit(MaterialUnit.BAGS)
                .standardRate(new BigDecimal("380.00"))
                .reorderLevel(new BigDecimal("500.00"))
                .currentStock(BigDecimal.ZERO)
                .build();

        when(materialService.createMaterial(any(MaterialRequestDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.materialCode").value("MAT-CEM-53"))
                .andExpect(jsonPath("$.data.category").value("CEMENT"));
    }

    @Test
    void testGetReorderAlerts() throws Exception {
        Material lowStockMat = Material.builder()
                .id(2L)
                .materialCode("MAT-STL-12")
                .name("12mm TMT Fe500 Rebar")
                .category("STEEL")
                .unit(MaterialUnit.TONNES)
                .currentStock(new BigDecimal("50.00"))
                .reorderLevel(new BigDecimal("100.00"))
                .build();

        when(materialService.getMaterialsNeedingReorder()).thenReturn(List.of(lowStockMat));

        mockMvc.perform(get("/api/v1/materials/reorder-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].materialCode").value("MAT-STL-12"));
    }

    @Test
    void testGetStockBalance() throws Exception {
        MaterialStockBalanceDto balanceDto = MaterialStockBalanceDto.builder()
                .materialId(1L)
                .materialCode("MAT-CEM-53")
                .name("OPC 53 Grade Cement")
                .category("CEMENT")
                .unit(MaterialUnit.BAGS)
                .currentStock(new BigDecimal("150.00"))
                .reorderLevel(new BigDecimal("50.00"))
                .standardRate(new BigDecimal("380.00"))
                .totalStockValue(new BigDecimal("57000.00"))
                .lowStock(false)
                .build();

        when(materialService.getStockBalance(1L)).thenReturn(balanceDto);

        mockMvc.perform(get("/api/v1/materials/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.materialId").value(1L))
                .andExpect(jsonPath("$.data.currentStock").value(150.00))
                .andExpect(jsonPath("$.data.totalStockValue").value(57000.00))
                .andExpect(jsonPath("$.data.lowStock").value(false));
    }

    @Test
    void testGetLowStockMaterialsWithProjectFilter() throws Exception {
        Material lowStockMat = Material.builder()
                .id(3L)
                .materialCode("MAT-AGG-20")
                .name("20mm Aggregate")
                .category("AGGREGATE")
                .unit(MaterialUnit.CU_M)
                .currentStock(new BigDecimal("10.00"))
                .reorderLevel(new BigDecimal("25.00"))
                .build();

        when(materialService.getLowStockMaterials(eq(101L))).thenReturn(List.of(lowStockMat));

        mockMvc.perform(get("/api/v1/materials/low-stock").param("projectId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].materialCode").value("MAT-AGG-20"));
    }
}

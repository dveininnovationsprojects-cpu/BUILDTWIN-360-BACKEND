package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.materials.dto.SupplierCreateDto;
import com.example.BuildTwin._0.domain.materials.model.Supplier;
import com.example.BuildTwin._0.domain.materials.service.SupplierService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupplierController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/suppliers - Register Supplier Success")
    void testCreateSupplier() throws Exception {
        SupplierCreateDto dto = new SupplierCreateDto();
        dto.setSupplierCode("SUP-101");
        dto.setName("UltraTech Cements");
        dto.setContactPerson("Ravi Kumar");
        dto.setPhone("9876543210");
        dto.setGstin("33AAAAA0000A1Z5");
        dto.setStatus("ACTIVE");

        Supplier saved = Supplier.builder()
                .id(1L)
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .contactPerson("Ravi Kumar")
                .phone("9876543210")
                .gstin("33AAAAA0000A1Z5")
                .status("ACTIVE")
                .build();

        when(supplierService.createSupplier(any(SupplierCreateDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supplierCode").value("SUP-101"))
                .andExpect(jsonPath("$.data.name").value("UltraTech Cements"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers - List All Suppliers Success")
    void testGetAllSuppliers() throws Exception {
        Supplier s = Supplier.builder()
                .id(1L)
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .status("ACTIVE")
                .build();

        when(supplierService.getAllSuppliers()).thenReturn(List.of(s));

        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].supplierCode").value("SUP-101"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/code/{code} - Get By Code Success")
    void testGetSupplierByCode() throws Exception {
        Supplier s = Supplier.builder()
                .id(1L)
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .status("ACTIVE")
                .build();

        when(supplierService.getSupplierByCode("SUP-101")).thenReturn(s);

        mockMvc.perform(get("/api/v1/suppliers/code/SUP-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supplierCode").value("SUP-101"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/status/{status} - Filter By Status Success")
    void testGetSuppliersByStatus() throws Exception {
        Supplier s = Supplier.builder()
                .id(1L)
                .supplierCode("SUP-101")
                .status("ACTIVE")
                .build();

        when(supplierService.getSuppliersByStatus("ACTIVE")).thenReturn(List.of(s));

        mockMvc.perform(get("/api/v1/suppliers/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/{id} - Delete Supplier Success")
    void testDeleteSupplier() throws Exception {
        doNothing().when(supplierService).deleteSupplier(1L);

        mockMvc.perform(delete("/api/v1/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier deleted successfully"));
    }
}

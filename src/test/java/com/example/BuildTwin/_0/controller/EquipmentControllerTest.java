package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.equipment.model.Equipment;
import com.example.BuildTwin._0.domain.equipment.model.EquipmentUsage;
import com.example.BuildTwin._0.domain.equipment.service.EquipmentService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipmentService equipmentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Equipment sampleEquipment;
    private EquipmentUsage sampleUsage;

    @BeforeEach
    void setUp() {
        sampleEquipment = Equipment.builder()
                .id(1L)
                .assetCode("EQ-MIX-01")
                .name("Transit Concrete Mixer 6m3")
                .type("MIXER")
                .status("AVAILABLE")
                .build();

        sampleUsage = EquipmentUsage.builder()
                .id(1L)
                .equipmentId(1L)
                .projectId(100L)
                .usageDate(LocalDate.now())
                .hoursUsed(new BigDecimal("7.50"))
                .downtimeHours(new BigDecimal("1.50"))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/equipment should register equipment asset")
    void testRegisterEquipment() throws Exception {
        when(equipmentService.registerEquipment(any(Equipment.class))).thenReturn(sampleEquipment);

        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEquipment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetCode").value("EQ-MIX-01"));
    }

    @Test
    @DisplayName("GET /api/v1/equipment should return all equipment assets")
    void testGetAllEquipment() throws Exception {
        when(equipmentService.getAllEquipment()).thenReturn(List.of(sampleEquipment));

        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Transit Concrete Mixer 6m3"));
    }

    @Test
    @DisplayName("GET /api/v1/equipment/{id} should return equipment details")
    void testGetEquipmentById() throws Exception {
        when(equipmentService.getEquipmentById(1L)).thenReturn(sampleEquipment);

        mockMvc.perform(get("/api/v1/equipment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/equipment/{id} should update equipment asset")
    void testUpdateEquipment() throws Exception {
        Equipment updated = Equipment.builder()
                .id(1L).assetCode("EQ-MIX-01").name("Heavy Transit Mixer").type("MIXER").status("IN_USE").build();
        when(equipmentService.updateEquipment(eq(1L), any(Equipment.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/equipment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_USE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/equipment/{id} should delete equipment asset")
    void testDeleteEquipment() throws Exception {
        doNothing().when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(delete("/api/v1/equipment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(equipmentService, times(1)).deleteEquipment(1L);
    }

    @Test
    @DisplayName("POST /api/v1/equipment/usage should record equipment usage and downtime")
    void testRecordUsage() throws Exception {
        when(equipmentService.recordUsage(any(EquipmentUsage.class))).thenReturn(sampleUsage);

        mockMvc.perform(post("/api/v1/equipment/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUsage)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hoursUsed").value(7.50));
    }

    @Test
    @DisplayName("GET /api/v1/equipment/usage/{id} should return equipment usage log")
    void testGetEquipmentUsageById() throws Exception {
        when(equipmentService.getEquipmentUsageById(1L)).thenReturn(sampleUsage);

        mockMvc.perform(get("/api/v1/equipment/usage/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/equipment/usage/project/{projectId} should return project usage logs")
    void testGetUsageByProject() throws Exception {
        when(equipmentService.getUsageByProject(100L)).thenReturn(List.of(sampleUsage));

        mockMvc.perform(get("/api/v1/equipment/usage/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].downtimeHours").value(1.50));
    }

    @Test
    @DisplayName("DELETE /api/v1/equipment/usage/{id} should delete usage log")
    void testDeleteEquipmentUsage() throws Exception {
        doNothing().when(equipmentService).deleteEquipmentUsage(1L);

        mockMvc.perform(delete("/api/v1/equipment/usage/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(equipmentService, times(1)).deleteEquipmentUsage(1L);
    }
}

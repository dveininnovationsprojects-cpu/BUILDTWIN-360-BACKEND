package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.dpr.model.DprHeader;
import com.example.BuildTwin._0.domain.dpr.service.DprService;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DprController.class)
@AutoConfigureMockMvc(addFilters = false)
class DprControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DprService dprService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testCreateDprDraft() throws Exception {
        DprHeader header = DprHeader.builder()
                .projectId(10L)
                .reportDate(LocalDate.now())
                .weather("SUNNY")
                .status("DRAFT")
                .submittedBy("site_eng_1")
                .build();

        DprHeader saved = DprHeader.builder()
                .id(100L)
                .projectId(10L)
                .reportDate(LocalDate.now())
                .weather("SUNNY")
                .status("DRAFT")
                .submittedBy("site_eng_1")
                .build();

        when(dprService.createDpr(any(DprHeader.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/dpr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(header)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void testApproveDpr() throws Exception {
        DprHeader approved = DprHeader.builder()
                .id(100L)
                .projectId(10L)
                .reportDate(LocalDate.now())
                .status("APPROVED")
                .build();

        when(dprService.approveDpr(100L)).thenReturn(approved);

        mockMvc.perform(post("/api/v1/dpr/100/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}

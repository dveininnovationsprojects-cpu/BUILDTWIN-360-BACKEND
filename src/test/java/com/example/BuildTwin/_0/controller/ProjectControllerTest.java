package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.project.CreateProjectRequest;
import com.example.BuildTwin._0.dto.project.ProjectResponse;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.example.BuildTwin._0.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProjectSuccess() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setCode("PRJ-101");
        request.setName("Grand Horizon Towers");
        request.setLocation("Chennai");

        ProjectResponse response = ProjectResponse.builder()
                .id(1L)
                .code("PRJ-101")
                .name("Grand Horizon Towers")
                .location("Chennai")
                .status("ACTIVE")
                .build();

        when(projectService.createProject(any(CreateProjectRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("PRJ-101"));
    }

    @Test
    void testGetAllProjects() throws Exception {
        ProjectResponse p1 = ProjectResponse.builder().id(1L).code("PRJ-01").name("Site Alpha").build();
        ProjectResponse p2 = ProjectResponse.builder().id(2L).code("PRJ-02").name("Site Beta").build();

        PageResponse<ProjectResponse> pageResponse = PageResponse.<ProjectResponse>builder()
                .content(List.of(p1, p2))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(2L)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(projectService.getAllProjects(any(), any(), any(), eq(0), eq(10), anyString(), anyString()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].code").value("PRJ-01"))
                .andExpect(jsonPath("$.data.content[1].code").value("PRJ-02"));
    }

    @Test
    void testGetProjectByIdNotFound() throws Exception {
        when(projectService.getProjectById(999L)).thenThrow(new ResourceNotFoundException("Project", "id", 999L));

        mockMvc.perform(get("/api/v1/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found with id: '999'"));
    }
}


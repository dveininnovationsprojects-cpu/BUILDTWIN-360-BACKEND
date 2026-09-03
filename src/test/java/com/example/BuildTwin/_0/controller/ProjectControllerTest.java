package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.service.ProjectService;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    void testCreateProjectSuccess() throws Exception {
        Project project = Project.builder()
                .code("PRJ-101")
                .name("Grand Horizon Towers")
                .location("Chennai")
                .status("ACTIVE")
                .build();

        Project created = Project.builder()
                .id(1L)
                .code("PRJ-101")
                .name("Grand Horizon Towers")
                .location("Chennai")
                .status("ACTIVE")
                .build();

        when(projectService.createProject(any(Project.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("PRJ-101"));
    }

    @Test
    void testGetAllProjects() throws Exception {
        Project p1 = Project.builder().id(1L).code("PRJ-01").name("Site Alpha").build();
        Project p2 = Project.builder().id(2L).code("PRJ-02").name("Site Beta").build();

        when(projectService.getAllProjects()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("PRJ-01"))
                .andExpect(jsonPath("$.data[1].code").value("PRJ-02"));
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

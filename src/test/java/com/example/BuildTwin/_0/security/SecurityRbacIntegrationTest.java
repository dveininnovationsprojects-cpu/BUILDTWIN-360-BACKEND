package com.example.BuildTwin._0.security;

import com.example.BuildTwin._0.controller.AuthController;
import com.example.BuildTwin._0.controller.ProjectController;
import com.example.BuildTwin._0.domain.identity.model.UserProjectRole;
import com.example.BuildTwin._0.domain.projects.service.ProjectService;
import com.example.BuildTwin._0.dto.auth.AssignProjectRoleRequest;
import com.example.BuildTwin._0.exception.GlobalExceptionHandler;
import com.example.BuildTwin._0.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, ProjectController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class SecurityRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void testProtectedEndpointUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "engineer_joe", roles = {"SITE_ENGINEER"})
    void testAdminEndpointWithSiteEngineerRoleReturns403() throws Exception {
        AssignProjectRoleRequest request = new AssignProjectRoleRequest();
        request.setUserId(2L);
        request.setProjectId(10L);
        request.setRoleId(1L);

        mockMvc.perform(post("/api/v1/auth/assign-project-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void testAdminEndpointWithAdminRoleAllowed() throws Exception {
        AssignProjectRoleRequest request = new AssignProjectRoleRequest();
        request.setUserId(2L);
        request.setProjectId(10L);
        request.setRoleId(1L);

        UserProjectRole roleMock = new UserProjectRole();
        when(authService.assignProjectRole(any(AssignProjectRoleRequest.class))).thenReturn(roleMock);

        mockMvc.perform(post("/api/v1/auth/assign-project-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}

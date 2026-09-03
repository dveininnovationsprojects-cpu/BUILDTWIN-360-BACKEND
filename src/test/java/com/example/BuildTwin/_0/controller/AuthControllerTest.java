package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.identity.model.Role;
import com.example.BuildTwin._0.dto.auth.AuthResponse;
import com.example.BuildTwin._0.dto.auth.LoginRequest;
import com.example.BuildTwin._0.dto.auth.RegisterRequest;
import com.example.BuildTwin._0.dto.auth.UserSummaryDto;
import com.example.BuildTwin._0.exception.GlobalExceptionHandler;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.example.BuildTwin._0.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testRegisterEndpoint() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setRoles(Set.of("SITE_ENGINEER"));

        UserSummaryDto summaryDto = UserSummaryDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status("ACTIVE")
                .roles(Set.of("ROLE_SITE_ENGINEER"))
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock_access_token")
                .refreshToken("mock_refresh_token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(summaryDto)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock_access_token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void testLoginEndpoint() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("Password123!");

        UserSummaryDto summaryDto = UserSummaryDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status("ACTIVE")
                .roles(Set.of("ROLE_SITE_ENGINEER"))
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock_access_token")
                .refreshToken("mock_refresh_token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(summaryDto)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock_access_token"));
    }

    @Test
    void testGetAllRolesEndpoint() throws Exception {
        Role role1 = Role.builder().id(1L).name("ROLE_ADMIN").build();
        Role role2 = Role.builder().id(2L).name("ROLE_PROJECT_MANAGER").build();

        when(authService.getAllRoles()).thenReturn(List.of(role1, role2));

        mockMvc.perform(get("/api/v1/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data[1].name").value("ROLE_PROJECT_MANAGER"));
    }
}

package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.domain.identity.model.Role;
import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.dto.auth.AuthResponse;
import com.example.BuildTwin._0.dto.auth.LoginRequest;
import com.example.BuildTwin._0.dto.auth.RegisterRequest;

import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.UnauthorizedException;
import com.example.BuildTwin._0.repository.RoleRepository;
import com.example.BuildTwin._0.repository.UserProjectRoleRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.security.CustomUserDetails;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.example.BuildTwin._0.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserProjectRoleRepository userProjectRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder().id(1L).name("ROLE_ADMIN").build();
        sampleUser = User.builder()
                .id(10L)
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_pass")
                .status("ACTIVE")
                .roles(Set.of(sampleRole))
                .build();
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john_doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRoles(Set.of("ADMIN"));

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(sampleRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertEquals("john_doe", response.getUser().getUsername());
    }

    @Test
    void testRegisterDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john_doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("john_doe");
        request.setPassword("password123");

        CustomUserDetails userDetails = new CustomUserDetails(sampleUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "password123", userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("jwt_access_token");
        when(jwtTokenProvider.generateRefreshToken("john_doe")).thenReturn("jwt_refresh_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt_access_token", response.getAccessToken());
        assertEquals("john_doe", response.getUser().getUsername());
    }

    @Test
    void testLoginBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("john_doe");
        request.setPassword("wrong_pass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}

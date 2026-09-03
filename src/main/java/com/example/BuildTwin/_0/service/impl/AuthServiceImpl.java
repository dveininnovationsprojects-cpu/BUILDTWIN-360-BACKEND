package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.auth.*;
import com.example.BuildTwin._0.dto.user.ChangePasswordRequest;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ForbiddenException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.exception.UnauthorizedException;
import com.example.BuildTwin._0.domain.identity.model.Role;
import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.domain.identity.model.UserProjectRole;
import com.example.BuildTwin._0.repository.RoleRepository;
import com.example.BuildTwin._0.repository.UserProjectRoleRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.security.CustomUserDetails;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Set<Role> assignedRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                String normalizedName = roleName.toUpperCase().startsWith("ROLE_")
                        ? roleName.toUpperCase()
                        : "ROLE_" + roleName.toUpperCase();

                Role role = roleRepository.findByName(normalizedName)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(normalizedName).build()));
                assignedRoles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_SITE_ENGINEER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_SITE_ENGINEER").build()));
            assignedRoles.add(defaultRole);
        }

        // New registrations require Admin / Management approval
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status("PENDING_APPROVAL")
                .roles(assignedRoles)
                .build();

        User savedUser = userRepository.save(user);

        auditService.logAction(
                savedUser.getUsername(),
                "REGISTER",
                "USER",
                String.valueOf(savedUser.getId()),
                "User submitted registration (PENDING_APPROVAL). Awaiting Admin/Director approval.",
                null
        );

        return AuthResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .tokenType("Bearer")
                .expiresIn(0L)
                .user(mapToUserSummaryDto(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Pre-check user status before authentication
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElse(null);

        if (user != null) {
            if ("PENDING_APPROVAL".equalsIgnoreCase(user.getStatus())) {
                throw new ForbiddenException("Your registration is pending approval by System Administrator or Project Director.");
            }
            if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
                throw new ForbiddenException("Your registration request was rejected by administration. Please contact support.");
            }
            if ("INACTIVE".equalsIgnoreCase(user.getStatus()) || "SUSPENDED".equalsIgnoreCase(user.getStatus())) {
                throw new ForbiddenException("Account is " + user.getStatus().toLowerCase() + ". Please contact administrator.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (user == null) {
                user = userRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
            }

            if ("PENDING_APPROVAL".equalsIgnoreCase(user.getStatus())) {
                throw new ForbiddenException("Your registration is pending approval by System Administrator or Project Director.");
            }
            if ("INACTIVE".equalsIgnoreCase(user.getStatus()) || "SUSPENDED".equalsIgnoreCase(user.getStatus())) {
                throw new ForbiddenException("Account is " + user.getStatus().toLowerCase() + ". Please contact administrator.");
            }

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            auditService.logAction(
                    user.getUsername(),
                    "LOGIN",
                    "USER",
                    String.valueOf(user.getId()),
                    "User logged in successfully",
                    null
            );

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationMs())
                    .user(mapToUserSummaryDto(user))
                    .build();
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username/email or password");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ForbiddenException("Account is not active (" + user.getStatus() + "). Token refresh denied.");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(),
                user.getId(),
                user.getEmail(),
                roleNames
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .user(mapToUserSummaryDto(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToUserSummaryDto(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logAction(
                username,
                "CHANGE_PASSWORD",
                "USER",
                String.valueOf(user.getId()),
                "User successfully changed their own password",
                null
        );
    }

    @Override
    @Transactional
    public UserProjectRole assignProjectRole(AssignProjectRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        UserProjectRole userProjectRole = UserProjectRole.builder()
                .user(user)
                .projectId(request.getProjectId())
                .role(role)
                .build();

        return userProjectRoleRepository.save(userProjectRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    private UserSummaryDto mapToUserSummaryDto(User user) {
        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Set.of();

        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}

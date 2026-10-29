package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.domain.identity.model.Role;
import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.domain.identity.model.UserProjectRole;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.project.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.repository.RoleRepository;
import com.example.BuildTwin._0.repository.SiteRepository;
import com.example.BuildTwin._0.repository.UserProjectRoleRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProjectRoleRepository userProjectRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    private ProjectServiceImpl projectService;

    private Project sampleProject;
    private User samplePmUser;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(
                projectRepository,
                siteRepository,
                userRepository,
                userProjectRoleRepository,
                roleRepository,
                auditService,
                Optional.of(notificationService)
        );

        samplePmUser = User.builder()
                .id(10L)
                .username("pm_karthik")
                .email("pm.karthik@buildtwin360.com")
                .build();

        sampleProject = Project.builder()
                .id(1L)
                .code("PADUR-AG-01")
                .name("Ashok Grandeur - Padur, Chennai")
                .description("Luxury High-Rise")
                .clientName("Ashok Builders")
                .projectType("RESIDENTIAL")
                .location("Padur, OMR, Chennai")
                .status("PLANNED")
                .plannedStartDate(LocalDate.of(2026, 11, 1))
                .plannedEndDate(LocalDate.of(2028, 11, 1))
                .estimatedBudget(new BigDecimal("45000000.00"))
                .currency("INR")
                .totalBuiltUpAreaSqFt(350000.0)
                .projectManagerId(10L)
                .build();
    }

    @Test
    @DisplayName("Should create project successfully and assign PM role")
    void testCreateProjectSuccess() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .code("PADUR-AG-01")
                .name("Ashok Grandeur - Padur, Chennai")
                .projectType("RESIDENTIAL")
                .status("PLANNED")
                .projectManagerId(10L)
                .build();

        when(projectRepository.existsByCode("PADUR-AG-01")).thenReturn(false);
        when(userRepository.existsById(10L)).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);
        when(roleRepository.findByName("ROLE_PROJECT_MANAGER")).thenReturn(Optional.of(Role.builder().id(3L).name("ROLE_PROJECT_MANAGER").build()));
        when(userRepository.findById(10L)).thenReturn(Optional.of(samplePmUser));
        when(userProjectRoleRepository.existsByUserIdAndProjectIdAndRoleId(10L, 1L, 3L)).thenReturn(false);

        ProjectResponse response = projectService.createProject(request, "admin");

        assertNotNull(response);
        assertEquals("PADUR-AG-01", response.getCode());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(userProjectRoleRepository, times(1)).save(any(UserProjectRole.class));
        verify(auditService, times(1)).logAction(eq("admin"), eq("CREATE_PROJECT"), eq("PROJECT"), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if project code exists")
    void testCreateProjectDuplicateCode() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .code("PADUR-AG-01")
                .name("Ashok Grandeur")
                .build();

        when(projectRepository.existsByCode("PADUR-AG-01")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> projectService.createProject(request, "admin"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve project detail by ID")
    void testGetProjectById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(userRepository.findById(10L)).thenReturn(Optional.of(samplePmUser));
        when(siteRepository.findByProjectId(1L)).thenReturn(List.of());
        when(userProjectRoleRepository.findByProjectId(1L)).thenReturn(List.of());

        ProjectDetailResponse detail = projectService.getProjectById(1L);

        assertNotNull(detail);
        assertEquals(1L, detail.getId());
        assertEquals("pm_karthik", detail.getProjectManagerName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown project ID")
    void testGetProjectByIdNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(999L));
    }

    @Test
    @DisplayName("Should update project status to ACTIVE and auto-set actualStartDate")
    void testUpdateProjectStatusToActive() {
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest();
        request.setStatus("ACTIVE");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(10L)).thenReturn(Optional.of(samplePmUser));

        ProjectResponse response = projectService.updateProjectStatus(1L, request, "admin");

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(sampleProject.getActualStartDate());
        assertEquals(LocalDate.now(), sampleProject.getActualStartDate());
        verify(auditService, times(1)).logAction(eq("admin"), eq("UPDATE_PROJECT_STATUS"), eq("PROJECT"), eq("1"), any(), any());
        verify(notificationService, times(1)).dispatchNotification(any());
    }

    @Test
    @DisplayName("Should update project status to COMPLETED and auto-set actualEndDate")
    void testUpdateProjectStatusToCompleted() {
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest();
        request.setStatus("COMPLETED");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(10L)).thenReturn(Optional.of(samplePmUser));

        ProjectResponse response = projectService.updateProjectStatus(1L, request, "admin");

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(sampleProject.getActualEndDate());
        assertEquals(LocalDate.now(), sampleProject.getActualEndDate());
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid project status")
    void testUpdateProjectStatusInvalid() {
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest();
        request.setStatus("INVALID_STATUS");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        assertThrows(BadRequestException.class, () -> projectService.updateProjectStatus(1L, request, "admin"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete project and clean up roles")
    void testDeleteProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        projectService.deleteProject(1L, "admin");

        verify(userProjectRoleRepository, times(1)).deleteByProjectId(1L);
        verify(projectRepository, times(1)).delete(sampleProject);
        verify(auditService, times(1)).logAction(eq("admin"), eq("DELETE_PROJECT"), eq("PROJECT"), eq("1"), any(), any());
    }

    @Test
    @DisplayName("Should return project metrics correctly")
    void testGetProjectMetrics() {
        when(projectRepository.count()).thenReturn(5L);
        when(projectRepository.countByStatus("ACTIVE")).thenReturn(2L);
        when(projectRepository.countByStatus("PLANNED")).thenReturn(1L);
        when(projectRepository.countByStatus("ON_HOLD")).thenReturn(1L);
        when(projectRepository.countByStatus("COMPLETED")).thenReturn(1L);
        when(projectRepository.sumTotalEstimatedBudget()).thenReturn(new BigDecimal("150000000.00"));
        when(siteRepository.count()).thenReturn(12L);

        ProjectMetricsResponse metrics = projectService.getProjectMetrics();

        assertNotNull(metrics);
        assertEquals(5L, metrics.getTotalProjects());
        assertEquals(2L, metrics.getActiveProjects());
        assertEquals(new BigDecimal("150000000.00"), metrics.getTotalEstimatedBudget());
        assertEquals(12L, metrics.getTotalSites());
    }
}

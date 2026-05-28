package com.opsbytemitope.service;

import com.opsbytemitope.dto.request.CreateProjectRequest;
import com.opsbytemitope.dto.request.UpdateProjectRequest;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.ProjectResponse;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.ProjectStatus;
import com.opsbytemitope.entity.Role;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ForbiddenException;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.repository.ProjectRepository;
import com.opsbytemitope.repository.TaskRepository;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ActivityEventService activityEventService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User owner;
    private Project sampleProject;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Johnson")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        sampleProject = Project.builder()
                .id(10L)
                .name("Website Redesign")
                .description("Redesign the company website")
                .status(ProjectStatus.ACTIVE)
                .dueDate(LocalDate.now().plusDays(60))
                .owner(owner)
                .tasks(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── createProject ────────────────────────────────────────

    @Test
    @DisplayName("createProject – success returns project response")
    void createProject_success() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Website Redesign");
        request.setDescription("Redesign the company website");
        request.setDueDate(LocalDate.now().plusDays(60));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        ProjectResponse response = projectService.createProject(request, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Website Redesign");
        assertThat(response.getOwner().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        verify(projectRepository).save(any(Project.class));
        verify(activityEventService).logProjectCreated(any(), any());
    }

    @Test
    @DisplayName("createProject – throws ResourceNotFoundException when user not found")
    void createProject_userNotFound_throws() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(request, "unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getProjectById ───────────────────────────────────────

    @Test
    @DisplayName("getProjectById – returns project for owner")
    void getProjectById_success() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        ProjectResponse response = projectService.getProjectById(10L, "alice@example.com");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Website Redesign");
    }

    @Test
    @DisplayName("getProjectById – throws ForbiddenException when non-owner requests")
    void getProjectById_nonOwner_throwsForbidden() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        assertThatThrownBy(() -> projectService.getProjectById(10L, "other@example.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("getProjectById – throws ResourceNotFoundException when not found")
    void getProjectById_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L, "alice@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getMyProjects ────────────────────────────────────────

    @Test
    @DisplayName("getMyProjects – returns paged list")
    void getMyProjects_returnsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findByOwnerId(eq(1L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(sampleProject), pageable, 1));

        PagedResponse<ProjectResponse> result = projectService.getMyProjects("alice@example.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Website Redesign");
    }

    // ── updateProject ────────────────────────────────────────

    @Test
    @DisplayName("updateProject – updates name and status")
    void updateProject_success() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Website Redesign v2");
        request.setStatus(ProjectStatus.ON_HOLD);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));

        ProjectResponse response = projectService.updateProject(10L, request, "alice@example.com");

        assertThat(response.getName()).isEqualTo("Website Redesign v2");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ON_HOLD);
    }

    @Test
    @DisplayName("updateProject – throws ForbiddenException for non-owner")
    void updateProject_nonOwner_throwsForbidden() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Hacked");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        assertThatThrownBy(() -> projectService.updateProject(10L, request, "hacker@example.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── deleteProject ────────────────────────────────────────

    @Test
    @DisplayName("deleteProject – deletes successfully for owner")
    void deleteProject_success() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        projectService.deleteProject(10L, "alice@example.com");

        verify(projectRepository).delete(sampleProject);
    }

    @Test
    @DisplayName("deleteProject – throws ForbiddenException for non-owner")
    void deleteProject_nonOwner_throwsForbidden() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(sampleProject));

        assertThatThrownBy(() -> projectService.deleteProject(10L, "stranger@example.com"))
                .isInstanceOf(ForbiddenException.class);
    }
}

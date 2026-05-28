package com.opsbytemitope.service;

import com.opsbytemitope.dto.request.CreateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskStatusRequest;
import com.opsbytemitope.dto.response.TaskResponse;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.ProjectStatus;
import com.opsbytemitope.entity.Role;
import com.opsbytemitope.entity.Task;
import com.opsbytemitope.entity.TaskPriority;
import com.opsbytemitope.entity.TaskStatus;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ForbiddenException;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.repository.ProjectRepository;
import com.opsbytemitope.repository.TaskRepository;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityEventService activityEventService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User owner;
    private Project project;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Johnson")
                .role(Role.USER)
                .enabled(true)
                .build();

        project = Project.builder()
                .id(10L)
                .name("Website Redesign")
                .status(ProjectStatus.ACTIVE)
                .owner(owner)
                .tasks(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        sampleTask = Task.builder()
                .id(100L)
                .title("Design homepage")
                .description("Create mockups for the new homepage")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(14))
                .project(project)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── createTask ───────────────────────────────────────────

    @Test
    @DisplayName("createTask – success creates and returns task")
    void createTask_success() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Design homepage");
        request.setDescription("Create mockups for the new homepage");
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(14));

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(10L, request, "alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Design homepage");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.getProjectId()).isEqualTo(10L);
        verify(taskRepository).save(any(Task.class));
        verify(activityEventService).logTaskCreated(any(), any());
    }

    @Test
    @DisplayName("createTask – throws ResourceNotFoundException when project not found")
    void createTask_projectNotFound_throws() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Orphan task");
        request.setPriority(TaskPriority.LOW);

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(999L, request, "alice@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createTask – throws ForbiddenException for non-project-owner")
    void createTask_nonOwner_throwsForbidden() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Sneaky task");
        request.setPriority(TaskPriority.LOW);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("hacker@example.com")).thenReturn(
                Optional.of(User.builder().id(99L).email("hacker@example.com")
                        .firstName("H").lastName("Hacker").role(Role.USER).build()));

        assertThatThrownBy(() -> taskService.createTask(10L, request, "hacker@example.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── getTaskById ──────────────────────────────────────────

    @Test
    @DisplayName("getTaskById – returns task for authorized user")
    void getTaskById_success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(sampleTask));

        TaskResponse response = taskService.getTaskById(100L, "alice@example.com");

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("Design homepage");
    }

    @Test
    @DisplayName("getTaskById – throws ResourceNotFoundException when task missing")
    void getTaskById_notFound_throws() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L, "alice@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateTaskStatus ─────────────────────────────────────

    @Test
    @DisplayName("updateTaskStatus – transitions TODO -> IN_PROGRESS")
    void updateTaskStatus_success() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));

        TaskResponse response = taskService.updateTaskStatus(100L, request, "alice@example.com");

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(activityEventService).logTaskStatusChanged(any(), any(), eq(TaskStatus.TODO), eq(TaskStatus.IN_PROGRESS));
    }

    // ── deleteTask ───────────────────────────────────────────

    @Test
    @DisplayName("deleteTask – deletes successfully for project owner")
    void deleteTask_success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(sampleTask));

        taskService.deleteTask(100L, "alice@example.com");

        verify(taskRepository).delete(sampleTask);
    }

    @Test
    @DisplayName("deleteTask – throws ForbiddenException for unauthorized user")
    void deleteTask_unauthorized_throwsForbidden() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(sampleTask));

        assertThatThrownBy(() -> taskService.deleteTask(100L, "stranger@example.com"))
                .isInstanceOf(ForbiddenException.class);
    }
}

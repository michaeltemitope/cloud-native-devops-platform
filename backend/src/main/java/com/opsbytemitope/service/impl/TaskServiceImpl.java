package com.opsbytemitope.service.impl;

import com.opsbytemitope.dto.request.CreateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskStatusRequest;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.TaskResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.Task;
import com.opsbytemitope.entity.TaskStatus;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ForbiddenException;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.repository.ProjectRepository;
import com.opsbytemitope.repository.TaskRepository;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.ActivityEventService;
import com.opsbytemitope.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of TaskService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityEventService activityEventService;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, String creatorEmail) {
        log.info("Creating task '{}' in project id={} by user={}", request.getTitle(), projectId, creatorEmail);

        Project project = findProject(projectId);
        User creator = findUserByEmail(creatorEmail);
        verifyProjectAccess(project, creatorEmail);

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .project(project)
                .createdBy(creator)
                .assignee(assignee)
                .build();

        Task saved = taskRepository.save(task);
        activityEventService.logTaskCreated(saved, creator);

        log.info("Task created. id={}, title={}", saved.getId(), saved.getTitle());
        return mapToTaskResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, String requestingUser) {
        Task task = findTask(taskId);
        verifyProjectAccess(task.getProject(), requestingUser);
        return mapToTaskResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasksByProject(Long projectId, String requestingUser, Pageable pageable) {
        Project project = findProject(projectId);
        verifyProjectAccess(project, requestingUser);

        Page<Task> page = taskRepository.findByProjectId(projectId, pageable);

        return PagedResponse.<TaskResponse>builder()
                .content(page.getContent().stream().map(this::mapToTaskResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getMyTasks(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        Page<Task> page = taskRepository.findByProjectId(user.getId(), pageable);

        // Use assignee query
        java.util.List<Task> tasks = taskRepository.findByAssigneeId(user.getId());

        return PagedResponse.<TaskResponse>builder()
                .content(tasks.stream().map(this::mapToTaskResponse).toList())
                .page(0)
                .size(tasks.size())
                .totalElements(tasks.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, String requestingUser) {
        log.info("Updating task id={} by user={}", taskId, requestingUser);

        Task task = findTask(taskId);
        verifyProjectAccess(task.getProject(), requestingUser);

        TaskStatus previousStatus = task.getStatus();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task updated = taskRepository.save(task);
        User actor = findUserByEmail(requestingUser);

        if (request.getStatus() != null && !request.getStatus().equals(previousStatus)) {
            activityEventService.logTaskStatusChanged(updated, actor, previousStatus, request.getStatus());
        } else {
            activityEventService.logTaskUpdated(updated, actor);
        }

        return mapToTaskResponse(updated);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, String requestingUser) {
        log.info("Updating task status. taskId={}, newStatus={}, user={}", taskId, request.getStatus(), requestingUser);

        Task task = findTask(taskId);
        verifyProjectAccess(task.getProject(), requestingUser);

        TaskStatus previousStatus = task.getStatus();
        task.setStatus(request.getStatus());

        Task updated = taskRepository.save(task);
        User actor = findUserByEmail(requestingUser);
        activityEventService.logTaskStatusChanged(updated, actor, previousStatus, request.getStatus());

        return mapToTaskResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, String requestingUser) {
        log.info("Deleting task id={} by user={}", taskId, requestingUser);

        Task task = findTask(taskId);
        verifyProjectAccess(task.getProject(), requestingUser);
        taskRepository.delete(task);

        log.info("Task deleted. id={}", taskId);
    }

    private void verifyProjectAccess(Project project, String userEmail) {
        if (!project.getOwner().getEmail().equals(userEmail)) {
            // In a full multi-tenant system this would also check team membership
            throw new ForbiddenException("You do not have permission to access this project");
        }
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignee(task.getAssignee() != null ? mapToUserResponse(task.getAssignee()) : null)
                .createdBy(mapToUserResponse(task.getCreatedBy()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}

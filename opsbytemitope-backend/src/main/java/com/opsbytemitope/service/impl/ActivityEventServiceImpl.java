package com.opsbytemitope.service.impl;

import com.opsbytemitope.dto.response.ActivityEventResponse;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.ActivityEvent;
import com.opsbytemitope.entity.EventType;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.Task;
import com.opsbytemitope.entity.TaskStatus;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.repository.ActivityEventRepository;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.ActivityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ActivityEventService for audit trail logging.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityEventServiceImpl implements ActivityEventService {

    private final ActivityEventRepository activityEventRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    @Transactional
    public void logTaskCreated(Task task, User actor) {
        persist(ActivityEvent.builder()
                .eventType(EventType.TASK_CREATED)
                .description(String.format("Task '%s' was created", task.getTitle()))
                .task(task)
                .project(task.getProject())
                .actor(actor)
                .build());
    }

    @Override
    @Async
    @Transactional
    public void logTaskUpdated(Task task, User actor) {
        persist(ActivityEvent.builder()
                .eventType(EventType.TASK_UPDATED)
                .description(String.format("Task '%s' was updated", task.getTitle()))
                .task(task)
                .project(task.getProject())
                .actor(actor)
                .build());
    }

    @Override
    @Async
    @Transactional
    public void logTaskStatusChanged(Task task, User actor, TaskStatus from, TaskStatus to) {
        persist(ActivityEvent.builder()
                .eventType(EventType.TASK_STATUS_CHANGED)
                .description(String.format("Task '%s' status changed from %s to %s", task.getTitle(), from, to))
                .task(task)
                .project(task.getProject())
                .actor(actor)
                .metadata(String.format("{\"from\":\"%s\",\"to\":\"%s\"}", from, to))
                .build());
    }

    @Override
    @Async
    @Transactional
    public void logTaskDeleted(Task task, User actor) {
        persist(ActivityEvent.builder()
                .eventType(EventType.TASK_DELETED)
                .description(String.format("Task '%s' was deleted", task.getTitle()))
                .project(task.getProject())
                .actor(actor)
                .build());
    }

    @Override
    @Async
    @Transactional
    public void logProjectCreated(Project project, User actor) {
        persist(ActivityEvent.builder()
                .eventType(EventType.PROJECT_CREATED)
                .description(String.format("Project '%s' was created", project.getName()))
                .project(project)
                .actor(actor)
                .build());
    }

    @Override
    @Async
    @Transactional
    public void logProjectUpdated(Project project, User actor) {
        persist(ActivityEvent.builder()
                .eventType(EventType.PROJECT_UPDATED)
                .description(String.format("Project '%s' was updated", project.getName()))
                .project(project)
                .actor(actor)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ActivityEventResponse> getProjectActivity(Long projectId, String requestingUser, Pageable pageable) {
        Page<ActivityEvent> page = activityEventRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable);

        return buildPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ActivityEventResponse> getMyActivity(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Page<ActivityEvent> page = activityEventRepository.findByActorIdOrderByCreatedAtDesc(user.getId(), pageable);
        return buildPagedResponse(page);
    }

    private void persist(ActivityEvent event) {
        try {
            activityEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to persist activity event: {}", e.getMessage(), e);
        }
    }

    private PagedResponse<ActivityEventResponse> buildPagedResponse(Page<ActivityEvent> page) {
        return PagedResponse.<ActivityEventResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private ActivityEventResponse mapToResponse(ActivityEvent event) {
        return ActivityEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .description(event.getDescription())
                .taskId(event.getTask() != null ? event.getTask().getId() : null)
                .taskTitle(event.getTask() != null ? event.getTask().getTitle() : null)
                .projectId(event.getProject() != null ? event.getProject().getId() : null)
                .projectName(event.getProject() != null ? event.getProject().getName() : null)
                .actor(mapToUserResponse(event.getActor()))
                .createdAt(event.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .build();
    }
}

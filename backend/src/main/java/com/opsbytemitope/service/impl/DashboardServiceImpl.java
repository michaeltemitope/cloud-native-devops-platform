package com.opsbytemitope.service.impl;

import com.opsbytemitope.dto.response.ActivityEventResponse;
import com.opsbytemitope.dto.response.DashboardResponse;
import com.opsbytemitope.dto.response.ProjectResponse;
import com.opsbytemitope.dto.response.TaskResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.ActivityEvent;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.Task;
import com.opsbytemitope.entity.TaskPriority;
import com.opsbytemitope.entity.TaskStatus;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.repository.ActivityEventRepository;
import com.opsbytemitope.repository.ProjectRepository;
import com.opsbytemitope.repository.TaskRepository;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard service providing aggregated metrics and summaries.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_ITEMS_LIMIT = 5;

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ActivityEventRepository activityEventRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "#userEmail")
    public DashboardResponse getDashboard(String userEmail) {
        log.debug("Building dashboard for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Project> allProjects = projectRepository.findActiveProjectsByOwner(user.getId());
        List<Task> openTasks = taskRepository.findByAssigneeId(user.getId());
        List<Task> myOpenTasks = openTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
                .toList();

        long totalTasks = allProjects.stream().mapToLong(p -> p.getTasks().size()).sum();
        long completedTasks = allProjects.stream()
                .flatMap(p -> p.getTasks().stream())
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
        long overdueTasks = allProjects.stream()
                .flatMap(p -> p.getTasks().stream())
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(LocalDate.now())
                        && t.getStatus() != TaskStatus.DONE
                        && t.getStatus() != TaskStatus.CANCELLED)
                .count();

        Map<String, Long> tasksByStatus = Arrays.stream(TaskStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> allProjects.stream()
                                .flatMap(p -> p.getTasks().stream())
                                .filter(t -> t.getStatus() == status)
                                .count()
                ));

        Map<String, Long> tasksByPriority = Arrays.stream(TaskPriority.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        priority -> allProjects.stream()
                                .flatMap(p -> p.getTasks().stream())
                                .filter(t -> t.getPriority() == priority)
                                .count()
                ));

        List<Project> recentProjects = allProjects.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(RECENT_ITEMS_LIMIT)
                .toList();

        List<ActivityEvent> recentEvents = activityEventRepository
                .findRecentByActor(user.getId(), LocalDateTime.now().minusDays(7));

        return DashboardResponse.builder()
                .totalProjects((long) allProjects.size())
                .activeProjects(allProjects.stream()
                        .filter(p -> p.getStatus().name().equals("ACTIVE"))
                        .count())
                .totalTasks(totalTasks)
                .openTasks((long) myOpenTasks.size())
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .recentProjects(recentProjects.stream().map(this::mapToProjectResponse).toList())
                .myOpenTasks(myOpenTasks.stream()
                        .limit(RECENT_ITEMS_LIMIT)
                        .map(this::mapToTaskResponse)
                        .toList())
                .recentActivity(recentEvents.stream()
                        .limit(RECENT_ITEMS_LIMIT)
                        .map(this::mapToActivityResponse)
                        .toList())
                .build();
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .dueDate(project.getDueDate())
                .owner(mapToUserResponse(project.getOwner()))
                .totalTasks((long) project.getTasks().size())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignee(task.getAssignee() != null ? mapToUserResponse(task.getAssignee()) : null)
                .build();
    }

    private ActivityEventResponse mapToActivityResponse(ActivityEvent event) {
        return ActivityEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .description(event.getDescription())
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

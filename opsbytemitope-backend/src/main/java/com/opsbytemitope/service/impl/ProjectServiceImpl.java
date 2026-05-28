package com.opsbytemitope.service.impl;

import com.opsbytemitope.dto.request.CreateProjectRequest;
import com.opsbytemitope.dto.request.UpdateProjectRequest;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.ProjectResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.TaskStatus;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ForbiddenException;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.repository.ProjectRepository;
import com.opsbytemitope.repository.TaskRepository;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.ActivityEventService;
import com.opsbytemitope.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProjectService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ActivityEventService activityEventService;

    @Override
    @Transactional
    @CacheEvict(value = "projects", key = "#ownerEmail")
    public ProjectResponse createProject(CreateProjectRequest request, String ownerEmail) {
        log.info("Creating project '{}' for user: {}", request.getName(), ownerEmail);

        User owner = findUserByEmail(ownerEmail);

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .owner(owner)
                .build();

        Project saved = projectRepository.save(project);
        activityEventService.logProjectCreated(saved, owner);

        log.info("Project created. id={}, name={}", saved.getId(), saved.getName());
        return mapToProjectResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, String requestingUserEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        verifyOwnership(project, requestingUserEmail);
        return mapToProjectResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#ownerEmail + '_' + #pageable.pageNumber")
    public PagedResponse<ProjectResponse> getMyProjects(String ownerEmail, Pageable pageable) {
        User owner = findUserByEmail(ownerEmail);
        Page<Project> page = projectRepository.findByOwnerId(owner.getId(), pageable);

        return PagedResponse.<ProjectResponse>builder()
                .content(page.getContent().stream().map(this::mapToProjectResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "projects", key = "#ownerEmail")
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, String ownerEmail) {
        log.info("Updating project id={} for user={}", projectId, ownerEmail);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        verifyOwnership(project, ownerEmail);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            project.setDueDate(request.getDueDate());
        }

        Project updated = projectRepository.save(project);
        User actor = findUserByEmail(ownerEmail);
        activityEventService.logProjectUpdated(updated, actor);

        return mapToProjectResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "projects", key = "#ownerEmail")
    public void deleteProject(Long projectId, String ownerEmail) {
        log.info("Deleting project id={} for user={}", projectId, ownerEmail);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        verifyOwnership(project, ownerEmail);
        projectRepository.delete(project);

        log.info("Project deleted. id={}", projectId);
    }

    private void verifyOwnership(Project project, String userEmail) {
        if (!project.getOwner().getEmail().equals(userEmail)) {
            throw new ForbiddenException("You do not have permission to access this project");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        long totalTasks = taskRepository.countByProjectIdAndStatus(project.getId(), null) == 0
                ? project.getTasks().size()
                : project.getTasks().size();
        long completedTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .dueDate(project.getDueDate())
                .owner(mapToUserResponse(project.getOwner()))
                .totalTasks((long) project.getTasks().size())
                .completedTasks(completedTasks)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
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

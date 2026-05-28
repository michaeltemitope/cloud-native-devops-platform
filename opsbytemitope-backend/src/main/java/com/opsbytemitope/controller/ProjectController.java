package com.opsbytemitope.controller;

import com.opsbytemitope.dto.request.CreateProjectRequest;
import com.opsbytemitope.dto.request.UpdateProjectRequest;
import com.opsbytemitope.dto.response.ApiResponse;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.ProjectResponse;
import com.opsbytemitope.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project management REST controller.
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/v1/projects - user={}", userDetails.getUsername());
        ProjectResponse project = projectService.createProject(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProjectResponse project = projectService.getProjectById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @GetMapping
    @Operation(summary = "List all projects for the authenticated user")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal UserDetails userDetails) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ProjectResponse> projects = projectService.getMyProjects(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/v1/projects/{} - user={}", id, userDetails.getUsername());
        ProjectResponse project = projectService.updateProject(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/v1/projects/{} - user={}", id, userDetails.getUsername());
        projectService.deleteProject(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }
}

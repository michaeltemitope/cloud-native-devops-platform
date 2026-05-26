package com.opsbytemitope.controller;

import com.opsbytemitope.dto.request.CreateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskStatusRequest;
import com.opsbytemitope.dto.response.ApiResponse;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.TaskResponse;
import com.opsbytemitope.service.TaskService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Task management REST controller.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Create a new task in a project")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/v1/projects/{}/tasks - user={}", projectId, userDetails.getUsername());
        TaskResponse task = taskService.createTask(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskResponse task = taskService.getTaskById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(summary = "List all tasks in a project")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getProjectTasks(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal UserDetails userDetails) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<TaskResponse> tasks = taskService.getTasksByProject(projectId, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/tasks/me")
    @Operation(summary = "List all tasks assigned to the authenticated user")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<TaskResponse> tasks = taskService.getMyTasks(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/v1/tasks/{} - user={}", id, userDetails.getUsername());
        TaskResponse task = taskService.updateTask(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @PatchMapping("/tasks/{id}/status")
    @Operation(summary = "Update only the status of a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PATCH /api/v1/tasks/{}/status - status={}, user={}", id, request.getStatus(), userDetails.getUsername());
        TaskResponse task = taskService.updateTaskStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task status updated", task));
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/v1/tasks/{} - user={}", id, userDetails.getUsername());
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}

package com.opsbytemitope.service;

import com.opsbytemitope.dto.request.CreateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskRequest;
import com.opsbytemitope.dto.request.UpdateTaskStatusRequest;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

/**
 * Task management service interface.
 */
public interface TaskService {

    /**
     * Creates a new task within a project.
     *
     * @param projectId  the project ID
     * @param request    task creation data
     * @param creatorEmail the creating user's email
     * @return the created task
     */
    TaskResponse createTask(Long projectId, CreateTaskRequest request, String creatorEmail);

    /**
     * Retrieves a task by ID.
     *
     * @param taskId         the task ID
     * @param requestingUser the requesting user's email
     * @return the task
     */
    TaskResponse getTaskById(Long taskId, String requestingUser);

    /**
     * Lists all tasks within a project.
     *
     * @param projectId    the project ID
     * @param requestingUser the requesting user's email
     * @param pageable     pagination parameters
     * @return paged list of tasks
     */
    PagedResponse<TaskResponse> getTasksByProject(Long projectId, String requestingUser, Pageable pageable);

    /**
     * Lists all tasks assigned to the authenticated user.
     *
     * @param userEmail the user's email
     * @return list of assigned tasks
     */
    PagedResponse<TaskResponse> getMyTasks(String userEmail, Pageable pageable);

    /**
     * Updates a task.
     *
     * @param taskId       the task ID
     * @param request      update data
     * @param requestingUser the requesting user's email
     * @return updated task
     */
    TaskResponse updateTask(Long taskId, UpdateTaskRequest request, String requestingUser);

    /**
     * Updates only the status of a task.
     *
     * @param taskId       the task ID
     * @param request      the new status
     * @param requestingUser the requesting user's email
     * @return updated task
     */
    TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, String requestingUser);

    /**
     * Deletes a task.
     *
     * @param taskId       the task ID
     * @param requestingUser the requesting user's email
     */
    void deleteTask(Long taskId, String requestingUser);
}

package com.opsbytemitope.service;

import com.opsbytemitope.dto.request.CreateProjectRequest;
import com.opsbytemitope.dto.request.UpdateProjectRequest;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.ProjectResponse;
import org.springframework.data.domain.Pageable;

/**
 * Project management service interface.
 */
public interface ProjectService {

    /**
     * Creates a new project for the authenticated user.
     *
     * @param request project creation data
     * @param ownerEmail the authenticated user's email
     * @return the created project
     */
    ProjectResponse createProject(CreateProjectRequest request, String ownerEmail);

    /**
     * Retrieves a project by ID.
     *
     * @param projectId the project ID
     * @param requestingUserEmail the requesting user's email
     * @return the project
     */
    ProjectResponse getProjectById(Long projectId, String requestingUserEmail);

    /**
     * Lists all projects belonging to the authenticated user.
     *
     * @param ownerEmail the authenticated user's email
     * @param pageable pagination parameters
     * @return paged list of projects
     */
    PagedResponse<ProjectResponse> getMyProjects(String ownerEmail, Pageable pageable);

    /**
     * Updates a project.
     *
     * @param projectId the project ID
     * @param request update data
     * @param ownerEmail the authenticated user's email
     * @return updated project
     */
    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, String ownerEmail);

    /**
     * Deletes a project and all its tasks.
     *
     * @param projectId the project ID
     * @param ownerEmail the authenticated user's email
     */
    void deleteProject(Long projectId, String ownerEmail);
}

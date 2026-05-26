package com.opsbytemitope.dto.request;

import com.opsbytemitope.entity.ProjectStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for updating an existing project.
 */
@Data
public class UpdateProjectRequest {

    @Size(min = 3, max = 150, message = "Project name must be between 3 and 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private ProjectStatus status;

    private LocalDate dueDate;
}

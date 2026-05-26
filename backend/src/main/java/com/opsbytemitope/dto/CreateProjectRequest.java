package com.opsbytemitope.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for creating a new project.
 */
@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 150, message = "Project name must be between 3 and 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;
}i

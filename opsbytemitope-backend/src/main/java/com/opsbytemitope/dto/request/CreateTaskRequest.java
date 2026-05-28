package com.opsbytemitope.dto.request;

import com.opsbytemitope.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for creating a new task.
 */
@Data
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private LocalDate dueDate;

    private Long assigneeId;
}

package com.opsbytemitope.dto.request;

import com.opsbytemitope.entity.TaskPriority;
import com.opsbytemitope.entity.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for updating an existing task.
 */
@Data
public class UpdateTaskRequest {

    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private Long assigneeId;
}

package com.opsbytemitope.dto.response;

import com.opsbytemitope.entity.TaskPriority;
import com.opsbytemitope.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task data returned in API responses.
 */
@Data
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Long projectId;
    private String projectName;
    private UserResponse assignee;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

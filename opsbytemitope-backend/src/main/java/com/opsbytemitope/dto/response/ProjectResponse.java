package com.opsbytemitope.dto.response;

import com.opsbytemitope.entity.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Project data returned in API responses.
 */
@Data
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate dueDate;
    private UserResponse owner;
    private Long totalTasks;
    private Long completedTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

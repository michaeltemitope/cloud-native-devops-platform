package com.opsbytemitope.dto.response;

import com.opsbytemitope.entity.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Activity event data returned in API responses.
 */
@Data
@Builder
public class ActivityEventResponse {

    private Long id;
    private EventType eventType;
    private String description;
    private Long taskId;
    private String taskTitle;
    private Long projectId;
    private String projectName;
    private UserResponse actor;
    private LocalDateTime createdAt;
}

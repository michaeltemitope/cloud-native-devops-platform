package com.opsbytemitope.dto.request;

import com.opsbytemitope.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for updating a task status only.
 */
@Data
public class UpdateTaskStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}

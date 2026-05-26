package com.opsbytemitope.service;

import com.opsbytemitope.dto.response.ActivityEventResponse;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.Task;
import com.opsbytemitope.entity.TaskStatus;
import com.opsbytemitope.entity.User;
import org.springframework.data.domain.Pageable;

/**
 * Activity event logging service.
 */
public interface ActivityEventService {

    void logTaskCreated(Task task, User actor);

    void logTaskUpdated(Task task, User actor);

    void logTaskStatusChanged(Task task, User actor, TaskStatus from, TaskStatus to);

    void logTaskDeleted(Task task, User actor);

    void logProjectCreated(Project project, User actor);

    void logProjectUpdated(Project project, User actor);

    PagedResponse<ActivityEventResponse> getProjectActivity(Long projectId, String requestingUser, Pageable pageable);

    PagedResponse<ActivityEventResponse> getMyActivity(String userEmail, Pageable pageable);
}

package com.opsbytemitope.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Dashboard summary data for the authenticated user.
 */
@Data
@Builder
public class DashboardResponse {

    private Long totalProjects;
    private Long activeProjects;
    private Long totalTasks;
    private Long openTasks;
    private Long completedTasks;
    private Long overdueTasks;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
    private List<ProjectResponse> recentProjects;
    private List<TaskResponse> myOpenTasks;
    private List<ActivityEventResponse> recentActivity;
}

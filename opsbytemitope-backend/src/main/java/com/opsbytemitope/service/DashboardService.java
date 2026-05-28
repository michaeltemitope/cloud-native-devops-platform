package com.opsbytemitope.service;

import com.opsbytemitope.dto.response.DashboardResponse;

/**
 * Dashboard summary service.
 */
public interface DashboardService {

    /**
     * Returns dashboard summary data for the authenticated user.
     *
     * @param userEmail the user's email
     * @return dashboard summary
     */
    DashboardResponse getDashboard(String userEmail);
}

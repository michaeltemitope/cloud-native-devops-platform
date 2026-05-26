package com.opsbytemitope.controller;

import com.opsbytemitope.dto.response.ActivityEventResponse;
import com.opsbytemitope.dto.response.ApiResponse;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.service.ActivityEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Activity event REST controller for audit trail and notification feeds.
 */
@RestController
@RequestMapping("/api/v1/activity")
@Tag(name = "Activity", description = "Activity event and audit trail endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class ActivityEventController {

    private final ActivityEventService activityEventService;

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "Get activity feed for a project")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityEventResponse>>> getProjectActivity(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ActivityEventResponse> events =
                activityEventService.getProjectActivity(projectId, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's activity feed")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityEventResponse>>> getMyActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ActivityEventResponse> events =
                activityEventService.getMyActivity(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}

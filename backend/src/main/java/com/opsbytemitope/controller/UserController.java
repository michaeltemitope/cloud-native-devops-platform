package com.opsbytemitope.controller;

import com.opsbytemitope.dto.response.ApiResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User management REST controller.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and management endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(user)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(user)));
    }

    @GetMapping
    @Operation(summary = "List all users (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

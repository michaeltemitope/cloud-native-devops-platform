package com.opsbytemitope.dto.response;

import com.opsbytemitope.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Public-safe user data returned in API responses.
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}

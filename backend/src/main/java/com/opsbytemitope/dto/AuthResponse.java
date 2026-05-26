package com.opsbytemitope.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response body containing JWT token after successful authentication.
 */
@Data
@Builder
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private UserResponse user;
}

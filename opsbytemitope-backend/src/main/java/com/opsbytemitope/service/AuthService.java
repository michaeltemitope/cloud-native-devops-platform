package com.opsbytemitope.service;

import com.opsbytemitope.dto.request.LoginRequest;
import com.opsbytemitope.dto.request.RegisterRequest;
import com.opsbytemitope.dto.response.AuthResponse;

/**
 * Authentication service interface.
 */
public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param request registration data
     * @return authentication response with JWT token
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password.
     *
     * @param request login credentials
     * @return authentication response with JWT token
     */
    AuthResponse login(LoginRequest request);
}

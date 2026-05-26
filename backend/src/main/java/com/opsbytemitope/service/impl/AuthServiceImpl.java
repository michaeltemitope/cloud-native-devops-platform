package com.opsbytemitope.service.impl;

import com.opsbytemitope.dto.request.LoginRequest;
import com.opsbytemitope.dto.request.RegisterRequest;
import com.opsbytemitope.dto.response.AuthResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ConflictException;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.AuthService;
import com.opsbytemitope.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService handling registration and login.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email address is already in use: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully. id={}, email={}", saved.getId(), saved.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(token, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after successful auth"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        log.info("User logged in successfully. email={}", request.getEmail());
        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .expiresIn(jwtUtil.getExpirationMs())
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
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

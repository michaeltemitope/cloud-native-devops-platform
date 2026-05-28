package com.opsbytemitope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbytemitope.config.TestSecurityConfig;
import com.opsbytemitope.dto.request.LoginRequest;
import com.opsbytemitope.dto.request.RegisterRequest;
import com.opsbytemitope.dto.response.AuthResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.Role;
import com.opsbytemitope.exception.ConflictException;
import com.opsbytemitope.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController MVC Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthResponse buildAuthResponse() {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Johnson")
                .fullName("Alice Johnson")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        return AuthResponse.builder()
                .accessToken("mocked.jwt.token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(user)
                .build();
    }

    // ── POST /api/v1/auth/register ───────────────────────────

    @Test
    @DisplayName("POST /register – 201 with valid payload")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(authService.register(any())).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("POST /register – 400 when email is blank")
    void register_blankEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setEmail("");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /register – 400 when email format is invalid")
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setEmail("not-an-email");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register – 400 when password is too short")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setEmail("alice@example.com");
        request.setPassword("short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register – 409 when email already exists")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(authService.register(any()))
                .thenThrow(new ConflictException("Email already in use"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ── POST /api/v1/auth/login ──────────────────────────────

    @Test
    @DisplayName("POST /login – 200 with valid credentials")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(authService.login(any())).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.firstName").value("Alice"));
    }

    @Test
    @DisplayName("POST /login – 401 with bad credentials")
    void login_badCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("WrongPassword!");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /login – 400 when request body is empty")
    void login_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

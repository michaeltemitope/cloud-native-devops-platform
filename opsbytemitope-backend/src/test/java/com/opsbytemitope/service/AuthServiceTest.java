package com.opsbytemitope.service;

import com.opsbytemitope.dto.request.LoginRequest;
import com.opsbytemitope.dto.request.RegisterRequest;
import com.opsbytemitope.dto.response.AuthResponse;
import com.opsbytemitope.entity.Role;
import com.opsbytemitope.entity.User;
import com.opsbytemitope.exception.ConflictException;
import com.opsbytemitope.repository.UserRepository;
import com.opsbytemitope.service.impl.AuthServiceImpl;
import com.opsbytemitope.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("$2a$12$encodedPassword")
                .firstName("Alice")
                .lastName("Johnson")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── register ─────────────────────────────────────────────

    @Test
    @DisplayName("register – success returns token and user details")
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice@example.com")
                .password("$2a$12$encodedPassword")
                .authorities(List.of())
                .build();
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getUser().getFirstName()).isEqualTo("Alice");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register – throws ConflictException when email already exists")
    void register_emailAlreadyExists_throwsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");
        request.setFirstName("Alice");
        request.setLastName("Johnson");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("alice@example.com");

        verify(userRepository, never()).save(any());
    }

    // ── login ────────────────────────────────────────────────

    @Test
    @DisplayName("login – success returns token and user details")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice@example.com")
                .password("$2a$12$encodedPassword")
                .authorities(List.of())
                .build();
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("login – throws BadCredentialsException on wrong password")
    void login_wrongPassword_throwsBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("WrongPassword!");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login – password is never included in the response")
    void login_responseDoesNotExposePassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice@example.com")
                .password("$2a$12$encodedPassword")
                .authorities(List.of())
                .build();
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");
        when(jwtUtil.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        // UserResponse must never carry the hashed password
        assertThat(response.getUser()).hasNoNullFieldsOrPropertiesExcept("createdAt");
        assertThat(response.toString()).doesNotContain("encodedPassword");
    }
}

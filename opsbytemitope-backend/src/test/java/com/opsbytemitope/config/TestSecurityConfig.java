package com.opsbytemitope.config;

import com.opsbytemitope.util.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security configuration for controller slice tests.
 * Permits all requests so that authentication does not interfere with
 * endpoint logic being tested in isolation.
 */
@TestConfiguration
public class TestSecurityConfig {

    @MockBean
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}

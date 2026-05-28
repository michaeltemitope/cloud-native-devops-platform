package com.opsbytemitope.service;

import com.opsbytemitope.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hs256-minimum-256-bits-okay";
    private static final Long EXPIRATION_MS = 3600000L; // 1 h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", EXPIRATION_MS);
    }

    private UserDetails buildUserDetails(String email) {
        return User.withUsername(email)
                .password("irrelevant")
                .authorities(List.of())
                .build();
    }

    @Test
    @DisplayName("generateToken – returns non-blank token")
    void generateToken_returnsNonBlankToken() {
        UserDetails user = buildUserDetails("alice@example.com");
        String token = jwtUtil.generateToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername – returns correct subject")
    void extractUsername_returnsCorrectEmail() {
        UserDetails user = buildUserDetails("alice@example.com");
        String token = jwtUtil.generateToken(user);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("isTokenValid – valid token for same user returns true")
    void isTokenValid_validToken_returnsTrue() {
        UserDetails user = buildUserDetails("alice@example.com");
        String token = jwtUtil.generateToken(user);
        assertThat(jwtUtil.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid – token for different user returns false")
    void isTokenValid_differentUser_returnsFalse() {
        UserDetails alice = buildUserDetails("alice@example.com");
        UserDetails bob = buildUserDetails("bob@example.com");
        String token = jwtUtil.generateToken(alice);
        assertThat(jwtUtil.isTokenValid(token, bob)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid – malformed token returns false")
    void isTokenValid_malformedToken_returnsFalse() {
        UserDetails user = buildUserDetails("alice@example.com");
        assertThat(jwtUtil.isTokenValid("not.a.valid.token", user)).isFalse();
    }

    @Test
    @DisplayName("generateToken – two tokens for same user are different (timestamp)")
    void generateToken_twoCallsProduce_differentTokens() throws InterruptedException {
        UserDetails user = buildUserDetails("alice@example.com");
        String token1 = jwtUtil.generateToken(user);
        Thread.sleep(10);
        String token2 = jwtUtil.generateToken(user);
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("getExpirationMs – returns configured value")
    void getExpirationMs_returnsConfiguredValue() {
        assertThat(jwtUtil.getExpirationMs()).isEqualTo(EXPIRATION_MS);
    }
}

package com.curtaincall.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        authService = new AuthService(authenticationManager);
    }

    @Test
    @DisplayName("자격증명이 유효하면 인증된 Authentication을 반환한다")
    void authenticateReturnsAuthentication() {
        Authentication authenticated = new TestingAuthenticationToken("hong@example.com", null);
        given(authenticationManager.authenticate(any())).willReturn(authenticated);

        Authentication result = authService.authenticate("hong@example.com", "password123");

        assertThat(result).isSameAs(authenticated);
    }

    @Test
    @DisplayName("자격증명이 올바르지 않으면 BadCredentialsException을 전파한다")
    void authenticatePropagatesBadCredentials() {
        willThrow(new BadCredentialsException("bad"))
                .given(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.authenticate("hong@example.com", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }
}

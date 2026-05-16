package com.ga.capstone.controllers;


import com.ga.capstone.dto.LoginRequest;
import com.ga.capstone.dto.PasswordChangeRequest;
import com.ga.capstone.dto.PasswordResetRequest;
import com.ga.capstone.dto.RegisterRequest;
import com.ga.capstone.models.User;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    // ============ TEST DATA ============
    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        registerRequest = RegisterRequest.builder()
                .email("newuser@test.com")
                .password("Password1")
                .build();

        loginRequest = new LoginRequest("user@test.com", "password123");
    }

    // ============ TEST: login ============
    @Test
    @DisplayName("login: valid credentials → returns OK with JWT token")
    void login_validCredentials_returnsOkWithToken() {
        when(authService.login("user@test.com", "password123")).thenReturn("jwt-token-123");

        ResponseEntity<SuccessResponse> result = authController.login(loginRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Login successful");
        assertThat(result.getBody().data()).isEqualTo(Map.of("token", "jwt-token-123"));

        verify(authService, times(1)).login("user@test.com", "password123");
    }

    // ============ TEST: register ============
    @Test
    @DisplayName("register: valid request → returns CREATED with user data")
    void register_validRequest_returnsCreated() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        ResponseEntity<SuccessResponse> result = authController.register(registerRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Registration successful. Please check your email to verify your account.");
        assertThat(result.getBody().data()).isEqualTo(user);

        verify(authService, times(1)).register(registerRequest);
    }

    // ============ TEST: verifyEmail ============
    @Test
    @DisplayName("verifyEmail: valid token → returns OK with success message")
    void verifyEmail_validToken_returnsOk() {
        doNothing().when(authService).verifyEmail("valid-token");

        ResponseEntity<SuccessResponse> result = authController.verifyEmail("valid-token");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Email verified successfully. You can now log in.");
        assertThat(result.getBody().data()).isNull();

        verify(authService, times(1)).verifyEmail("valid-token");
    }

    // ============ TEST: resendVerification ============
    @Test
    @DisplayName("resendVerification: valid email → returns OK")
    void resendVerification_validEmail_returnsOk() {
        doNothing().when(authService).resendVerificationEmail("user@test.com");

        ResponseEntity<SuccessResponse> result = authController.resendVerification("user@test.com");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Verification email resent. Please check your inbox.");
        assertThat(result.getBody().data()).isNull();

        verify(authService, times(1)).resendVerificationEmail("user@test.com");
    }

    // ============ TEST: requestPasswordReset ============
    @Test
    @DisplayName("requestPasswordReset: valid email → returns OK")
    void requestPasswordReset_validEmail_returnsOk() {
        PasswordResetRequest request = new PasswordResetRequest("user@test.com");
        doNothing().when(authService).requestPasswordReset("user@test.com");

        ResponseEntity<SuccessResponse> result = authController.requestPasswordReset(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("If an account exists with this email, a password reset link has been sent.");
        assertThat(result.getBody().data()).isNull();

        verify(authService, times(1)).requestPasswordReset("user@test.com");
    }

    // ============ TEST: resetPassword ============
    @Test
    @DisplayName("resetPassword: valid token and password → returns OK")
    void resetPassword_validTokenAndPassword_returnsOk() {
        PasswordChangeRequest request = new PasswordChangeRequest("valid-token", "NewPass1");
        doNothing().when(authService).resetPassword("valid-token", "NewPass1");

        ResponseEntity<SuccessResponse> result = authController.resetPassword(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Password reset successful. You can now log in with your new password.");
        assertThat(result.getBody().data()).isNull();

        verify(authService, times(1)).resetPassword("valid-token", "NewPass1");
    }
}

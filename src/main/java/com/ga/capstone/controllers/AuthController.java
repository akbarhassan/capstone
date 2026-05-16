package com.ga.capstone.controllers;


import com.ga.capstone.dto.LoginRequest;
import com.ga.capstone.dto.PasswordChangeRequest;
import com.ga.capstone.dto.PasswordResetRequest;
import com.ga.capstone.dto.RegisterRequest;
import com.ga.capstone.services.AuthService;
import com.ga.capstone.models.User;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.utils.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for authentication endpoints: login, register,
 * email verification, and password reset. All endpoints are public.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    /**
     * Authenticate user and return JWT token.
     *
     * @param loginRequest email and password
     * @return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseBuilder.success(HttpStatus.OK, "Login successful", Map.of("token", token));
    }

    /**
     * Register a new user account. Sends verification email.
     *
     * @param registerRequest email and password
     * @return the created user
     */
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User newUser = authService.register(registerRequest);
        return ResponseBuilder.success(HttpStatus.CREATED, "Registration successful. Please check your email to verify your account.", newUser);
    }


    /**
     * Verify email address using token from verification link.
     *
     * @param token the verification token
     * @return success message
     */
    @GetMapping("/verify-email")
    public ResponseEntity<SuccessResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseBuilder.success(
                HttpStatus.OK,
                "Email verified successfully. You can now log in.",
                null
        );
    }

    /**
     * Resend verification email if user didn't receive it.
     *
     * @param email the user's email
     * @return success message
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<SuccessResponse> resendVerification(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseBuilder.success(
                HttpStatus.OK,
                "Verification email resent. Please check your inbox.",
                null
        );
    }

    /**
     * Request password reset (sends email with reset link).
     *
     * @param request contains the user's email
     * @return success message
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<SuccessResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseBuilder.success(
                HttpStatus.OK,
                "If an account exists with this email, a password reset link has been sent.",
                null
        );
    }

    /**
     * Reset password using token from reset email.
     *
     * @param request contains token and new password
     * @return success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.resetPassword(request.token(), request.password());
        return ResponseBuilder.success(
                HttpStatus.OK,
                "Password reset successful. You can now log in with your new password.",
                null
        );
    }
}

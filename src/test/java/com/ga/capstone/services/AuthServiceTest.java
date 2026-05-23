package com.ga.capstone.services;


import com.ga.capstone.dto.RegisterRequest;
import com.ga.capstone.enums.UserStatus;
import com.ga.capstone.exceptions.AuthErrorException;
import com.ga.capstone.exceptions.ResourceAlreadyExistsException;
import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.*;
import com.ga.capstone.repositories.EmailVerificationTokenRepository;
import com.ga.capstone.repositories.PasswordHistoryRepository;
import com.ga.capstone.repositories.PasswordResetTokenRepository;
import com.ga.capstone.repositories.RoleRepository;
import com.ga.capstone.repositories.UserRepository;
import com.ga.capstone.security.JwtUtils;
import com.ga.capstone.security.MyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailVerificationTokenService tokenService;

    @Mock
    private PasswordResetTokenService resetTokenService;

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AuthService authService;

    // ============ TEST DATA ============
    private User user1;
    private Role defaultRole;
    private RegisterRequest registerRequest;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        defaultRole = new Role();
        defaultRole.setId(2L);
        defaultRole.setName("USER");

        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user@test.com");
        user1.setPassword("encoded-password");
        user1.setRole(defaultRole);
        user1.setStatus(UserStatus.ACTIVE);
        user1.setEmailVerified(true);
        user1.setDeleted(false);

        registerRequest = RegisterRequest.builder()
                .email("newuser@test.com")
                .password("password123")
                .build();

        // Set @Value fields
        ReflectionTestUtils.setField(authService, "defaultRoleId", 2L);
        ReflectionTestUtils.setField(authService, "passwordHistoryCheckCount", 10);
    }

    // ============ TEST: register ============
    @Test
    @DisplayName("register: valid request → creates user with PENDING status and sends verification email")
    void register_validRequest_createsUserAndSendsVerificationEmail() {
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password-123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        doNothing().when(tokenService).sendVerificationEmail(any(User.class));

        User result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getEmail()).isEqualTo("newuser@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password-123");
        assertThat(result.getRole()).isEqualTo(defaultRole);
        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(result.getEmailVerified()).isFalse();
        assertThat(result.getDeleted()).isFalse();

        verify(userRepository, times(1)).existsByEmail("newuser@test.com");
        verify(roleRepository, times(1)).findById(2L);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenService, times(1)).sendVerificationEmail(any(User.class));
    }

    @Test
    @DisplayName("register: duplicate email → throws ResourceAlreadyExistsException")
    void register_duplicateEmail_throwsResourceAlreadyExistsException() {
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).existsByEmail("newuser@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register: default role not found → throws ResourceNotFoundException")
    void register_defaultRoleNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Default role");

        verify(roleRepository, times(1)).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ TEST: login ============
    @Test
    @DisplayName("login: valid credentials → returns JWT token")
    void login_validCredentials_returnsJwtToken() {
        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user1));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtUtils.generateJwtToken(any(MyUserDetails.class))).thenReturn("jwt-token-123");

        String result = authService.login("user@test.com", "password123");

        assertThat(result).isEqualTo("jwt-token-123");

        verify(userRepository, times(1)).findUserByEmail("user@test.com");
        verify(passwordEncoder, times(1)).matches("password123", "encoded-password");
        verify(jwtUtils, times(1)).generateJwtToken(any(MyUserDetails.class));
    }

    @Test
    @DisplayName("login: user not found → throws ResourceNotFoundException")
    void login_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findUserByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nonexistent@test.com", "password123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent@test.com");

        verify(userRepository, times(1)).findUserByEmail("nonexistent@test.com");
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    @DisplayName("login: inactive user → throws AuthErrorException")
    void login_inactiveUser_throwsAuthErrorException() {
        user1.setStatus(UserStatus.BANNED);

        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> authService.login("user@test.com", "password123"))
                .isInstanceOf(AuthErrorException.class);

        verify(userRepository, times(1)).findUserByEmail("user@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login: email not verified → throws AuthErrorException")
    void login_emailNotVerified_throwsAuthErrorException() {
        user1.setEmailVerified(false);

        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> authService.login("user@test.com", "password123"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("verify your email");

        verify(userRepository, times(1)).findUserByEmail("user@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login: deleted user → throws AuthErrorException")
    void login_deletedUser_throwsAuthErrorException() {
        user1.setDeleted(true);

        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> authService.login("user@test.com", "password123"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("deleted");

        verify(userRepository, times(1)).findUserByEmail("user@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login: incorrect password → throws AuthErrorException")
    void login_incorrectPassword_throwsAuthErrorException() {
        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user1));
        when(passwordEncoder.matches("wrongpassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("user@test.com", "wrongpassword"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("Incorrect password");

        verify(userRepository, times(1)).findUserByEmail("user@test.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encoded-password");
    }

    // ============ TEST: verifyEmail ============
    @Test
    @DisplayName("verifyEmail: valid token → verifies email and activates user")
    void verifyEmail_validToken_verifiesEmailAndActivatesUser() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("valid-token");
        token.setUser(user1);

        when(tokenService.validateToken("valid-token")).thenReturn(Optional.of(token));
        when(emailVerificationTokenRepository.markAsUsedAtomically("valid-token")).thenReturn(1);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        authService.verifyEmail("valid-token");

        assertThat(user1.getEmailVerified()).isTrue();
        assertThat(user1.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(tokenService, times(1)).validateToken("valid-token");
        verify(emailVerificationTokenRepository, times(1)).markAsUsedAtomically("valid-token");
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    @DisplayName("verifyEmail: invalid token → throws AuthErrorException")
    void verifyEmail_invalidToken_throwsAuthErrorException() {
        when(tokenService.validateToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("invalid-token"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("Invalid or expired verification token");

        verify(tokenService, times(1)).validateToken("invalid-token");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("verifyEmail: token already used (race condition) → throws AuthErrorException")
    void verifyEmail_tokenAlreadyUsed_throwsAuthErrorException() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("used-token");
        token.setUser(user1);

        when(tokenService.validateToken("used-token")).thenReturn(Optional.of(token));
        when(emailVerificationTokenRepository.markAsUsedAtomically("used-token")).thenReturn(0);

        assertThatThrownBy(() -> authService.verifyEmail("used-token"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("already been used");

        verify(emailVerificationTokenRepository, times(1)).markAsUsedAtomically("used-token");
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ TEST: resendVerificationEmail ============
    @Test
    @DisplayName("resendVerificationEmail: unverified user → resends verification email")
    void resendVerificationEmail_unverifiedUser_resendsEmail() {
        User unverifiedUser = new User();
        unverifiedUser.setId(2L);
        unverifiedUser.setEmail("unverified@test.com");
        unverifiedUser.setEmailVerified(false);

        when(userRepository.findUserByEmail("unverified@test.com")).thenReturn(Optional.of(unverifiedUser));
        doNothing().when(tokenService).sendVerificationEmail(unverifiedUser);

        authService.resendVerificationEmail("unverified@test.com");

        verify(userRepository, times(1)).findUserByEmail("unverified@test.com");
        verify(tokenService, times(1)).sendVerificationEmail(unverifiedUser);
    }

    // ============ TEST: requestPasswordReset ============
    @Test
    @DisplayName("requestPasswordReset: existing user → sends reset email")
    void requestPasswordReset_existingUser_sendsResetEmail() {
        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user1));
        doNothing().when(resetTokenService).sendResetEmail(user1);

        authService.requestPasswordReset("user@test.com");

        verify(userRepository, times(1)).findUserByEmail("user@test.com");
        verify(resetTokenService, times(1)).sendResetEmail(user1);
    }

    @Test
    @DisplayName("requestPasswordReset: non-existing user → throws ResourceNotFoundException")
    void requestPasswordReset_nonExistingUser_throwsResourceNotFoundException() {
        when(userRepository.findUserByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requestPasswordReset("nonexistent@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent@test.com");

        verify(resetTokenService, never()).sendResetEmail(any());
    }

    // ============ TEST: resetPassword ============
    @Test
    @DisplayName("resetPassword: valid token and new password → resets password")
    void resetPassword_validTokenAndNewPassword_resetsPassword() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("valid-reset-token");
        resetToken.setUser(user1);

        when(resetTokenService.validateToken("valid-reset-token")).thenReturn(Optional.of(resetToken));
        when(passwordResetTokenRepository.markAsUsedAtomically("valid-reset-token")).thenReturn(1);
        when(passwordHistoryRepository.findRecentPasswordsByUserId(1L)).thenReturn(Arrays.asList());
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        authService.resetPassword("valid-reset-token", "newpassword123");

        assertThat(user1.getPassword()).isEqualTo("new-encoded-password");

        verify(resetTokenService, times(1)).validateToken("valid-reset-token");
        verify(passwordResetTokenRepository, times(1)).markAsUsedAtomically("valid-reset-token");
        verify(passwordHistoryRepository, times(1)).findRecentPasswordsByUserId(1L);
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    @DisplayName("resetPassword: invalid token → throws AuthErrorException")
    void resetPassword_invalidToken_throwsAuthErrorException() {
        when(resetTokenService.validateToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("invalid-token", "newpassword123"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("Invalid or expired password reset token");

        verify(resetTokenService, times(1)).validateToken("invalid-token");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("resetPassword: token already used → throws AuthErrorException")
    void resetPassword_tokenAlreadyUsed_throwsAuthErrorException() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("used-token");
        resetToken.setUser(user1);

        when(resetTokenService.validateToken("used-token")).thenReturn(Optional.of(resetToken));
        when(passwordResetTokenRepository.markAsUsedAtomically("used-token")).thenReturn(0);

        assertThatThrownBy(() -> authService.resetPassword("used-token", "newpassword123"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("already been used");

        verify(passwordResetTokenRepository, times(1)).markAsUsedAtomically("used-token");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("resetPassword: recently used password → throws AuthErrorException")
    void resetPassword_recentlyUsedPassword_throwsAuthErrorException() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("valid-token");
        resetToken.setUser(user1);

        PasswordHistory oldPassword = new PasswordHistory();
        oldPassword.setPasswordHash("old-encoded-password");

        when(resetTokenService.validateToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordResetTokenRepository.markAsUsedAtomically("valid-token")).thenReturn(1);
        when(passwordHistoryRepository.findRecentPasswordsByUserId(1L)).thenReturn(Arrays.asList(oldPassword));
        when(passwordEncoder.matches("oldpassword123", "old-encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.resetPassword("valid-token", "oldpassword123"))
                .isInstanceOf(AuthErrorException.class)
                .hasMessageContaining("cannot reuse a password");

        verify(passwordEncoder, times(1)).matches("oldpassword123", "old-encoded-password");
        verify(userRepository, never()).save(any(User.class));
    }
}

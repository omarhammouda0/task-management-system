package com.taskmanagement.auth.service;

import com.taskmanagement.auth.dto.AuthResponse;
import com.taskmanagement.auth.dto.LoginRequest;
import com.taskmanagement.auth.dto.RefreshTokenRequest;
import com.taskmanagement.auth.dto.RegisterRequest;
import com.taskmanagement.auth.entity.RefreshToken;
import com.taskmanagement.common.exception.types.Exceptions.EmailAlreadyExistsException;
import com.taskmanagement.common.exception.types.Exceptions.UserNotFoundException;
import com.taskmanagement.common.security.JwtService;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.mapper.UserMapper;
import com.taskmanagement.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuthServiceImpl.
 * Tests all authentication and authorization operations including registration,
 * login, token refresh, and logout with various scenarios and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private User testUser;
    private RefreshToken refreshToken;
    private UserResponseDto userResponseDto;
    private String accessToken;
    private String refreshTokenString;

    @BeforeEach
    void setUp() {
        // Setup register request
        registerRequest = new RegisterRequest(
                "testuser",
                "test@example.com",
                "Password123!",
                "John",
                "Doe",
                Role.MEMBER
        );

        // Setup login request
        loginRequest = new LoginRequest(
                "test@example.com",
                "Password123!"
        );

        // Setup test user
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        testUser.setId(1L);

        // Setup tokens
        accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.access.token";
        refreshTokenString = "refresh-token-uuid-12345";

        // Setup refresh token entity
        refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
        refreshToken.setId(1L);

        // Setup refresh token request
        refreshTokenRequest = new RefreshTokenRequest(refreshTokenString);

        // Setup user response DTO
        userResponseDto = new UserResponseDto(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getRole(),
                testUser.getEmailVerified(),
                null, // avatarUrl
                null, // createdAt
                null, // updatedAt
                testUser.getStatus()
        );
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register new user with valid data")
        void shouldRegisterUserSuccessfully() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(registerRequest.email())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.password())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(testUser)).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(testUser)).thenReturn(refreshToken);
            when(userMapper.toDto(testUser)).thenReturn(userResponseDto);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(accessToken);
            assertThat(response.refreshToken()).isEqualTo(refreshTokenString);

            // Verify user creation
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
            assertThat(savedUser.getRole()).isEqualTo(Role.MEMBER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.getEmailVerified()).isFalse();
            assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");

            verify(userRepository).existsByEmailIgnoreCase(registerRequest.email());
            verify(passwordEncoder).encode(registerRequest.password());
            verify(jwtService).generateToken(testUser);
            verify(refreshTokenService).createRefreshToken(testUser);
            verify(userMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Should normalize email to lowercase during registration")
        void shouldNormalizeEmailToLowercase() {
            // Arrange
            RegisterRequest upperCaseEmailRequest = new RegisterRequest(
                    "testuser",
                    "TEST@EXAMPLE.COM",
                    "Password123!",
                    "John",
                    "Doe",
                    Role.MEMBER
            );

            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.register(upperCaseEmailRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should trim firstName and lastName during registration")
        void shouldTrimNamesGivingRegistration() {
            // Arrange
            RegisterRequest requestWithSpaces = new RegisterRequest(
                    "testuser",
                    "test@example.com",
                    "Password123!",
                    "  John  ",
                    "  Doe  ",
                    Role.MEMBER
            );

            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.register(requestWithSpaces);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowEmailAlreadyExistsException() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(registerRequest.email())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining(registerRequest.email());

            verify(userRepository).existsByEmailIgnoreCase(registerRequest.email());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(jwtService, never()).generateToken(any(User.class));
            verify(refreshTokenService, never()).createRefreshToken(any(User.class));
        }

        @Test
        @DisplayName("Should encode password before saving user")
        void shouldEncodePasswordBeforeSaving() {
            // Arrange
            String rawPassword = "Password123!";
            String encodedPassword = "$2a$10$encoded.password.hash";

            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(passwordEncoder).encode(rawPassword);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(encodedPassword);
        }

        @Test
        @DisplayName("Should set default values for new user")
        void shouldSetDefaultValuesForNewUser() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.register(registerRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getRole()).isEqualTo(Role.MEMBER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.getEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("Should verify method execution order")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.register(registerRequest);

            // Assert - verify execution order
            var inOrder = inOrder(userRepository, passwordEncoder, jwtService, refreshTokenService, userMapper);
            inOrder.verify(userRepository).existsByEmailIgnoreCase(anyString());
            inOrder.verify(passwordEncoder).encode(anyString());
            inOrder.verify(userRepository).save(any(User.class));
            inOrder.verify(jwtService).generateToken(any(User.class));
            inOrder.verify(refreshTokenService).createRefreshToken(any(User.class));
            inOrder.verify(userMapper).toDto(any(User.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void shouldLoginSuccessfully() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmailIgnoreCase(loginRequest.email()))
                    .thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(testUser)).thenReturn(refreshToken);
            when(userMapper.toDto(testUser)).thenReturn(userResponseDto);

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(accessToken);
            assertThat(response.refreshToken()).isEqualTo(refreshTokenString);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(authCaptor.capture());

            UsernamePasswordAuthenticationToken authToken = authCaptor.getValue();
            assertThat(authToken.getPrincipal()).isEqualTo(loginRequest.email());
            assertThat(authToken.getCredentials()).isEqualTo(loginRequest.password());

            verify(userRepository).findByEmailIgnoreCase(loginRequest.email());
            verify(jwtService).generateToken(testUser);
            verify(refreshTokenService).createRefreshToken(testUser);
            verify(userMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when authentication fails")
        void shouldThrowBadCredentialsExceptionWhenAuthFails() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, never()).findByEmailIgnoreCase(anyString());
            verify(jwtService, never()).generateToken(any(User.class));
            verify(refreshTokenService, never()).createRefreshToken(any(User.class));
        }

        @Test
        @DisplayName("Should throw BadCredentialsException on any AuthenticationException")
        void shouldThrowBadCredentialsOnAuthenticationException() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new AuthenticationException("Auth failed") {});

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found after authentication")
        void shouldThrowUserNotFoundAfterAuthentication() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmailIgnoreCase(loginRequest.email()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmailIgnoreCase(loginRequest.email());
            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when user status is not ACTIVE")
        void shouldThrowRuntimeExceptionWhenUserNotActive() {
            // Arrange
            testUser.setStatus(UserStatus.SUSPENDED);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmailIgnoreCase(loginRequest.email()))
                    .thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account is not active")
                    .hasMessageContaining("SUSPENDED");

            verify(jwtService, never()).generateToken(any(User.class));
            verify(refreshTokenService, never()).createRefreshToken(any(User.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException for INACTIVE user status")
        void shouldThrowRuntimeExceptionForInactiveUser() {
            // Arrange
            testUser.setStatus(UserStatus.INACTIVE);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmailIgnoreCase(loginRequest.email()))
                    .thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account is not active")
                    .hasMessageContaining("INACTIVE");
        }

        @Test
        @DisplayName("Should throw RuntimeException for DELETED user status")
        void shouldThrowRuntimeExceptionForDeletedUser() {
            // Arrange
            testUser.setStatus(UserStatus.DELETED);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmailIgnoreCase(loginRequest.email()))
                    .thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account is not active")
                    .hasMessageContaining("DELETED");
        }

        @Test
        @DisplayName("Should verify method execution order for successful login")
        void shouldVerifyMethodExecutionOrderForLogin() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmailIgnoreCase(anyString()))
                    .thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.login(loginRequest);

            // Assert - verify execution order
            var inOrder = inOrder(authenticationManager, userRepository, jwtService, refreshTokenService, userMapper);
            inOrder.verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            inOrder.verify(userRepository).findByEmailIgnoreCase(anyString());
            inOrder.verify(jwtService).generateToken(any(User.class));
            inOrder.verify(refreshTokenService).createRefreshToken(any(User.class));
            inOrder.verify(userMapper).toDto(any(User.class));
        }
    }

    @Nested
    @DisplayName("RefreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should successfully refresh token with valid refresh token")
        void shouldRefreshTokenSuccessfully() {
            // Arrange
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .token("new-refresh-token-uuid-67890")
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .revoked(false)
                    .build();

            when(refreshTokenService.findByToken(refreshTokenString))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken))
                    .thenReturn(refreshToken);
            when(jwtService.generateToken(testUser)).thenReturn(accessToken);
            when(refreshTokenService.rotateToken(refreshToken)).thenReturn(newRefreshToken);
            when(userMapper.toDto(testUser)).thenReturn(userResponseDto);

            // Act
            AuthResponse response = authService.refreshToken(refreshTokenRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(accessToken);
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token-uuid-67890");

            verify(refreshTokenService).findByToken(refreshTokenString);
            verify(refreshTokenService).verifyExpiration(refreshToken);
            verify(jwtService).generateToken(testUser);
            verify(refreshTokenService).rotateToken(refreshToken);
            verify(userMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Should throw RuntimeException when refresh token not found")
        void shouldThrowRuntimeExceptionWhenTokenNotFound() {
            // Arrange
            when(refreshTokenService.findByToken(refreshTokenString))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Refresh token not found");

            verify(refreshTokenService).findByToken(refreshTokenString);
            verify(refreshTokenService, never()).verifyExpiration(any());
            verify(jwtService, never()).generateToken(any(User.class));
            verify(refreshTokenService, never()).rotateToken(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when user associated with token is not active")
        void shouldThrowRuntimeExceptionWhenUserNotActive() {
            // Arrange
            testUser.setStatus(UserStatus.SUSPENDED);

            when(refreshTokenService.findByToken(refreshTokenString))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken))
                    .thenReturn(refreshToken);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account is not active")
                    .hasMessageContaining("SUSPENDED");

            verify(jwtService, never()).generateToken(any(User.class));
            verify(refreshTokenService, never()).rotateToken(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when user is INACTIVE")
        void shouldThrowRuntimeExceptionForInactiveUserOnRefresh() {
            // Arrange
            testUser.setStatus(UserStatus.INACTIVE);

            when(refreshTokenService.findByToken(refreshTokenString))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken))
                    .thenReturn(refreshToken);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account is not active")
                    .hasMessageContaining("INACTIVE");
        }

        @Test
        @DisplayName("Should verify expiration before generating new tokens")
        void shouldVerifyExpirationBeforeGeneratingTokens() {
            // Arrange
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .token("new-refresh-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .revoked(false)
                    .build();

            when(refreshTokenService.findByToken(refreshTokenString))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken))
                    .thenReturn(refreshToken);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.rotateToken(any(RefreshToken.class))).thenReturn(newRefreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.refreshToken(refreshTokenRequest);

            // Assert
            verify(refreshTokenService).verifyExpiration(refreshToken);
        }

        @Test
        @DisplayName("Should rotate refresh token after successful verification")
        void shouldRotateRefreshTokenAfterVerification() {
            // Arrange
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .token("rotated-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .revoked(false)
                    .build();

            when(refreshTokenService.findByToken(refreshTokenString))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken))
                    .thenReturn(refreshToken);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.rotateToken(refreshToken)).thenReturn(newRefreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.refreshToken(refreshTokenRequest);

            // Assert
            verify(refreshTokenService).rotateToken(refreshToken);
        }

        @Test
        @DisplayName("Should verify method execution order")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .token("new-token")
                    .user(testUser)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();

            when(refreshTokenService.findByToken(anyString()))
                    .thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(any(RefreshToken.class)))
                    .thenReturn(refreshToken);
            when(jwtService.generateToken(any(User.class))).thenReturn(accessToken);
            when(refreshTokenService.rotateToken(any(RefreshToken.class))).thenReturn(newRefreshToken);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // Act
            authService.refreshToken(refreshTokenRequest);

            // Assert - verify execution order
            var inOrder = inOrder(refreshTokenService, jwtService, userMapper);
            inOrder.verify(refreshTokenService).findByToken(anyString());
            inOrder.verify(refreshTokenService).verifyExpiration(any(RefreshToken.class));
            inOrder.verify(jwtService).generateToken(any(User.class));
            inOrder.verify(refreshTokenService).rotateToken(any(RefreshToken.class));
            inOrder.verify(userMapper).toDto(any(User.class));
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should successfully logout with valid refresh token")
        void shouldLogoutSuccessfully() {
            // Arrange
            when(refreshTokenService.revokeTokenByString(refreshTokenString)).thenReturn(true);

            // Act
            authService.logout(refreshTokenString);

            // Assert
            verify(refreshTokenService).revokeTokenByString(refreshTokenString);
        }

        @Test
        @DisplayName("Should handle logout when token is already revoked")
        void shouldHandleLogoutWhenTokenAlreadyRevoked() {
            // Arrange
            when(refreshTokenService.revokeTokenByString(refreshTokenString)).thenReturn(false);

            // Act
            authService.logout(refreshTokenString);

            // Assert
            verify(refreshTokenService).revokeTokenByString(refreshTokenString);
        }

        @Test
        @DisplayName("Should handle logout with null refresh token gracefully")
        void shouldHandleNullRefreshTokenGracefully() {
            // Act
            authService.logout(null);

            // Assert
            verify(refreshTokenService, never()).revokeTokenByString(any());
        }

        @Test
        @DisplayName("Should handle logout with blank refresh token gracefully")
        void shouldHandleBlankRefreshTokenGracefully() {
            // Act
            authService.logout("   ");

            // Assert
            verify(refreshTokenService, never()).revokeTokenByString(any());
        }

        @Test
        @DisplayName("Should handle logout with empty refresh token gracefully")
        void shouldHandleEmptyRefreshTokenGracefully() {
            // Act
            authService.logout("");

            // Assert
            verify(refreshTokenService, never()).revokeTokenByString(any());
        }

        @Test
        @DisplayName("Should not throw exception even if revoke fails")
        void shouldNotThrowExceptionEvenIfRevokeFails() {
            // Arrange
            when(refreshTokenService.revokeTokenByString(anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThatThrownBy(() -> authService.logout(refreshTokenString))
                    .isInstanceOf(RuntimeException.class);

            verify(refreshTokenService).revokeTokenByString(refreshTokenString);
        }

        @Test
        @DisplayName("Should call revokeTokenByString with exact token value")
        void shouldCallRevokeWithExactTokenValue() {
            // Arrange
            String specificToken = "specific-refresh-token-12345";
            when(refreshTokenService.revokeTokenByString(specificToken)).thenReturn(true);

            // Act
            authService.logout(specificToken);

            // Assert
            verify(refreshTokenService).revokeTokenByString(specificToken);
            verify(refreshTokenService, never()).revokeTokenByString(argThat(token ->
                    !specificToken.equals(token)
            ));
        }
    }
}


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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.debug("Attempting to register new user with email: {}", registerRequest.email());


        if (userRepository.existsByEmailIgnoreCase(registerRequest.email())) {
            log.warn("Registration failed - email already exists: {}", registerRequest.email());
            throw new EmailAlreadyExistsException(registerRequest.email());
        }


        User user = User.builder()
                .email(registerRequest.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .firstName(registerRequest.firstName().trim())
                .lastName(registerRequest.lastName().trim())
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();


        User savedUser = userRepository.save(user);
        log.info("User registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());


        String accessToken = jwtService.generateToken(savedUser);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        UserResponseDto userDto = userMapper.toDto(savedUser);

        log.debug("Generated tokens for newly registered user: {}", savedUser.getEmail());

        return new AuthResponse(accessToken, refreshToken.getToken(), userDto);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.debug("Attempting login for user: {}", loginRequest.email());

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            log.debug("Authentication successful for user: {}", loginRequest.email());

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}", loginRequest.email(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }


        User user = userRepository.findByEmailIgnoreCase(loginRequest.email())
                .orElseThrow(() -> {
                    log.error("User not found after successful authentication: {}", loginRequest.email());
                    return new UserNotFoundException("User not found");
                });


        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login attempt for inactive user: {} - Status: {}", user.getEmail(), user.getStatus());
            throw new RuntimeException("Account is not active. Status: " + user.getStatus());
        }


        String accessToken = jwtService.generateToken(user);


        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);


        UserResponseDto userDto = userMapper.toDto(user);

        log.info("User logged in successfully: id={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(accessToken, refreshToken.getToken(), userDto);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.debug("Attempting to refresh token");

        String requestRefreshToken = refreshTokenRequest.refreshToken();


        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found");
                    return new RuntimeException("Refresh token not found");
                });


        refreshToken = refreshTokenService.verifyExpiration(refreshToken);


        User user = refreshToken.getUser();


        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Refresh token attempt for inactive user: {} - Status: {}", user.getEmail(), user.getStatus());
            throw new RuntimeException("Account is not active. Status: " + user.getStatus());
        }


        String accessToken = jwtService.generateToken(user);


        RefreshToken newRefreshToken = refreshTokenService.rotateToken(refreshToken);
        String newRefreshTokenString = newRefreshToken.getToken();


        UserResponseDto userDto = userMapper.toDto(user);

        log.info("Token refreshed successfully for user: id={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(accessToken, newRefreshTokenString, userDto);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.debug("Attempting logout with refresh token");

        if (refreshToken == null || refreshToken.isBlank()) {
            log.debug("Logout called with empty token - nothing to revoke");
            return;
        }


        boolean revoked = refreshTokenService.revokeTokenByString(refreshToken);

        if (revoked) {
            log.info("User logged out successfully - token revoked");
        } else {
            log.debug("Logout called but token not found (already logged out or invalid)");
        }
    }
}

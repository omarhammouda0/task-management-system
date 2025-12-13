package com.taskmanagement.auth.controller;

import com.taskmanagement.auth.dto.AuthResponse;
import com.taskmanagement.auth.dto.LoginRequest;
import com.taskmanagement.auth.dto.RefreshTokenRequest;
import com.taskmanagement.auth.dto.RegisterRequest;
import com.taskmanagement.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints for user registration, login, token refresh, and logout")
public class AuthController {

    private final AuthService authService;



    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account with the provided details.
                    
                    **📋 Required Fields:**
                    - `userName` (String, 2-50 chars) - Username ✅ REQUIRED
                    - `email` (String) - Valid email address ✅ REQUIRED
                    - `password` (String, min 10 chars) - Strong password ✅ REQUIRED
                    - `firstName` (String) - First name ✅ REQUIRED
                    - `lastName` (String) - Last name ✅ REQUIRED
                    
                    **Business Logic:**
                    - User is created with ACTIVE status by default
                    - Default role is MEMBER
                    - Password is hashed using BCrypt
                    - Email verification is not required (can be added later)
                    - Returns JWT tokens immediately upon successful registration
                    - Access token expires in 15 minutes (900000ms)
                    - Refresh token expires in 7 days (604800000ms)
                    
                    **⚠️ IMPORTANT - Role Assignment:**
                    - User role is ALWAYS set to MEMBER (hardcoded for security)
                    - Even if you send "role": "ADMIN" in the request, it will be IGNORED
                    - Only system admins can create users with ADMIN role (use /api/users endpoint)
                    - This prevents unauthorized privilege escalation
                    
                    **Validations:**
                    - Email: Required, valid email format, must be unique
                    - Password: Required, minimum 8 characters, must contain uppercase, lowercase, and number
                    - First Name: Required, max 50 characters
                    - Last Name: Required, max 50 characters
                    
                    **Security:**
                    - Password is hashed with BCrypt (cost factor 10)
                    - JWT tokens are signed with HS256 algorithm
                    - Refresh token is stored securely in database
                    - Role field in request body is ignored (security measure)
                    
                    **No Authentication Required**: This is a public endpoint
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Registration",
                                    value = """
                                            {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 900000
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation error (weak password, invalid email format, missing fields)"),
            @ApiResponse(responseCode = "409", description = "Conflict - Email already exists in the system")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Registration Request",
                                    value = """
                                            {
                                                "email": "john.doe@example.com",
                                                "password": "SecurePass123!",
                                                "firstName": "John",
                                                "lastName": "Doe" ,
                                                "userName": "john_doe_09"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.email());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Login user",
            description = """
                    Authenticates user with email and password.
                    
                    **📋 Required Fields:**
                    - `email` (String) - Registered email address ✅ REQUIRED
                    - `password` (String) - Account password ✅ REQUIRED
                    
                    **Business Logic:**
                    - Validates credentials against stored BCrypt hash
                    - User must have ACTIVE status to login
                    - INACTIVE, SUSPENDED, or DELETED users cannot login
                    - Generates new JWT access and refresh tokens
                    - Previous refresh tokens remain valid until they expire
                    - Access token expires in 15 minutes
                    - Refresh token expires in 7 days
                    
                    **Validations:**
                    - Email: Required, must exist in system
                    - Password: Required, must match stored hash
                    - User Status: Must be ACTIVE
                    
                    **Security:**
                    - Failed login attempts are logged
                    - Passwords are never returned in response
                    - Tokens include user role for authorization
                    - Case-insensitive email matching
                    
                    **No Authentication Required**: This is a public endpoint
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = """
                                            {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 900000
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input - missing email or password"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials or user account is not active")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Request",
                                    value = """
                                            {
                                                "email": "john.doe@example.com",
                                                "password": "SecurePass123!"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh access token",
            description = """
                    Generates a new access token using a valid refresh token.
                    
                    **📋 Required Fields:**
                    - `refreshToken` (String) - Valid refresh token from login ✅ REQUIRED
                    
                    **Business Logic:**
                    - Use this when access token expires (after 15 minutes)
                    - Validates refresh token against database
                    - Refresh token must not be expired or revoked
                    - Generates new access token with same permissions
                    - Does NOT generate a new refresh token
                    - User must still be ACTIVE to refresh
                    
                    **Token Lifecycle:**
                    - Access Token: 15 minutes (900000ms)
                    - Refresh Token: 7 days (604800000ms)
                    - Old access tokens become invalid immediately
                    - Refresh token remains valid for its full lifetime
                    
                    **Security:**
                    - Refresh token is validated against database
                    - Expired refresh tokens are automatically cleaned up
                    - User status is checked before issuing new access token
                    - Invalid or tampered tokens are rejected
                    
                    **No Authentication Required**: Refresh token serves as authentication
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Refresh token expired, revoked, or user inactive")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Refresh Token Request",
                                    value = """
                                            {
                                                "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Logout user",
            description = """
                    Invalidates the refresh token to logout the user.
                    
                    **📋 Required Fields:**
                    - `refreshToken` (String) - Valid refresh token to invalidate ✅ REQUIRED
                    
                    **Business Logic:**
                    - Removes refresh token from database (revokes it)
                    - User must obtain new tokens via login to continue
                    - Access token remains valid until expiration (15 minutes)
                    - For complete security, client should discard access token
                    - Multiple devices can have different refresh tokens
                    
                    **Token Behavior:**
                    - Refresh token: Immediately invalidated
                    - Access token: Valid until natural expiration
                    - Client responsibility: Delete stored tokens
                    
                    **Security Note:**
                    If access token is compromised, it remains valid until expiration.
                    For critical operations, consider implementing token blacklist.
                    
                    **No Authentication Required**: Refresh token serves as authentication
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful - refresh token invalidated"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token format"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found or already revoked")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token to invalidate",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Logout Request",
                                    value = """
                                            {
                                                "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request received");
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Health check",
            description = "Check if the authentication service is running"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}


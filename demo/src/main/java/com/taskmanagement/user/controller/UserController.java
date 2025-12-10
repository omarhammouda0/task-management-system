package com.taskmanagement.user.controller;

import com.taskmanagement.user.dto.UserCreateDto;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.dto.UserUpdateDto;
import com.taskmanagement.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints - manage user accounts, profiles, and status")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Create a new user (Admin)",
            description = """
                    Creates a new user account. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Only system administrators can create users
                    - User is created with specified role (ADMIN or MEMBER)
                    - Default status is ACTIVE
                    - Password is hashed with BCrypt
                    - Email must be unique in the system
                    - Created user can login immediately
                    
                    **✅ Role Assignment (Admin Controlled):**
                    - Admin CAN specify the role in the request body
                    - If "role" is provided: Uses that role (ADMIN or MEMBER)
                    - If "role" is omitted/null: Defaults to MEMBER
                    - This is DIFFERENT from /api/auth/register which always creates MEMBER users
                    
                    **Validations:**
                    - Email: Required, valid format, must be unique
                    - Password: Required, min 8 characters, must contain uppercase, lowercase, number
                    - First Name: Required, max 50 characters
                    - Last Name: Required, max 50 characters
                    - Role: Optional, defaults to MEMBER (options: ADMIN, MEMBER)
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    Typically used by admins to create accounts for new team members
                    
                    **Example Requests:**
                    ```json
                    // Create a MEMBER user (explicit)
                    {"email": "user@example.com", "password": "Pass123!", "firstName": "John", "lastName": "Doe", "role": "MEMBER"}
                    
                    // Create an ADMIN user
                    {"email": "admin@example.com", "password": "Pass123!", "firstName": "Jane", "lastName": "Smith", "role": "ADMIN"}
                    
                    // Create a user without specifying role (defaults to MEMBER)
                    {"email": "user2@example.com", "password": "Pass123!", "firstName": "Bob", "lastName": "Wilson"}
                    ```
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed or weak password"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "409", description = "Conflict - Email already exists in system")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create User",
                                    value = """
                                            {
                                                "email": "newuser@example.com",
                                                "password": "SecurePass123!",
                                                "firstName": "Jane",
                                                "lastName": "Smith",
                                                "role": "MEMBER"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @Operation(
            summary = "Get all users",
            description = """
                    Retrieves all active users with pagination support.
                    
                    **Business Logic:**
                    - Returns only ACTIVE users
                    - Excludes INACTIVE, SUSPENDED, and DELETED users
                    - Supports pagination and sorting
                    - Any authenticated user can view active users
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: id, ascending
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc
                    
                    **Authorization:**
                    - Any authenticated user
                    
                    **Use Case:**
                    Used to see active team members, assign tasks, add to teams
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @Operation(
            summary = "Get all users including inactive (Admin)",
            description = """
                    Retrieves all users including inactive, suspended, and deleted ones. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Returns ALL users regardless of status
                    - Includes ACTIVE, INACTIVE, SUSPENDED, and DELETED users
                    - Supports pagination and sorting
                    - Only system admins can view all users
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: id, ascending
                    - Sort example: ?sort=status,asc&sort=createdAt,desc
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    User management, auditing, restoring deleted accounts
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsersForAdmin(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsersForAdmin(pageable));
    }

    @Operation(
            summary = "Get user by ID",
            description = """
                    Retrieves user information by their ID.
                    
                    **Business Logic:**
                    - Returns user details if ACTIVE
                    - Cannot retrieve INACTIVE, SUSPENDED, or DELETED users
                    - Any authenticated user can view active users
                    - Password hash is never returned
                    
                    **Response Includes:**
                    - Basic profile information (name, email)
                    - User role and status
                    - Creation and update timestamps
                    - Avatar URL (if set)
                    
                    **Authorization:**
                    - Any authenticated user
                    
                    **Use Case:**
                    View team member profiles, check assignee details
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found or not active")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.findUserById(userId));
    }

    @Operation(
            summary = "Get user by ID with full details (Admin)",
            description = "Retrieves complete user information including sensitive data. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserByIdForAdmin(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.findUserByIdForAdmin(userId));
    }

    @Operation(
            summary = "Update user",
            description = """
                    Updates user information. Users can update their own profile, admins can update any user.
                    
                    **Business Logic:**
                    - Users can update their own profile (firstName, lastName, avatarUrl)
                    - System admins can update any user
                    - Email cannot be changed (immutable)
                    - Password cannot be changed via this endpoint (use dedicated endpoint)
                    - Role can only be changed by admins
                    - Partial updates supported (only send fields to change)
                    
                    **Updatable Fields:**
                    - First Name: max 50 characters
                    - Last Name: max 50 characters
                    - Avatar URL: valid URL format
                    
                    **Authorization:**
                    - User can update their own profile
                    - System admins can update any user
                    
                    **Use Case:**
                    Profile management, avatar updates, name corrections
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - can only update own profile unless admin"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update User",
                                    value = """
                                            {
                                                "firstName": "Updated Name",
                                                "lastName": "Updated LastName",
                                                "avatarUrl": "https://example.com/avatar.jpg"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    @Operation(
            summary = "Activate user (Admin)",
            description = """
                    Activates an inactive or suspended user account. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Changes user status to ACTIVE
                    - Can activate INACTIVE or SUSPENDED users
                    - Cannot activate DELETED users (use restore endpoint)
                    - User can login immediately after activation
                    - All previous team memberships remain intact
                    
                    **Status Changes:**
                    - INACTIVE → ACTIVE ✓
                    - SUSPENDED → ACTIVE ✓
                    - DELETED → ACTIVE ✗ (use restore)
                    - ACTIVE → ACTIVE (no change)
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    Re-enable accounts after resolution of issues, end of suspension period
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/activate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> activateUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.activateUser(userId));
    }

    @Operation(
            summary = "Deactivate user (Admin)",
            description = """
                    Deactivates a user account. User will not be able to login. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Changes user status to INACTIVE
                    - User cannot login while inactive
                    - Existing refresh tokens remain valid (logout separately if needed)
                    - Team memberships are preserved
                    - Assigned tasks remain assigned
                    - Can be reactivated later
                    
                    **Impact:**
                    - Login: Blocked ✗
                    - API Access: Blocked ✗
                    - Team Membership: Preserved ✓
                    - Task Assignments: Preserved ✓
                    - Data: Preserved ✓
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    Temporary account disable, user leave, pending investigation
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> deactivateUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.deactivateUser(userId));
    }

    @Operation(
            summary = "Suspend user (Admin)",
            description = """
                    Suspends a user account for policy violations. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Changes user status to SUSPENDED
                    - User cannot login while suspended
                    - Typically used for policy violations or security concerns
                    - More severe than deactivation
                    - Team memberships preserved
                    - Can be activated later after issue resolution
                    
                    **Difference from Deactivate:**
                    - SUSPENDED: Policy violation, security concern
                    - INACTIVE: Normal temporary disable, user request
                    
                    **Impact:**
                    - Login: Blocked ✗
                    - API Access: Blocked ✗
                    - Visibility: Marked as suspended
                    - Data: Fully preserved ✓
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    Policy violations, security breaches, account abuse
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User suspended successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/suspend/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> suspendUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.suspendUser(userId));
    }

    @Operation(
            summary = "Restore deleted user (Admin)",
            description = """
                    Restores a soft-deleted user account. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Changes user status from DELETED to ACTIVE
                    - Only works for soft-deleted users
                    - All user data is restored
                    - Team memberships are restored
                    - Task assignments are restored
                    - User can login immediately
                    
                    **What Gets Restored:**
                    - User profile and credentials
                    - Team memberships
                    - Task assignments
                    - Historical data
                    - Access permissions
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    Accidental deletion, user return, data recovery
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found or not deleted")
    })
    @PutMapping("/restore/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> restoreUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.restoreUser(userId));
    }

    @Operation(
            summary = "Delete user (Admin)",
            description = """
                    Soft deletes a user account. The account can be restored later. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Changes user status to DELETED (soft delete)
                    - User data is NOT physically removed from database
                    - User cannot login while deleted
                    - Team memberships are preserved but hidden
                    - Task assignments remain in database
                    - Can be restored using restore endpoint
                    - Permanent deletion is NOT supported (data retention)
                    
                    **Impact:**
                    - Login: Blocked ✗
                    - API Access: Blocked ✗
                    - Visibility: Hidden from regular users ✗
                    - Team Membership: Preserved but hidden ✓
                    - Data: Fully preserved in database ✓
                    - Restoration: Possible via restore endpoint ✓
                    
                    **Soft Delete vs Hard Delete:**
                    - Soft: Status changed to DELETED, data preserved
                    - Hard: Data physically removed (NOT implemented for compliance)
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    
                    **Use Case:**
                    User termination, account closure, data retention compliance
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found or already deleted")
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}

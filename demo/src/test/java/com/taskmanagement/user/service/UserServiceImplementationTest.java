package com.taskmanagement.user.service;

import com.taskmanagement.common.exception.types.Base.NotFoundException;
import com.taskmanagement.common.exception.types.Base.StatuesException;
import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.user.dto.UserCreateDto;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.dto.UserUpdateDto;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImplementation Unit Tests")
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserServiceImplementation userService;

    private User adminUser;
    private User memberUser;
    private User managerUser;
    private User targetUser;
    private UserResponseDto userResponseDto;
    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {

        // Setup admin user
        adminUser = User.builder()
                .email("admin@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        adminUser.setId(1L);

        // Setup member user
        memberUser = User.builder()
                .email("member@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Member")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        memberUser.setId(2L);

        // Setup manager user
        managerUser = User.builder()
                .email("manager@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Manager")
                .lastName("User")
                .role(Role.MANAGER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        managerUser.setId(3L);

        // Setup target user for testing operations
        targetUser = User.builder()
                .email("target@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Target")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        targetUser.setId(4L);

        // Setup DTOs
        userResponseDto = new UserResponseDto(
                1L,
                "test@example.com",
                "Test",
                "User",
                Role.MEMBER,
                false,
                null,
                Instant.now(),
                Instant.now(),
                UserStatus.ACTIVE
        );

        userCreateDto = new UserCreateDto(
                "test@example.com",
                "Password123!",
                "Test",
                "User",
                Role.MEMBER,
                UserStatus.ACTIVE
        );

        userUpdateDto = new UserUpdateDto(
                "updated@example.com",
                "NewPassword123!",
                "Updated",
                "User",
                Role.MEMBER,
                true,
                "https://example.com/avatar.jpg",
                UserStatus.ACTIVE
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper method to set up authentication in SecurityContext
     */
    private void setupAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    // ============================================
    // CREATE USER TESTS
    // ============================================

    @Nested
    @DisplayName("createUser() Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully as admin")
        void shouldCreateUserAsAdmin() {
            // Given
            setupAuthentication(adminUser);

            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.existsByEmailIgnoreCase(userCreateDto.email()))
                    .thenReturn(false);
            when(userMapper.toEntity(userCreateDto)).thenReturn(targetUser);
            when(passwordEncoder.encode(userCreateDto.password()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(targetUser)).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.createUser(userCreateDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo(userResponseDto.email());

            verify(userRepository).existsByEmailIgnoreCase(userCreateDto.email());
            verify(passwordEncoder).encode(userCreateDto.password());
            verify(userRepository).save(any(User.class));
            verify(userMapper).toDto(targetUser);
        }

        @Test
        @DisplayName("Should create user without authentication (system operation)")
        void shouldCreateUserWithoutAuthentication() {
            // Given
            SecurityContextHolder.clearContext();
            when(userRepository.existsByEmailIgnoreCase(userCreateDto.email()))
                    .thenReturn(false);
            when(userMapper.toEntity(userCreateDto)).thenReturn(targetUser);
            when(passwordEncoder.encode(userCreateDto.password()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(targetUser)).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.createUser(userCreateDto);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should create user with anonymous authentication")
        void shouldCreateUserWithAnonymousAuthentication() {
            // Given
            Authentication auth = new AnonymousAuthenticationToken(
                    "key",
                    "anonymous",
                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            when(userRepository.existsByEmailIgnoreCase(userCreateDto.email()))
                    .thenReturn(false);
            when(userMapper.toEntity(userCreateDto)).thenReturn(targetUser);
            when(passwordEncoder.encode(userCreateDto.password()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(targetUser)).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.createUser(userCreateDto);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-admin tries to create user")
        void shouldThrowExceptionWhenNonAdminCreatesUser() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));

            // When/Then
            assertThatThrownBy(() -> userService.createUser(userCreateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can create users");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.existsByEmailIgnoreCase(userCreateDto.email()))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.createUser(userCreateDto))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should encode password before saving")
        void shouldEncodePasswordBeforeSaving() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.existsByEmailIgnoreCase(userCreateDto.email()))
                    .thenReturn(false);
            when(userMapper.toEntity(userCreateDto)).thenReturn(targetUser);
            when(passwordEncoder.encode(userCreateDto.password()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(targetUser)).thenReturn(userResponseDto);

            // When
            userService.createUser(userCreateDto);

            // Then
            verify(passwordEncoder).encode(userCreateDto.password());
            verify(userRepository).save(argThat(user ->
                    user.getPasswordHash().equals("$2a$10$encodedPassword")
            ));
        }
    }

    // ============================================
    // FIND USER BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("findUserById() Tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("Should find user by id for self")
        void shouldFindUserByIdForSelf() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(memberUser.getId()))
                    .thenReturn(Optional.of(memberUser));
            when(userMapper.toDto(memberUser)).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.findUserById(memberUser.getId());

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(memberUser.getId());
            verify(userMapper).toDto(memberUser);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when accessing other user's details")
        void shouldThrowExceptionWhenAccessingOtherUser() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.findUserById(targetUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You can only access your own user details");
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.findUserById(999L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when accessing deleted user")
        void shouldThrowExceptionWhenAccessingDeletedUser() {
            // Given
            User deletedUser = User.builder()
                    .email("deleted@example.com")
                    .status(UserStatus.DELETED)
                    .role(Role.MEMBER)
                    .build();
            deletedUser.setId(5L);

            setupAuthentication(deletedUser);
            when(userRepository.findByEmailIgnoreCase(deletedUser.getEmail()))
                    .thenReturn(Optional.of(deletedUser));
            when(userRepository.findById(deletedUser.getId()))
                    .thenReturn(Optional.of(deletedUser));

            // When/Then
            assertThatThrownBy(() -> userService.findUserById(deletedUser.getId()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ============================================
    // FIND USER BY ID FOR ADMIN TESTS
    // ============================================

    @Nested
    @DisplayName("findUserByIdForAdmin() Tests")
    class FindUserByIdForAdminTests {

        @Test
        @DisplayName("Should find any user including deleted ones for admin")
        void shouldFindAnyUserForAdmin() {
            // Given
            User deletedUser = User.builder()
                    .email("deleted@example.com")
                    .status(UserStatus.DELETED)
                    .role(Role.MEMBER)
                    .build();
            deletedUser.setId(5L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(deletedUser.getId()))
                    .thenReturn(Optional.of(deletedUser));
            when(userMapper.toDto(deletedUser)).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.findUserByIdForAdmin(deletedUser.getId());

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(deletedUser.getId());
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to use admin method")
        void shouldThrowExceptionWhenNonAdminUsesAdminMethod() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));

            // When/Then
            assertThatThrownBy(() -> userService.findUserByIdForAdmin(targetUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can access other users' details");
        }
    }

    // ============================================
    // UPDATE USER TESTS
    // ============================================

    @Nested
    @DisplayName("updateUser() Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully as admin")
        void shouldUpdateUserAsAdmin() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));
            when(userRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), anyLong()))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.updateUser(targetUser.getId(), userUpdateDto);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update own profile successfully")
        void shouldUpdateOwnProfile() {
            // Given
            UserUpdateDto selfUpdate = new UserUpdateDto(
                    null,
                    "NewPassword123!",
                    "UpdatedFirst",
                    "UpdatedLast",
                    null,
                    null,
                    null,
                    null
            );

            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(memberUser.getId()))
                    .thenReturn(Optional.of(memberUser));
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(memberUser);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.updateUser(memberUser.getId(), selfUpdate);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when non-admin updates other user")
        void shouldThrowExceptionWhenNonAdminUpdatesOtherUser() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(targetUser.getId(), userUpdateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins or the user himself can update the user");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when updating inactive user")
        void shouldThrowExceptionWhenUpdatingInactiveUser() {
            // Given
            targetUser.setStatus(UserStatus.INACTIVE);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(targetUser.getId(), userUpdateDto))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("Cannot update user");
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to change role")
        void shouldThrowExceptionWhenNonAdminChangesRole() {
            // Given
            UserUpdateDto roleUpdate = new UserUpdateDto(
                    null,
                    null,
                    null,
                    null,
                    Role.ADMIN,
                    null,
                    null,
                    null
            );

            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(memberUser.getId()))
                    .thenReturn(Optional.of(memberUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(memberUser.getId(), roleUpdate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can change roles");
        }

        @Test
        @DisplayName("Should throw exception when demoting last admin")
        void shouldThrowExceptionWhenDemotingLastAdmin() {
            // Given
            UserUpdateDto roleUpdate = new UserUpdateDto(
                    null,
                    null,
                    null,
                    null,
                    Role.MEMBER,
                    null,
                    null,
                    null
            );

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(adminUser.getId()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.existsOtherAdmins(adminUser.getId()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(adminUser.getId(), roleUpdate))
                    .isInstanceOf(LastAdminException.class);
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to change email verification")
        void shouldThrowExceptionWhenNonAdminChangesEmailVerification() {
            // Given
            UserUpdateDto verifyUpdate = new UserUpdateDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    true,
                    null,
                    null
            );

            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(memberUser.getId()))
                    .thenReturn(Optional.of(memberUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(memberUser.getId(), verifyUpdate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can change email verification status");
        }
    }

    // ============================================
    // ACTIVATE USER TESTS
    // ============================================

    @Nested
    @DisplayName("activateUser() Tests")
    class ActivateUserTests {

        @Test
        @DisplayName("Should activate inactive user successfully")
        void shouldActivateUserSuccessfully() {
            // Given
            targetUser.setStatus(UserStatus.INACTIVE);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.activateUser(targetUser.getId());

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getStatus() == UserStatus.ACTIVE
            ));
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to activate")
        void shouldThrowExceptionWhenNonAdminActivates() {
            // Given
            targetUser.setStatus(UserStatus.INACTIVE);

            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.activateUser(targetUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can activate users");
        }

        @Test
        @DisplayName("Should throw exception when user is already active")
        void shouldThrowExceptionWhenAlreadyActive() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.activateUser(targetUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("already active");
        }

        @Test
        @DisplayName("Should throw exception when trying to activate deleted user")
        void shouldThrowExceptionWhenActivatingDeletedUser() {
            // Given
            targetUser.setStatus(UserStatus.DELETED);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.activateUser(targetUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("Cannot activate a deleted user");
        }
    }

    // ============================================
    // DEACTIVATE USER TESTS
    // ============================================

    @Nested
    @DisplayName("deactivateUser() Tests")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.deactivateUser(targetUser.getId());

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getStatus() == UserStatus.INACTIVE
            ));
        }

        @Test
        @DisplayName("Should throw exception when trying to deactivate self")
        void shouldThrowExceptionWhenDeactivatingSelf() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(adminUser.getId()))
                    .thenReturn(Optional.of(adminUser));

            // When/Then
            assertThatThrownBy(() -> userService.deactivateUser(adminUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You cannot deactivate your own account");
        }

        @Test
        @DisplayName("Should throw exception when deactivating last admin")
        void shouldThrowExceptionWhenDeactivatingLastAdmin() {
            // Given
            User anotherAdmin = User.builder()
                    .email("another@example.com")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            anotherAdmin.setId(6L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(anotherAdmin.getId()))
                    .thenReturn(Optional.of(anotherAdmin));
            when(userRepository.existsOtherAdmins(anotherAdmin.getId()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.deactivateUser(anotherAdmin.getId()))
                    .isInstanceOf(LastAdminException.class);
        }

        @Test
        @DisplayName("Should throw exception when deactivating non-active user")
        void shouldThrowExceptionWhenDeactivatingNonActiveUser() {
            // Given
            targetUser.setStatus(UserStatus.INACTIVE);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.deactivateUser(targetUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("Cannot deactivate user");
        }
    }

    // ============================================
    // SUSPEND USER TESTS
    // ============================================

    @Nested
    @DisplayName("suspendUser() Tests")
    class SuspendUserTests {

        @Test
        @DisplayName("Should suspend user successfully")
        void shouldSuspendUserSuccessfully() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.suspendUser(targetUser.getId());

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getStatus() == UserStatus.SUSPENDED
            ));
        }

        @Test
        @DisplayName("Should throw exception when suspending self")
        void shouldThrowExceptionWhenSuspendingSelf() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(adminUser.getId()))
                    .thenReturn(Optional.of(adminUser));

            // When/Then
            assertThatThrownBy(() -> userService.suspendUser(adminUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You cannot suspend your own account");
        }

        @Test
        @DisplayName("Should throw exception when user already suspended")
        void shouldThrowExceptionWhenAlreadySuspended() {
            // Given
            targetUser.setStatus(UserStatus.SUSPENDED);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.suspendUser(targetUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("already suspended");
        }

        @Test
        @DisplayName("Should throw exception when suspending deleted user")
        void shouldThrowExceptionWhenSuspendingDeletedUser() {
            // Given
            targetUser.setStatus(UserStatus.DELETED);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.suspendUser(targetUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("Cannot suspend deleted user");
        }

        @Test
        @DisplayName("Should throw exception when suspending last admin")
        void shouldThrowExceptionWhenSuspendingLastAdmin() {
            // Given
            User anotherAdmin = User.builder()
                    .email("another2@example.com")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            anotherAdmin.setId(7L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(anotherAdmin.getId()))
                    .thenReturn(Optional.of(anotherAdmin));
            when(userRepository.existsOtherAdmins(anotherAdmin.getId()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.suspendUser(anotherAdmin.getId()))
                    .isInstanceOf(LastAdminException.class);
        }
    }

    // ============================================
    // SOFT DELETE USER TESTS
    // ============================================

    @Nested
    @DisplayName("softDeleteUser() Tests")
    class SoftDeleteUserTests {

        @Test
        @DisplayName("Should soft delete user successfully")
        void shouldSoftDeleteUserSuccessfully() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenReturn(targetUser);

            // When
            userService.softDeleteUser(targetUser.getId());

            // Then
            verify(userRepository).save(argThat(user ->
                    user.getStatus() == UserStatus.DELETED
            ));
        }

        @Test
        @DisplayName("Should throw exception when deleting self")
        void shouldThrowExceptionWhenDeletingSelf() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(adminUser.getId()))
                    .thenReturn(Optional.of(adminUser));

            // When/Then
            assertThatThrownBy(() -> userService.softDeleteUser(adminUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("You cannot delete your own account");
        }

        @Test
        @DisplayName("Should throw exception when deleting last admin")
        void shouldThrowExceptionWhenDeletingLastAdmin() {
            // Given
            User anotherAdmin = User.builder()
                    .email("another3@example.com")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            anotherAdmin.setId(8L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(anotherAdmin.getId()))
                    .thenReturn(Optional.of(anotherAdmin));
            when(userRepository.existsOtherAdmins(anotherAdmin.getId()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.softDeleteUser(anotherAdmin.getId()))
                    .isInstanceOf(LastAdminException.class);
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to delete")
        void shouldThrowExceptionWhenNonAdminDeletes() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.softDeleteUser(targetUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can delete users");
        }
    }

    // ============================================
    // RESTORE USER TESTS
    // ============================================

    @Nested
    @DisplayName("restoreUser() Tests")
    class RestoreUserTests {

        @Test
        @DisplayName("Should restore deleted user successfully")
        void shouldRestoreUserSuccessfully() {
            // Given
            targetUser.setStatus(UserStatus.DELETED);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenReturn(targetUser);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            UserResponseDto result = userService.restoreUser(targetUser.getId());

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getStatus() == UserStatus.ACTIVE
            ));
        }

        @Test
        @DisplayName("Should throw exception when user is not deleted")
        void shouldThrowExceptionWhenUserNotDeleted() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.restoreUser(targetUser.getId()))
                    .isInstanceOf(StatuesException.class)
                    .hasMessageContaining("is not deleted");
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to restore")
        void shouldThrowExceptionWhenNonAdminRestores() {
            // Given
            targetUser.setStatus(UserStatus.DELETED);

            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userRepository.findById(targetUser.getId()))
                    .thenReturn(Optional.of(targetUser));

            // When/Then
            assertThatThrownBy(() -> userService.restoreUser(targetUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can restore users");
        }
    }

    // ============================================
    // GET ALL USERS TESTS
    // ============================================

    @Nested
    @DisplayName("getAllUsers() Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return page of active users")
        void shouldReturnPageOfActiveUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(memberUser, managerUser, targetUser));

            when(userRepository.getAllUsers(pageable)).thenReturn(userPage);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            Page<UserResponseDto> result = userService.getAllUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            verify(userRepository).getAllUsers(pageable);
            verify(userMapper, times(3)).toDto(any(User.class));
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(List.of());

            when(userRepository.getAllUsers(pageable)).thenReturn(emptyPage);

            // When
            Page<UserResponseDto> result = userService.getAllUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ============================================
    // GET ALL USERS FOR ADMIN TESTS
    // ============================================

    @Nested
    @DisplayName("getAllUsersForAdmin() Tests")
    class GetAllUsersForAdminTests {

        @Test
        @DisplayName("Should return all users including deleted for admin")
        void shouldReturnAllUsersForAdmin() {
            // Given
            User deletedUser = User.builder()
                    .email("deleted@example.com")
                    .status(UserStatus.DELETED)
                    .role(Role.MEMBER)
                    .build();
            deletedUser.setId(9L);

            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(
                    List.of(adminUser, memberUser, managerUser, targetUser, deletedUser)
            );

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

            // When
            Page<UserResponseDto> result = userService.getAllUsersForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(5);
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should throw exception when non-admin tries to get all users")
        void shouldThrowExceptionWhenNonAdminGetsAllUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));

            // When/Then
            assertThatThrownBy(() -> userService.getAllUsersForAdmin(pageable))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins can access all users");
        }
    }

    // ============================================
    // FIND BY EMAIL TESTS
    // ============================================

    @Nested
    @DisplayName("findByEmail() Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by email as admin")
        void shouldFindUserByEmailAsAdmin() {
            // Given
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findByEmailIgnoreCase(targetUser.getEmail()))
                    .thenReturn(Optional.of(targetUser));
            when(userMapper.toDto(targetUser)).thenReturn(userResponseDto);

            // When
            Optional<UserResponseDto> result = userService.findByEmail(targetUser.getEmail());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isNotNull();
            verify(userRepository).findByEmailIgnoreCase(targetUser.getEmail());
        }

        @Test
        @DisplayName("Should find own email")
        void shouldFindOwnEmail() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(userMapper.toDto(memberUser)).thenReturn(userResponseDto);

            // When
            Optional<UserResponseDto> result = userService.findByEmail(memberUser.getEmail());

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should throw exception when non-admin searches other email")
        void shouldThrowExceptionWhenNonAdminSearchesOtherEmail() {
            // Given
            setupAuthentication(memberUser);
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));

            // When/Then
            assertThatThrownBy(() -> userService.findByEmail(targetUser.getEmail()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only admins or the user himself can access user by email");
        }

        @Test
        @DisplayName("Should return empty optional when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findByEmailIgnoreCase(nonExistentEmail))
                    .thenReturn(Optional.empty());

            // When
            Optional<UserResponseDto> result = userService.findByEmail(nonExistentEmail);

            // Then
            assertThat(result).isEmpty();
        }
    }
}

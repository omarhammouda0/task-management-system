package com.taskmanagement.user.service;


import com.taskmanagement.common.exception.ErrorCode.ErrorCode;
import com.taskmanagement.common.exception.types.Base.NotFoundException;
import com.taskmanagement.common.exception.types.Base.StatuesException;
import com.taskmanagement.common.exception.types.Exceptions.AccessDeniedException;
import com.taskmanagement.common.exception.types.Exceptions.EmailAlreadyExistsException;
import com.taskmanagement.common.exception.types.Exceptions.LastAdminException;
import com.taskmanagement.common.exception.types.Exceptions.UserNotFoundException;
import com.taskmanagement.user.dto.UserCreateDto;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.dto.UserUpdateDto;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.mapper.UserMapper;
import com.taskmanagement.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service

@AllArgsConstructor
@Slf4j

public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {

        Objects.requireNonNull(dto, "User DTO must not be null");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {

            User currentUser = getCurrentUser(authentication);
            if (!isCurrentUserAdmin(currentUser)) {
                throw new AccessDeniedException("Only admins can create users");
            }

            log.info("Admin user {} is creating a new user with email: {}", currentUser.getEmail(), dto.email());
        } else {
            log.debug("System operation: Creating user without authentication context");
        }

        emailExistsForCreate(dto.email());
        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        User savedUser = userRepository.save(user);

        log.info("Successfully created user with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findUserById(Long userId) {

        var user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );

        var currentUser = getAuthenticatedUser();

        if (!currentUser.getEmail().equals(user.getEmail())) {
            throw new AccessDeniedException("You can only access your own user details");
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND.name(),
                    userId.toString());
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)

    public UserResponseDto findUserByIdForAdmin(Long userId) {

        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can access other users' details");
        }

        var user = userRepository.findById ( userId ).orElseThrow (
                () -> new UserNotFoundException ( userId )
        );


        return userMapper.toDto ( user );

    }


    @Override
    @Transactional(readOnly = true)

    /**
     * Returns all active users. This is a public endpoint.
     * Only returns basic user information (no sensitive data).
     */

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {


        return userRepository.getAllUsers ( pageable )
                .map ( userMapper::toDto );

    }

    @Override
    @Transactional(readOnly = true)

    public Page<UserResponseDto> getAllUsersForAdmin(Pageable pageable) {

        var currentUser = getAuthenticatedUser();
        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can access all users");
        }

        return userRepository.findAll ( pageable )
                .map ( userMapper::toDto );

    }




    @Override
    @Transactional(readOnly = true)

    public Optional<UserResponseDto> findByEmail(String email) {

        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser) && !currentUser.getEmail().equals(email)) {
            throw new AccessDeniedException("Only admins or the user himself can access user by email");
        }

        return userRepository.findByEmailIgnoreCase ( email )
                .map ( userMapper::toDto );

    }


    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {

        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(dto, "User update DTO must not be null");

        var user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );

        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser) && !enSureTheSameUser(currentUser, userId)) {
            throw new AccessDeniedException("Only admins or the user himself can update the user");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new StatuesException(ErrorCode.USER_NOT_ACTIVE.name(),
                    "Cannot update user with id " + userId + ". User status is " + user.getStatus());
        }

        var u = updateConditions(user, dto, currentUser);

        User toSave = userRepository.save(u);

        log.info("User {} updated by {}", userId, currentUser.getEmail());

        return userMapper.toDto(toSave);
    }


    @Override
    @Transactional
    public UserResponseDto activateUser(Long userId) {

        var user = getTheUser(userId);
        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can activate users");
        }

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new StatuesException(ErrorCode.USER_ALREADY_ACTIVE.name(),
                    "User with id " + userId + " is already active");
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new StatuesException(ErrorCode.INVALID_STATUES_TRANSITION.name(),
                    "Cannot activate a deleted user with id " + userId + ". Please use the restore method");
        }

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        log.info("User {} activated by admin {}", userId, currentUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto deactivateUser(Long userId) {

        var user = getTheUser(userId);
        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can deactivate users");
        }

        // Prevent self-deactivation
        if (currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot deactivate your own account");
        }

        // Prevent deactivating last admin
        if (user.getRole() == Role.ADMIN && !userRepository.existsOtherAdmins(userId)) {
            throw new LastAdminException(userId);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new StatuesException(ErrorCode.USER_NOT_ACTIVE.name(),
                    "Cannot deactivate user with id " + userId + ". Current status: " + user.getStatus());
        }

        user.setStatus(UserStatus.INACTIVE);
        User savedUser = userRepository.save(user);

        log.info("User {} deactivated by admin {}", userId, currentUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto suspendUser(Long userId) {

        var user = getTheUser(userId);
        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can suspend users");
        }

        // Prevent self-suspension
        if (currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot suspend your own account");
        }

        // Prevent suspending last admin
        if (user.getRole() == Role.ADMIN && !userRepository.existsOtherAdmins(userId)) {
            throw new LastAdminException(userId);
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new StatuesException(ErrorCode.USER_IS_SUSPENDED.name(),
                    "User with id " + userId + " is already suspended");
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new StatuesException(ErrorCode.INVALID_STATUES_TRANSITION.name(),
                    "Cannot suspend deleted user with id " + userId);
        }

        user.setStatus(UserStatus.SUSPENDED);
        User savedUser = userRepository.save(user);

        log.info("User {} suspended by admin {}", userId, currentUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto restoreUser(Long userId) {

        var user = getTheUser(userId);
        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can restore users");
        }

        if (user.getStatus() != UserStatus.DELETED) {
            throw new StatuesException(ErrorCode.USER_IS_NOT_DELETED.name(),
                    "User with id " + userId + " is not deleted");
        }

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        log.info("User {} restored by admin {}", userId, currentUser.getEmail());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long userId) {

        var user = getTheUser(userId);
        var currentUser = getAuthenticatedUser();

        if (!isCurrentUserAdmin(currentUser)) {
            throw new AccessDeniedException("Only admins can delete users");
        }

        // Prevent self-deletion
        if (user.getId().equals(currentUser.getId())) {
            throw new StatuesException(ErrorCode.CANNOT_DELETE_OWN_ACCOUNT.name(),
                    "You cannot delete your own account");
        }

        // Prevent deleting last admin
        if (user.getRole() == Role.ADMIN && !userRepository.existsOtherAdmins(userId)) {
            throw new LastAdminException(userId);
        }

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User {} soft deleted by admin {}", userId, currentUser.getEmail());
    }

    private User getTheUser(Long userId) {
        return userRepository.findById ( userId ).orElseThrow (
                () -> new UserNotFoundException ( userId )
        );
    }

    private User updateConditions(User user, UserUpdateDto dto, User currentUser) {

        if (dto.email() != null && !user.getEmail().equals(dto.email())) {
            emailExistsForUpdate(dto.email(), user.getId());
            user.setEmail(dto.email().trim().toLowerCase());
        }

        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName().trim());
        }

        if (dto.lastName() != null) {
            user.setLastName(dto.lastName().trim());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        if (dto.role() != null) {
            if (!isCurrentUserAdmin(currentUser)) {
                throw new AccessDeniedException("Only admins can change roles");
            }

            if (user.getRole() == Role.ADMIN && dto.role() != Role.ADMIN) {
                if (!userRepository.existsOtherAdmins(user.getId())) {
                    throw new LastAdminException(user.getId());
                }
            }

            user.setRole(dto.role());
        }

        if (dto.emailVerified() != null) {
            if (!isCurrentUserAdmin(currentUser)) {
                throw new AccessDeniedException("Only admins can change email verification status");
            }

            user.setEmailVerified(dto.emailVerified());
        }

        if (dto.avatarUrl() != null) {
            user.setAvatarUrl(dto.avatarUrl().trim());
        }

        if (dto.status() != null) {

            if (!isCurrentUserAdmin(currentUser)) {
                throw new AccessDeniedException("Only admins can change user status");
            }



            user.setStatus(dto.status());
        }

        return user;

    }

    private void emailExistsForCreate(String email) {

        if (userRepository.existsByEmailIgnoreCase ( email ))
            throw new EmailAlreadyExistsException ( email );
    }

    private void emailExistsForUpdate(String email , Long id) {
        if (userRepository.existsByEmailIgnoreCaseAndIdNot ( email , id ))
            throw new EmailAlreadyExistsException ( email );
    }

    private User getAuthenticatedUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication required");
        }

        String email = auth.getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

    }

    private User getCurrentUser(Authentication auth) {

        String email = auth.getName ( );
        return userRepository.findByEmailIgnoreCase ( email ).orElseThrow (
                () -> new UserNotFoundException ( email )
        );

    }

    private boolean isCurrentUserAdmin(User user) {

        return user.getRole() == Role.ADMIN;

    }

    private boolean enSureTheSameUser( User currentUser , Long userId ) {

        return currentUser.getId ( ).equals ( userId );

    }

}








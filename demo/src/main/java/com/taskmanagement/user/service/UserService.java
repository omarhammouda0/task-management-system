package com.taskmanagement.user.service;

import com.taskmanagement.user.dto.UserCreateDto;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.dto.UserUpdateDto;
import com.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface UserService {


    UserResponseDto createUser (UserCreateDto dto  );
    UserResponseDto findUserById (Long userId);
    UserResponseDto findUserByIdForAdmin(Long userId);
    Optional<UserResponseDto> findByEmail(String email);
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    Page<UserResponseDto> getAllUsersForAdmin(Pageable pageable);
    UserResponseDto updateUser (Long userId ,  UserUpdateDto dto);
    UserResponseDto activateUser (Long userId);
    UserResponseDto deactivateUser (Long userId);
    UserResponseDto suspendUser (Long userId);
    UserResponseDto restoreUser (Long userId);
    void softDeleteUser (Long userId);


}



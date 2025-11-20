package com.taskmanagement.user.controller;

import com.taskmanagement.user.dto.UserCreateDto;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.dto.UserUpdateDto;
import com.taskmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<UserResponseDto> createUser
            (@Valid @RequestBody UserCreateDto dto ) {

        return ResponseEntity.ok ( userService.createUser ( dto  ) );

    }


    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(

            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {

        return ResponseEntity.ok ( userService.getAllUsers ( pageable ) );
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Page<UserResponseDto>> getAllUsersForAdmin(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {

        return ResponseEntity.ok ( userService.getAllUsersForAdmin ( pageable ) );
    }


    @GetMapping("/{userId}")

    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok ( userService.findUserById (  userId ) );
    }

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<UserResponseDto> getUserByIdForAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok ( userService.findUserByIdForAdmin ( userId ) );
    }

    @PutMapping("/{userId}")
    // Authorization handled in service layer - allows admin OR self-update
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto dto
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    @PutMapping("/activate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity <UserResponseDto> activateUser

            (
                    @PathVariable Long userId

            ) {

        return ResponseEntity.ok ( userService.activateUser ( userId ) );


    }

    @PutMapping("/deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity <UserResponseDto> deactivateUser

            (
                    @PathVariable Long userId
            ) {

        return ResponseEntity.ok ( userService.deactivateUser ( userId ) );


    }

    @PutMapping("/suspend/{userId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<UserResponseDto> suspendUser

            (
                    @PathVariable Long userId
            ) {

        return ResponseEntity.ok ( userService.suspendUser ( userId ) );


    }

    @PutMapping("/restore/{userId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<UserResponseDto> restoreUser

            (
                    @PathVariable Long userId
            ) {

        return ResponseEntity.ok ( userService.restoreUser ( userId ) );


    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> softDeleteUser

            (
                    @PathVariable Long userId
            ) {

        userService.softDeleteUser ( userId );

        return ResponseEntity.noContent ( ).build ( );


    }


}

package com.taskmanagement.user.mapper;

import com.taskmanagement.user.dto.UserCreateDto;
import com.taskmanagement.user.dto.UserResponseDto;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import org.springframework.stereotype.Service;

@Service

public class UserMapper {

    public User toEntity(UserCreateDto dto) {


        return User.builder ( )

                .email ( dto.email ( ).trim ( ).toLowerCase ( ) )
                .firstName ( dto.firstName ( ).trim ( ) )
                .lastName ( dto.lastName ( ).trim ( ) )
                .role ( dto.role ( ) != null ? dto.role ( ) : Role.MEMBER )
                .emailVerified ( false )
                .status ( dto.userStatus () != null ? dto.userStatus () : UserStatus.ACTIVE )
                .build ( );
    }

    public UserResponseDto toDto (User user) {

        return new UserResponseDto (

                user.getId (),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getEmailVerified (),
                user.getAvatarUrl (),
                user.getCreatedAt (),
                user.getUpdatedAt (),
                user.getStatus ()
        );
    }


}


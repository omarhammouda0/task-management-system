package com.taskmanagement.user.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table (name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder


public class User extends BaseEntity implements UserDetails {


    @Column(unique = true , nullable = false , length = 255)
    private String email;

    @Column( nullable = false ,length = 255)
    private String passwordHash;

    @Column (nullable = false ,  length = 100)
    private String firstName;

    @Column (nullable = false ,  length = 100)
    private String lastName;

    @Column (nullable = false )
    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;

    @Column (nullable = false )
    private Boolean emailVerified = false;

    @Column(length = 500)
    private String avatarUrl;

    @Column (nullable = false )
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority ("ROLE_" + role.name()));
    }
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}


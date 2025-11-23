package com.taskmanagement.team.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.team.repository.TeamRepository;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component

public class SecurityHelper {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    protected User getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext ( ).getAuthentication ( );
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException ( "Authentication required" );
        }

        String email = auth.getName ( );

        return userRepository.findByEmailIgnoreCase ( email )
                .orElseThrow ( () -> new UserNotFoundException ( "Current user not found" ) );

    }

    protected void isUserActive(User currentUser) {

        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException (currentUser.getEmail());
        }
    }

    protected User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    protected User userExistsAndActiveCheck(Long userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(user.getEmail());
        }

        return user;
    }

    protected Team teamExistsAndActiveCheck(Long teamId) {

        var team = teamRepository.findById ( teamId )
                .orElseThrow ( () -> new TeamNotFoundException ( teamId ) );

        if (team.getStatus ( ) != TeamStatus.ACTIVE)
            throw new TeamNotFoundException ( teamId.toString ( ) );

        return team;

    }

    protected boolean isMemberInTeam( Long teamId , User userToAdd  ) {

        return teamMemberRepository.existsByTeamIdAndUserId ( teamId , userToAdd.getId ( ) ) ;
    }

    protected boolean isOwner(Long userId , Long teamId) {

        return teamMemberRepository.existsByTeamIdAndUserIdAndRole ( teamId , userId , TeamRole.OWNER );
    }

    protected boolean isSelfOperation (Long currentUserId , Long targetUserId) {

        return currentUserId.equals ( targetUserId );
    }

    protected boolean isLastOwner (Long teamId ) {

        return teamMemberRepository.isLastOwner ( teamId );

    }

    protected void roleTransitionValidation (Long teamId , TeamRole currentRole , TeamRole newRole ) {

        if ( currentRole == newRole )
            throw new InvalidRoleTransitionException ( "New role must be different from current role " );


        if (currentRole.equals ( TeamRole.OWNER ) && isLastOwner ( teamId ) )
            throw new InvalidRoleTransitionException ( "Cannot demote the last owner " );


    }


}



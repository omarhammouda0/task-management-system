package com.taskmanagement.project.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.project.repository.ProjectRepository;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.team.repository.TeamRepository;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Component("projectSecurityHelper")


public class SecurityHelper {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;

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

        if (currentUser.getStatus ( ) != UserStatus.ACTIVE) {
            throw new UserNotActiveException ( currentUser.getEmail ( ) );
        }
    }

    protected boolean isSystemAdmin (User currentUser) {

        return currentUser.getRole ( ) == Role.ADMIN;
    }

    protected void systemAdminCheck (User currentUser) {

        if (currentUser.getRole ( ) != com.taskmanagement.user.enums.Role.ADMIN) {
            throw new AccessDeniedException ( "Only system admin can do this process" );
        }
    }

    protected User getUserById(Long userId) {

        return userRepository.findById ( userId )
                .orElseThrow ( () -> new UserNotFoundException ( userId ) );
    }

    protected User userExistsAndActiveCheck(Long userId) {

        var user = userRepository.findById ( userId )
                .orElseThrow ( () -> new UserNotFoundException ( userId ) );

        if (user.getStatus ( ) != UserStatus.ACTIVE) {
            throw new UserNotActiveException ( user.getEmail ( ) );
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

    protected boolean IsMemberInTheTeam (Long userId, Long teamId) {

        return teamMemberRepository.existsByTeamIdAndUserId ( teamId, userId ) ;

    }

    protected void isMemberInTeamOrSystemAdmin(Long teamId , User userToAdd) {

        if (userToAdd.getRole ( ) != Role.ADMIN &&
                !teamMemberRepository.existsByTeamIdAndUserId ( teamId , userToAdd.getId ( ) ))

            throw new UserNotInTeamException ( userToAdd.getId ( ) , teamId );
    }

    protected void isUserSystemAdminOrTeamOwner(Long userId , Long teamId) {

       if (! userRepository.existsByIdAndRoleAdmin ( userId ) &&
            ! teamMemberRepository.existsByTeamIdAndUserIdAndRole ( teamId , userId , TeamRole.OWNER ) )
           throw new AccessDeniedException ( "Only system admin or team owner can do this process" );

    }

    protected void isOwner(Long userId , Long teamId) {

        if (! teamMemberRepository.existsByTeamIdAndUserIdAndRole ( teamId , userId , TeamRole.OWNER ))
            throw new AccessDeniedException ( "Only the team owner can do this process" );
    }

    protected boolean isSelfOperation(Long currentUserId , Long targetUserId) {

        return currentUserId.equals ( targetUserId );
    }

    protected boolean isLastOwner(Long teamId) {

        return teamMemberRepository.isLastOwner ( teamId );

    }

    protected void roleTransitionValidation(Long teamId , TeamRole currentRole , TeamRole newRole) {

        if (currentRole == newRole)
            throw new InvalidRoleTransitionException ( "New role must be different from current role " );


        if (currentRole.equals ( TeamRole.OWNER ) && isLastOwner ( teamId ))
            throw new InvalidRoleTransitionException ( "Cannot demote the last owner " );


    }

    protected TeamMember getTeamMember(Long teamId , Long userId) {

        return teamMemberRepository.findByTeamIdAndUserId ( teamId , userId )
                .orElseThrow ( () -> new UserNotInTeamException ( userId , teamId ) );

    }

    protected boolean isLastActiveTeamMember(Long teamId) {

        return teamMemberRepository.isLastActiveTeamMember ( teamId );

    }

//    protected boolean isTeamOwnerOrAdmin(Long teamId , Long userId) {
//
//        return teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn ( teamId , userId ,
//                java.util.Arrays.asList ( TeamRole.OWNER , TeamRole.ADMIN ) );
//
//    }

    protected Team teamExists(Long teamId) {

        return teamRepository.findById ( teamId )
                .orElseThrow ( () -> new TeamNotFoundException ( teamId ) );

    }

    protected void validateProjectNameNotExists( String projectName , Long teamId ) {

        if (projectRepository.existsByNameIgnoreCaseAndTeamId( projectName , teamId ) )
            throw new ProjectNameAlreadyExistsException ( projectName , teamId );
    }

    protected void validateProjectNameNotExistsForUpdate( String projectName , Long teamId , Long projectId ) {

        if (projectRepository.existsByTeamIdAndNameIgnoreCaseAndIdNot( teamId , projectName , projectId ) )
            throw new ProjectNameAlreadyExistsException ( projectName , teamId );
    }

    protected ProjectStatus statusValidation(ProjectStatus status) {

        if (status == null)
            return ProjectStatus.PLANNED;

        if   (    status != ProjectStatus.PLANNED &&
                status != ProjectStatus.ACTIVE &&
                status != ProjectStatus.ON_HOLD) {

            throw new InvalidProjectStatusException ( status );
        }

        return status;
    }

    protected void dateValidation(Instant startDate , Instant endDate) {



        if (startDate != null) {
            if (startDate.isBefore ( Instant.now () )) {
                throw new InvalidProjectDateException ( "Start date must be in the future" );
            }
        }

        if (endDate != null) {
            if (endDate.isBefore ( Instant.now ()  )) {
                throw new InvalidProjectDateException ( "End date must be today or in the future" );
            }
        }

        if (startDate != null && endDate != null) {
            if (endDate.isBefore ( startDate )) {
                throw new InvalidProjectDateException ( "End date must be after start date" );
            }
        }
    }

    protected Project projectExistsCheck (Long projectId) {

        return projectRepository.findById ( projectId )
                .orElseThrow ( () -> new ProjectNotFoundException ( projectId ) );

    }

    protected Project projectExistsAndActiveCheck (Long projectId) {

        return projectRepository.findByIdAndStatusActive ( projectId )
                .orElseThrow ( () -> new ProjectNotFoundException ( projectId ) );

    }

    protected void validateStatusValidation(ProjectStatus oldStatus , ProjectStatus newStatus) {

        if (oldStatus == newStatus)
            throw new InvalidProjectStatusException ( oldStatus , newStatus );

        switch (newStatus) {

            case PLANNED:
                if (oldStatus != ProjectStatus.DELETED &&
                        oldStatus != ProjectStatus.ARCHIVED) {
                    throw new InvalidProjectStatusException ( oldStatus , newStatus );
                }
                break;

            case ACTIVE:
                if (oldStatus != ProjectStatus.PLANNED &&
                        oldStatus != ProjectStatus.ON_HOLD) {
                    throw new InvalidProjectStatusException ( oldStatus , newStatus );
                }
                break;

            case ARCHIVED:

                if (oldStatus != ProjectStatus.COMPLETED &&
                        oldStatus != ProjectStatus.ON_HOLD) {
                    throw new InvalidProjectStatusException ( oldStatus , newStatus );
                }
                break;

            case DELETED:

                break;



        }

    }

    protected void teamActiveCheck(Long teamId) {

        if (! teamRepository.existsByIdAndStatusActive ( teamId ) )
            throw new TeamNotFoundException ( teamId );

    }

    protected Project projectExistsCheckAndRetrievableCheckUponRole (User currentUser , Long projectId ) {

        if (! projectRepository.existsById ( projectId ) )
            throw new ProjectNotFoundException ( projectId );

        if (isSystemAdmin ( currentUser ))
            return projectExistsCheck ( projectId );

        else
            return projectExistsAndActiveCheck ( projectId );

    }

    protected Team teamRetrievableCheckUponRole (User currentUser , Long teamId ) {

        if (isSystemAdmin ( currentUser ))
            return teamExists ( teamId );

        else
            return teamExistsAndActiveCheck ( teamId );

    }

    protected void isSystemAdminOrTeamOwner ( User currentUser , Long ownerId) {

        if (! isSystemAdmin (currentUser) && ! isSelfOperation ( currentUser.getId () , ownerId ) )
            throw new AccessDeniedException ( "Only System admin or the team owner can do this process" );
    }

    protected Project projectExistCheck (Long projectId) {

        return projectRepository.findById ( projectId )
                .orElseThrow ( () -> new ProjectNotFoundException ( projectId ) );
    }



}







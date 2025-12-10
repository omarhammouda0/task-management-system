package com.taskmanagement.team.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.team.dto.TeamCreateDto;
import com.taskmanagement.team.dto.TeamResponseDto;
import com.taskmanagement.team.dto.TeamUpdateDto;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.team.mapper.TeamMapper;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.team.repository.TeamRepository;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Objects;


@AllArgsConstructor
@Slf4j

@Service
public class TeamServiceImplementation implements TeamService {


    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final TeamMemberRepository teamMemberRepository;


    @Override
    @Transactional
    public TeamResponseDto createTeam(TeamCreateDto teamCreateDto) {

        Objects.requireNonNull ( teamCreateDto , "Team can not be null" );

        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );
        teamUniqueNameCheck ( teamCreateDto.name ( ) );

        var toSave = teamMapper.toEntity ( teamCreateDto );
        toSave.setOwnerId ( currentUser.getId ( ) );
        toSave.setOwner (  currentUser );

        teamRepository.save ( toSave );

        var teamOwner = teamOwnerBuilder ( currentUser , toSave.getId ( ) );
        teamMemberRepository.save ( teamOwner );

        log.info ( "Team '{}' created by user {}" , toSave.getName ( ) , currentUser.getId ( ) );

        return teamMapper.toDto ( toSave );
    }


    @Override
    @Transactional (readOnly = true)
    public Page<TeamResponseDto> getAllTeamsForAdmin(Pageable pageable) {

        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );

        if (currentUser.getRole ( ) != Role.ADMIN)
            throw new AccessDeniedException ( "Only admins can access all teams" );

        return teamRepository.findAll ( pageable )
                .map ( teamMapper::toDto );
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TeamResponseDto> getMyTeams(Pageable pageable) {

        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );

        return teamRepository.findActiveTeamsByUserId ( currentUser.getId ( ) , pageable )
                .map ( teamMapper::toDto );

    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDto getTeamById(Long teamId) {

        Objects.requireNonNull (  teamId, "Team Id can not be null" );

        var team = teamExistsAndActiveCheck ( teamId );
        var currentUser = getCurrentUser ( );

        isUserActive ( currentUser );
        isMemberInTeam ( currentUser , teamId );

        return teamMapper.toDto ( team );

    }


    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponseDto> getTeamsByOwner(Long ownerId , Pageable pageable) {

        Objects.requireNonNull ( ownerId , "Owner ID can not be null" );

        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );

        if (currentUser.getRole ( ) == Role.ADMIN)
            return teamRepository.findByOwnerId ( ownerId , pageable )
                    .map ( teamMapper::toDto );

        if (!currentUser.getId ( ).equals ( ownerId ))

            throw new AccessDeniedException ( "Only admins can access teams of other users" );

        return teamRepository.findByOwnerIdAndStatusActive ( ownerId , pageable )
                .map ( teamMapper::toDto );

    }

    @Override
    @Transactional (readOnly = true)
    public TeamResponseDto getTeamByName(String teamName) {

        Objects.requireNonNull ( teamName , "Team name can not be null" );
        String trimmedTeamName = teamName.trim();

        if (trimmedTeamName.isEmpty ( ))
            throw new IllegalArgumentException ( "Team name can not be empty" );

        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );

        var team = teamRepository.findByNameIgnoreCase ( trimmedTeamName )
                .orElseThrow ( () -> new TeamNotFoundException ( teamName ) );

        if ( team.getStatus () != TeamStatus.ACTIVE)
            throw new TeamNotFoundException ( teamName );

        isMemberInTeam ( currentUser , team.getId ( ) );

        return teamMapper.toDto ( team );

    }

    @Override
    @Transactional
    public TeamResponseDto updateTeam(Long teamId , TeamUpdateDto teamUpdateDto) {

        Objects.requireNonNull ( teamUpdateDto , "Team can not be null" );
        Objects.requireNonNull ( teamId , "Team ID can not be null" );

        if  (   teamUpdateDto.name ( ) == null &&
                teamUpdateDto.description ( ) == null &&
                teamUpdateDto.status ( ) == null) {

            throw new IllegalArgumentException ( "At least one field must be provided for update" );
        }

        var team = teamExistsAndActiveCheck ( teamId );
        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );
        isMemberInTeam ( currentUser , teamId );
        isUserTeamOwnerOrAdmin ( teamId , currentUser.getId ( ) );
        updateTeamFields ( team , teamUpdateDto , currentUser.getId ( ) , teamId );

        var toSave = teamRepository.save ( team );
        log.info ( "Team '{}' updated by user {}" , team.getName ( ) , currentUser.getId ( ) );
        return teamMapper.toDto ( toSave );


    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId) {

        Objects.requireNonNull ( teamId , "Team ID can not be null" );

        var team = teamExistsAndActiveCheck ( teamId );
        var currentUser = getCurrentUser ( );

        isUserActive ( currentUser );

        if (!isOwner ( currentUser.getId ( ) , teamId ))
            throw new AccessDeniedException ( "Only team owners can delete the team" );

        isTeamDeleted ( team );

        team.setStatus ( TeamStatus.DELETED );
        teamRepository.save ( team );

        updateTeamMembersStatus ( teamId , TeamMemberStatus.INACTIVE );


        log.info ( "Team '{}' is deleted by the user with id ' {}'" ,
                team.getName ( ) , currentUser.getId ( ) );
    }



    @Override
    @Transactional
    public TeamResponseDto restoreTeam(Long teamId) {

        Objects.requireNonNull (  teamId, "Team ID can not be null" );

        var currentUser = getCurrentUser ( );
        isUserActive ( currentUser );

        if (currentUser.getRole () != Role.ADMIN)
            throw new AccessDeniedException ( "Only admins can restore deleted or inactive teams" );

        var team = teamRepository.findById ( teamId )
                .orElseThrow ( () -> new TeamNotFoundException ( teamId ) );

        if (team.getStatus () == TeamStatus.ACTIVE)
            throw new TeamAlreadyActive ( teamId );

        team.setStatus ( TeamStatus.ACTIVE );
        var toSave = teamRepository.save ( team );

        log.info("Team '{}' (ID: {}) is activated by admin {}",
                team.getName(), team.getId(), currentUser.getId());

        updateTeamMembersStatus (  teamId , TeamMemberStatus.ACTIVE );

        log.info ( "The members of the team '{}' is activated again " , team.getId () );

        return teamMapper.toDto ( toSave );
    }

    // HELPER METHODS


    private User getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext ( ).getAuthentication ( );
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException ( "Authentication required" );
        }

        String email = auth.getName ( );
        return userRepository.findByEmailIgnoreCase ( email )
                .orElseThrow ( () -> new UserNotFoundException ( "Current user not found" ) );

    }

    private void isUserActive(User user) {

        if (user.getStatus ( ) != UserStatus.ACTIVE)
            throw new UserNotActiveException ( user.getEmail ( ) );
    }

    private Team teamExistsCheck (Long teamId) {

        return teamRepository.findById (  teamId )
                .orElseThrow ( () -> new TeamNotFoundException ( teamId.toString ( ) ) );
    }

    private Team teamExistsAndActiveCheck(Long teamId) {


        var team = teamRepository.findById ( teamId )
                .orElseThrow ( () -> new TeamNotFoundException ( teamId ) );

        if (team.getStatus ( ) != TeamStatus.ACTIVE)
            throw new TeamNotFoundException ( teamId );

        return team;

    }

    private void isTeamDeleted (Team team) {


        if (team.getStatus ()== TeamStatus.DELETED)
            throw new TeamAlreadyDeletedException ( team.getId () );
    }

    private boolean isOwner(Long userId , Long teamId) {

        return teamMemberRepository.existsByTeamIdAndUserIdAndRole ( teamId , userId , TeamRole.OWNER );

    }

    private void isUserTeamOwnerOrAdmin(Long teamId , Long userId) {

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn ( teamId , userId ,
                java.util.Arrays.asList ( TeamRole.OWNER , TeamRole.ADMIN ) ))

            throw new AccessDeniedException ( "Only team owners or admins can perform this action" );

    }

    private void teamUniqueNameCheck(String teamName) {

        if (teamRepository.existsByNameIgnoreCase ( teamName ))
            throw new TeamNameAlreadyExistsException ( teamName );

    }

    private TeamMember teamOwnerBuilder(User user , Long teamId) {

        return TeamMember.builder ( )

                .teamId ( teamId )
                .userId ( user.getId ( ) )
                .role ( TeamRole.OWNER )
                .joinedAt ( Instant.now ( ) )
                .status ( TeamMemberStatus.ACTIVE )

                .build ( );
    }

    private void isMemberInTeam(User user , Long teamId) {

        if (!teamMemberRepository.existsByTeamIdAndUserId ( teamId , user.getId ( ) ))
            throw new AccessDeniedException ( "Members can only access teams they are already within" );

    }

    private void teamUniqueNameCheckForUpdate(String teamName , Long teamId) {
        if (teamRepository.existsByNameIgnoreCaseAndIdNot ( teamName , teamId ))
            throw new TeamNameAlreadyExistsException ( teamName );
    }

    private void updateTeamFields(Team team , TeamUpdateDto teamUpdateDto , Long userId , Long teamId) {

        if (teamUpdateDto.name ( ) != null && !teamUpdateDto.name ( ).isBlank ( )) {
            teamUniqueNameCheckForUpdate ( teamUpdateDto.name ( ) , team.getId ( ) );
            team.setName ( teamUpdateDto.name ( ).trim ( ) );
        }

        if (teamUpdateDto.description ( ) != null) {
            team.setDescription ( teamUpdateDto.description ( ).trim ( ) );
        }

        if (teamUpdateDto.status ( ) != null) {

            if (!isOwner ( userId , teamId ))
                throw new AccessDeniedException ( "Only team owners can change the team status" );

            team.setStatus ( teamUpdateDto.status ( ) );
        }
    }

    private void updateTeamMembersStatus(Long teamId , TeamMemberStatus status) {

        var members = teamMemberRepository.findByTeamId ( teamId );

        members.forEach ( member -> member.setStatus ( status ));

        teamMemberRepository.saveAll ( members );

    }



}

package com.taskmanagement.team.service;

import com.taskmanagement.common.exception.types.Exceptions.AccessDeniedException;
import com.taskmanagement.common.exception.types.Exceptions.UserAlreadyInTeamException;
import com.taskmanagement.common.exception.types.Exceptions.UserNotInTeamException;
import com.taskmanagement.team.dto.AddMemberRequestDto;
import com.taskmanagement.team.dto.TeamMemberResponseDto;
import com.taskmanagement.team.dto.UpdateMemberRoleDto;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.mapper.TeamMemberMapper;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.user.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service

public class TeamMemberServiceImplementation implements TeamMemberService {

    private final SecurityHelper securityHelper;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    @Transactional

    public TeamMemberResponseDto addMember(AddMemberRequestDto dto) {

        Objects.requireNonNull(dto , "The new member must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var currentUserTeam = securityHelper.teamExistsAndActiveCheck ( dto.teamId ( ) );
        var userToAdd = securityHelper.userExistsAndActiveCheck ( dto.userId ( ) );

        if (! securityHelper.isOwner ( currentUser.getId () , currentUserTeam.getId () ) )
            throw new  AccessDeniedException ( "Only team owner can add new members " );

        if ( securityHelper.isMemberInTeam (  dto.teamId ( ) , userToAdd ) )
            throw new UserAlreadyInTeamException ( userToAdd.getId () , dto.teamId ( ) );

        var toSaveMember = teamMemberMapper.toEntity ( dto );
        var savedMember =  teamMemberRepository.save ( toSaveMember );

        log.info("User {} added user {} to team {} with role {}",
                currentUser.getId(), userToAdd.getId(), dto.teamId(), dto.role());

        return teamMemberMapper.toDto ( savedMember , userToAdd );

    }


    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId) {


        Objects.requireNonNull(teamId, "Team ID must not be null");
        Objects.requireNonNull(userId, "User ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var team = securityHelper.teamExistsAndActiveCheck(teamId);

        if (!securityHelper.isOwner(currentUser.getId(), team.getId())) {
            throw new AccessDeniedException("Only team owner can remove members");
        }

        if (securityHelper.isSelfOperation(currentUser.getId(), userId)) {
            throw new AccessDeniedException(
                    "Team owner cannot remove themselves. " +
                            "Transfer ownership or delete the team."
            );
        }

        var teamMemberToRemove = teamMemberRepository
                .findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new UserNotInTeamException(userId, teamId));

        teamMemberToRemove.setStatus(TeamMemberStatus.REMOVED);
        teamMemberRepository.save(teamMemberToRemove);

        log.info("User {} removed user {} from team {}",
                currentUser.getId(), userId, teamId);
    }

    @Override
    @Transactional
    public TeamMemberResponseDto updateMemberRole(UpdateMemberRoleDto dto) {

        Objects.requireNonNull (  dto, "The new member must not be null" );

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var team = securityHelper.teamExistsAndActiveCheck ( dto.teamId ( ) );
        if (! securityHelper.isOwner ( currentUser.getId () , team.getId () ) )
            throw new  AccessDeniedException ( "Only team owner can update member roles " );

        var memberToUpdate = teamMemberRepository
                .findByTeamIdAndUserId ( dto.teamId ( ) , dto.memberId ( ) )
                .orElseThrow ( () -> new UserNotInTeamException ( dto.memberId ( ) , dto.teamId ( ) ) );

        if (securityHelper.isSelfOperation (  currentUser.getId () , dto.memberId () ))
            throw new AccessDeniedException ( "Team owner cannot update their own role," +
                    " please contact an admin " );

        securityHelper.roleTransitionValidation (team.getId (),memberToUpdate.getRole (),dto.newRole ( ) );

        var oldRole = memberToUpdate.getRole ();

        memberToUpdate.setRole ( dto.newRole ( ) );
        var updatedMember = teamMemberRepository.save ( memberToUpdate );

        log.info("User {} updated role of user {} in team {} from {} to {}",
                currentUser.getId(), dto.memberId (), dto.teamId (), oldRole, dto.newRole () );

        var user = securityHelper.getUserById (  memberToUpdate.getUserId () );

        return teamMemberMapper.toDto ( updatedMember , user );


    }

    @Override
    @Transactional
    public void leaveTeam(Long teamId ) {

        Objects.requireNonNull ( teamId , "The team can not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        var team = securityHelper.teamExistsAndActiveCheck ( teamId );

        if (! securityHelper.IsMemberInTheTeam (  currentUser.getId () , team.getId () ) )
            throw new UserNotInTeamException ( currentUser.getId () , teamId );

        if (securityHelper.isOwner ( currentUser.getId ( ) , teamId ) &&
                securityHelper.isLastOwner ( teamId ))

            throw new AccessDeniedException ( "The last owner of the team cannot leave the team. " +
                    "Please transfer ownership before leaving the team." );

        if (securityHelper.isLastActiveTeamMember ( teamId ))

            throw new AccessDeniedException ( "The last active member of the team cannot leave the team. " +
                    "Please delete the team or add new members before leaving the team." );

        var teamMember = securityHelper.getTeamMember ( teamId , currentUser.getId () );

        teamMember.setStatus ( TeamMemberStatus.INACTIVE );

        teamMemberRepository.save ( teamMember );

        log.info("User {} left team {}", currentUser.getId(), teamId);


    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamMemberResponseDto> getMembersByTeam(Long teamId , Pageable pageable) {

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        var team = securityHelper.teamExistsAndActiveCheck ( teamId );

        if (currentUser.getRole ( ) != Role.ADMIN &&
                !securityHelper.isTeamOwnerOrAdmin ( teamId , currentUser.getId ( ) ))

            throw new AccessDeniedException ( "Only team owner or admins can view members " );

        return teamMemberRepository.findByTeamIdAndStatusActive ( teamId , pageable )
                .map ( teamMember -> teamMemberMapper.toDto
                        ( teamMember , securityHelper.getUserById ( teamMember.getUserId ( ) ) ) );

    }

    @Override
    @Transactional (readOnly = true)

    public TeamMemberResponseDto getMember(Long teamId , Long userId) {

        Objects.requireNonNull ( userId , "The user can not be null" );
        Objects.requireNonNull ( teamId , "The team can not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        var team = securityHelper.teamExistsAndActiveCheck ( teamId );

            if (currentUser.getRole ()!= Role.ADMIN &&
                    !securityHelper.isTeamOwnerOrAdmin ( teamId , currentUser.getId () ) &&
                    !securityHelper.isSelfOperation ( currentUser.getId () , userId ) )

                throw new AccessDeniedException
                        (  "Only team owner , admins or the user themselves can view member details " );


            return teamMemberRepository.findByTeamIdAndUserId ( teamId , userId )
                    .map ( teamMember -> teamMemberMapper.toDto
                            (  teamMember , securityHelper.getUserById ( teamMember.getUserId () ) ) )
                    .orElseThrow ( () -> new UserNotInTeamException ( userId , teamId ) );
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalMembersCountForAdmin(Long teamId) {

        Objects.requireNonNull ( teamId , "Team ID cannot be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );


        if (currentUser.getRole ( ) != Role.ADMIN) {
            throw new AccessDeniedException ( "Only system admins can access member statistics" );
        }

        securityHelper.teamExists (  teamId );

        return teamMemberRepository.countByTeamId ( teamId );
    }

    @Override
    @Transactional (readOnly = true)
    public Long getActiveMembersCount(Long teamId) {

        Objects.requireNonNull ( teamId , "Team ID cannot be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        if (currentUser.getRole ( ) != Role.ADMIN) {
            throw new AccessDeniedException ( "Only system admins can access member statistics" );
        }

        securityHelper.teamExists (  teamId );

        return teamMemberRepository.countByTeamIdAndStatusActive ( teamId );

    }
}

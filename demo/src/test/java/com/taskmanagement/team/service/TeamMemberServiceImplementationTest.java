package com.taskmanagement.team.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.team.dto.AddMemberRequestDto;
import com.taskmanagement.team.dto.TeamMemberResponseDto;
import com.taskmanagement.team.dto.UpdateMemberRoleDto;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.team.mapper.TeamMemberMapper;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for TeamMemberServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamMemberServiceImplementation Unit Tests")
class TeamMemberServiceImplementationTest {

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @InjectMocks
    private TeamMemberServiceImplementation teamMemberService;

    private User ownerUser;
    private User userToAdd;
    private User suspendedUser;
    private Team activeTeam;
    private TeamMember teamMember;
    private TeamMemberResponseDto teamMemberResponseDto;

    @BeforeEach
    void setUp() {
        // Setup owner user
        ownerUser = User.builder()
                .email("owner@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Owner")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        ownerUser.setId(1L);

        // Setup user to add
        userToAdd = User.builder()
                .email("newuser@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("New")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        userToAdd.setId(2L);

        // Setup suspended user
        suspendedUser = User.builder()
                .email("suspended@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Suspended")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.SUSPENDED)
                .emailVerified(true)
                .build();
        suspendedUser.setId(3L);

        // Setup active team
        activeTeam = Team.builder()
                .name("Development Team")
                .description("Dev team description")
                .ownerId(ownerUser.getId())
                .status(TeamStatus.ACTIVE)
                .build();
        activeTeam.setId(100L);

        // Setup team member
        teamMember = TeamMember.builder()
                .teamId(activeTeam.getId())
                .userId(userToAdd.getId())
                .role(TeamRole.MEMBER)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
        teamMember.setId(1L);

        // Setup team member response DTO
        teamMemberResponseDto = new TeamMemberResponseDto(
                teamMember.getId(),
                teamMember.getTeamId(),
                teamMember.getUserId(),
                userToAdd.getEmail(),
                userToAdd.getFirstName(),
                userToAdd.getLastName(),
                teamMember.getRole(),
                teamMember.getJoinedAt()
        );
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Nested
    @DisplayName("AddMember Tests")
    class AddMemberTests {

        private AddMemberRequestDto addMemberRequestDto;

        @BeforeEach
        void setUpAddMemberTests() {
            addMemberRequestDto = new AddMemberRequestDto(
                    userToAdd.getId(),
                    activeTeam.getId(),
                    TeamRole.MEMBER
            );
        }

        @Test
        @DisplayName("Should successfully add member when all conditions are met")
        void shouldAddMemberSuccessfully() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId())).thenReturn(userToAdd);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isMemberInTeam(activeTeam.getId(), userToAdd)).thenReturn(false);
            when(teamMemberMapper.toEntity(addMemberRequestDto)).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMemberMapper.toDto(teamMember, userToAdd)).thenReturn(teamMemberResponseDto);

            // Act
            TeamMemberResponseDto result = teamMemberService.addMember(addMemberRequestDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.teamId()).isEqualTo(activeTeam.getId());
            assertThat(result.userId()).isEqualTo(userToAdd.getId());
            assertThat(result.role()).isEqualTo(TeamRole.MEMBER);

            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(ownerUser);
            verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            verify(securityHelper).userExistsAndActiveCheck(userToAdd.getId());
            verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            verify(securityHelper).isMemberInTeam(activeTeam.getId(), userToAdd);
            verify(teamMemberRepository).save(any(TeamMember.class));
            verify(teamMemberMapper).toDto(teamMember, userToAdd);
        }

        @Test
        @DisplayName("Should add member with ADMIN role when specified")
        void shouldAddMemberWithAdminRole() {
            // Arrange
            AddMemberRequestDto adminDto = new AddMemberRequestDto(
                    userToAdd.getId(),
                    activeTeam.getId(),
                    TeamRole.ADMIN
            );
            TeamMember adminMember = TeamMember.builder()
                    .teamId(activeTeam.getId())
                    .userId(userToAdd.getId())
                    .role(TeamRole.ADMIN)
                    .status(TeamMemberStatus.ACTIVE)
                    .build();

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId())).thenReturn(userToAdd);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isMemberInTeam(activeTeam.getId(), userToAdd)).thenReturn(false);
            when(teamMemberMapper.toEntity(adminDto)).thenReturn(adminMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(adminMember);
            when(teamMemberMapper.toDto(adminMember, userToAdd)).thenReturn(teamMemberResponseDto);

            // Act
            TeamMemberResponseDto result = teamMemberService.addMember(adminDto);

            // Assert
            assertThat(result).isNotNull();
            verify(teamMemberMapper).toEntity(adminDto);
        }

        @Test
        @DisplayName("Should throw NullPointerException when AddMemberRequestDto is null")
        void shouldThrowNullPointerExceptionWhenDtoIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("The new member must not be null");

            verifyNoInteractions(securityHelper);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMemberMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is missing")
        void shouldThrowAccessDeniedExceptionWhenAuthMissing() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenThrow(new AccessDeniedException("Authentication required"));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            verify(securityHelper).getCurrentUser();
            verify(securityHelper, never()).isUserActive(any());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when current user not found")
        void shouldThrowUserNotFoundExceptionWhenCurrentUserNotFound() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenThrow(new UserNotFoundException("Current user not found"));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Current user not found");

            verify(securityHelper).getCurrentUser();
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when current user is not active")
        void shouldThrowUserNotActiveExceptionWhenCurrentUserNotActive() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(suspendedUser);
            doThrow(new UserNotActiveException(suspendedUser.getEmail()))
                    .when(securityHelper).isUserActive(suspendedUser);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(suspendedUser);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenTeamNotExists() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId()))
                    .thenThrow(new TeamNotFoundException(activeTeam.getId()));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team is not active")
        void shouldThrowTeamNotFoundExceptionWhenTeamNotActive() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId()))
                    .thenThrow(new TeamNotFoundException(activeTeam.getId().toString()));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user to add does not exist")
        void shouldThrowUserNotFoundExceptionWhenUserToAddNotExists() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId()))
                    .thenThrow(new UserNotFoundException(userToAdd.getId()));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(UserNotFoundException.class);

            verify(securityHelper).userExistsAndActiveCheck(userToAdd.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user to add is not active")
        void shouldThrowUserNotActiveExceptionWhenUserToAddNotActive() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId()))
                    .thenThrow(new UserNotActiveException(userToAdd.getEmail()));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(securityHelper).userExistsAndActiveCheck(userToAdd.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when current user is not owner")
        void shouldThrowAccessDeniedExceptionWhenNotOwner() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId())).thenReturn(userToAdd);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only team owner can add new members ");

            verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserAlreadyInTeamException when user is already in team")
        void shouldThrowUserAlreadyInTeamExceptionWhenUserAlreadyMember() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId())).thenReturn(userToAdd);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isMemberInTeam(activeTeam.getId(), userToAdd)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.addMember(addMemberRequestDto))
                    .isInstanceOf(UserAlreadyInTeamException.class);

            verify(securityHelper).isMemberInTeam(activeTeam.getId(), userToAdd);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should verify method execution order")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId())).thenReturn(userToAdd);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isMemberInTeam(activeTeam.getId(), userToAdd)).thenReturn(false);
            when(teamMemberMapper.toEntity(addMemberRequestDto)).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMemberMapper.toDto(teamMember, userToAdd)).thenReturn(teamMemberResponseDto);

            // Act
            teamMemberService.addMember(addMemberRequestDto);

            // Assert - verify execution order
            var inOrder = inOrder(securityHelper, teamMemberRepository, teamMemberMapper);
            inOrder.verify(securityHelper).getCurrentUser();
            inOrder.verify(securityHelper).isUserActive(ownerUser);
            inOrder.verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            inOrder.verify(securityHelper).userExistsAndActiveCheck(userToAdd.getId());
            inOrder.verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            inOrder.verify(securityHelper).isMemberInTeam(activeTeam.getId(), userToAdd);
            inOrder.verify(teamMemberMapper).toEntity(addMemberRequestDto);
            inOrder.verify(teamMemberRepository).save(any(TeamMember.class));
            inOrder.verify(teamMemberMapper).toDto(teamMember, userToAdd);
        }

        @Test
        @DisplayName("Should save team member with correct properties")
        void shouldSaveTeamMemberWithCorrectProperties() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.userExistsAndActiveCheck(userToAdd.getId())).thenReturn(userToAdd);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isMemberInTeam(activeTeam.getId(), userToAdd)).thenReturn(false);
            when(teamMemberMapper.toEntity(addMemberRequestDto)).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMemberMapper.toDto(teamMember, userToAdd)).thenReturn(teamMemberResponseDto);

            // Act
            teamMemberService.addMember(addMemberRequestDto);

            // Assert
            ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
            verify(teamMemberRepository).save(memberCaptor.capture());
            TeamMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getTeamId()).isEqualTo(activeTeam.getId());
            assertThat(savedMember.getUserId()).isEqualTo(userToAdd.getId());
        }
    }

    @Nested
    @DisplayName("RemoveMember Tests")
    class RemoveMemberTests {

        private Long teamId;
        private Long userIdToRemove;
        private User userToRemove;
        private TeamMember memberToRemove;

        @BeforeEach
        void setUpRemoveMemberTests() {
            teamId = activeTeam.getId();
            userIdToRemove = 5L;

            userToRemove = User.builder()
                    .email("remove@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userToRemove.setId(userIdToRemove);

            memberToRemove = TeamMember.builder()
                    .teamId(teamId)
                    .userId(userIdToRemove)
                    .role(TeamRole.MEMBER)
                    .status(TeamMemberStatus.ACTIVE)
                    .joinedAt(Instant.now())
                    .build();
            memberToRemove.setId(10L);
        }

        @Test
        @DisplayName("Should successfully remove member when all conditions are met")
        void shouldRemoveMemberSuccessfully() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId)).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isSelfOperation(ownerUser.getId(), userIdToRemove)).thenReturn(false);
            when(teamMemberRepository.findByTeamIdAndUserId(teamId, userIdToRemove))
                    .thenReturn(Optional.of(memberToRemove));
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(memberToRemove);

            // Act
            teamMemberService.removeMember(teamId, userIdToRemove);

            // Assert
            ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
            verify(teamMemberRepository).save(memberCaptor.capture());
            TeamMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getStatus()).isEqualTo(TeamMemberStatus.REMOVED);

            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(ownerUser);
            verify(securityHelper).teamExistsAndActiveCheck(teamId);
            verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            verify(securityHelper).isSelfOperation(ownerUser.getId(), userIdToRemove);
            verify(teamMemberRepository).findByTeamIdAndUserId(teamId, userIdToRemove);
        }

        @Test
        @DisplayName("Should throw NullPointerException when teamId is null")
        void shouldThrowNullPointerExceptionWhenTeamIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(null, userIdToRemove))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team ID must not be null");

            verifyNoInteractions(securityHelper);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw NullPointerException when userId is null")
        void shouldThrowNullPointerExceptionWhenUserIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("User ID must not be null");

            verifyNoInteractions(securityHelper);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is missing")
        void shouldThrowAccessDeniedExceptionWhenAuthMissing() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenThrow(new AccessDeniedException("Authentication required"));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, userIdToRemove))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            verify(securityHelper).getCurrentUser();
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when current user is not active")
        void shouldThrowUserNotActiveExceptionWhenCurrentUserNotActive() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(suspendedUser);
            doThrow(new UserNotActiveException(suspendedUser.getEmail()))
                    .when(securityHelper).isUserActive(suspendedUser);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, userIdToRemove))
                    .isInstanceOf(UserNotActiveException.class);

            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(suspendedUser);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenTeamNotExists() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId))
                    .thenThrow(new TeamNotFoundException(teamId));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, userIdToRemove))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(securityHelper).teamExistsAndActiveCheck(teamId);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when current user is not owner")
        void shouldThrowAccessDeniedExceptionWhenNotOwner() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId)).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, userIdToRemove))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only team owner can remove members");

            verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when owner tries to remove themselves")
        void shouldThrowAccessDeniedExceptionWhenOwnerRemovesThemselves() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId)).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isSelfOperation(ownerUser.getId(), ownerUser.getId())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, ownerUser.getId()))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Team owner cannot remove themselves. Transfer ownership or delete the team.");

            verify(securityHelper).isSelfOperation(ownerUser.getId(), ownerUser.getId());
            verify(teamMemberRepository, never()).findByTeamIdAndUserId(any(), any());
        }

        @Test
        @DisplayName("Should throw UserNotInTeamException when user is not in team")
        void shouldThrowUserNotInTeamExceptionWhenUserNotInTeam() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId)).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isSelfOperation(ownerUser.getId(), userIdToRemove)).thenReturn(false);
            when(teamMemberRepository.findByTeamIdAndUserId(teamId, userIdToRemove))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.removeMember(teamId, userIdToRemove))
                    .isInstanceOf(UserNotInTeamException.class);

            verify(teamMemberRepository).findByTeamIdAndUserId(teamId, userIdToRemove);
            verify(teamMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update member status to REMOVED, not delete from database")
        void shouldUpdateStatusToRemovedNotDelete() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId)).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isSelfOperation(ownerUser.getId(), userIdToRemove)).thenReturn(false);
            when(teamMemberRepository.findByTeamIdAndUserId(teamId, userIdToRemove))
                    .thenReturn(Optional.of(memberToRemove));
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(memberToRemove);

            // Act
            teamMemberService.removeMember(teamId, userIdToRemove);

            // Assert
            verify(teamMemberRepository).save(any(TeamMember.class));
            verify(teamMemberRepository, never()).delete(any());
            verify(teamMemberRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should verify method execution order")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(teamId)).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(securityHelper.isSelfOperation(ownerUser.getId(), userIdToRemove)).thenReturn(false);
            when(teamMemberRepository.findByTeamIdAndUserId(teamId, userIdToRemove))
                    .thenReturn(Optional.of(memberToRemove));
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(memberToRemove);

            // Act
            teamMemberService.removeMember(teamId, userIdToRemove);

            // Assert - verify execution order
            var inOrder = inOrder(securityHelper, teamMemberRepository);
            inOrder.verify(securityHelper).getCurrentUser();
            inOrder.verify(securityHelper).isUserActive(ownerUser);
            inOrder.verify(securityHelper).teamExistsAndActiveCheck(teamId);
            inOrder.verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            inOrder.verify(securityHelper).isSelfOperation(ownerUser.getId(), userIdToRemove);
            inOrder.verify(teamMemberRepository).findByTeamIdAndUserId(teamId, userIdToRemove);
            inOrder.verify(teamMemberRepository).save(any(TeamMember.class));
        }
    }

    @Nested
    @DisplayName("UpdateMemberRole Tests")
    class UpdateMemberRoleTests {

        private UpdateMemberRoleDto updateMemberRoleDto;
        private Long memberIdToUpdate;
        private User memberUser;
        private TeamMember existingMember;

        @BeforeEach
        void setUpUpdateRoleTests() {
            memberIdToUpdate = 7L;
            memberUser = User.builder()
                    .email("member@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            memberUser.setId(memberIdToUpdate);

            existingMember = TeamMember.builder()
                    .teamId(activeTeam.getId())
                    .userId(memberIdToUpdate)
                    .role(TeamRole.MEMBER)
                    .status(TeamMemberStatus.ACTIVE)
                    .joinedAt(Instant.now())
                    .build();
            existingMember.setId(15L);

            updateMemberRoleDto = new UpdateMemberRoleDto(
                    memberIdToUpdate,
                    activeTeam.getId(),
                    TeamRole.ADMIN
            );
        }

        @Test
        @DisplayName("Should successfully update member role from MEMBER to ADMIN")
        void shouldUpdateMemberRoleSuccessfully() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doNothing().when(securityHelper).roleTransitionValidation(
                    activeTeam.getId(), TeamRole.MEMBER, TeamRole.ADMIN);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(existingMember);
            when(securityHelper.getUserById(memberIdToUpdate)).thenReturn(memberUser);
            when(teamMemberMapper.toDto(existingMember, memberUser)).thenReturn(teamMemberResponseDto);

            // Act
            TeamMemberResponseDto result = teamMemberService.updateMemberRole(updateMemberRoleDto);

            // Assert
            assertThat(result).isNotNull();
            ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
            verify(teamMemberRepository).save(memberCaptor.capture());
            TeamMember updatedMember = memberCaptor.getValue();
            assertThat(updatedMember.getRole()).isEqualTo(TeamRole.ADMIN);

            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(ownerUser);
            verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            verify(teamMemberRepository).findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate);
            verify(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.MEMBER, TeamRole.ADMIN);
        }

        @Test
        @DisplayName("Should successfully update member role from ADMIN to MEMBER")
        void shouldDemoteAdminToMember() {
            // Arrange
            existingMember.setRole(TeamRole.ADMIN);
            UpdateMemberRoleDto demoteDto = new UpdateMemberRoleDto(
                    memberIdToUpdate,
                    activeTeam.getId(),
                    TeamRole.MEMBER
            );

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doNothing().when(securityHelper).roleTransitionValidation(
                    activeTeam.getId(), TeamRole.ADMIN, TeamRole.MEMBER);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(existingMember);
            when(securityHelper.getUserById(memberIdToUpdate)).thenReturn(memberUser);
            when(teamMemberMapper.toDto(existingMember, memberUser)).thenReturn(teamMemberResponseDto);

            // Act
            TeamMemberResponseDto result = teamMemberService.updateMemberRole(demoteDto);

            // Assert
            assertThat(result).isNotNull();
            verify(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.ADMIN, TeamRole.MEMBER);
        }

        @Test
        @DisplayName("Should throw NullPointerException when UpdateMemberRoleDto is null")
        void shouldThrowNullPointerExceptionWhenDtoIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("The new member must not be null");

            verifyNoInteractions(securityHelper);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMemberMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is missing")
        void shouldThrowAccessDeniedExceptionWhenAuthMissing() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenThrow(new AccessDeniedException("Authentication required"));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(updateMemberRoleDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            verify(securityHelper).getCurrentUser();
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when current user is not active")
        void shouldThrowUserNotActiveExceptionWhenCurrentUserNotActive() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(suspendedUser);
            doThrow(new UserNotActiveException(suspendedUser.getEmail()))
                    .when(securityHelper).isUserActive(suspendedUser);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(updateMemberRoleDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(suspendedUser);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenTeamNotExists() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId()))
                    .thenThrow(new TeamNotFoundException(activeTeam.getId()));

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(updateMemberRoleDto))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when current user is not owner")
        void shouldThrowAccessDeniedExceptionWhenNotOwner() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(updateMemberRoleDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only team owner can update member roles ");

            verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotInTeamException when member to update is not in team")
        void shouldThrowUserNotInTeamExceptionWhenMemberNotInTeam() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(updateMemberRoleDto))
                    .isInstanceOf(UserNotInTeamException.class);

            verify(teamMemberRepository).findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate);
            verify(teamMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when owner tries to update their own role")
        void shouldThrowAccessDeniedExceptionWhenOwnerUpdatesOwnRole() {
            // Arrange
            UpdateMemberRoleDto selfUpdateDto = new UpdateMemberRoleDto(
                    ownerUser.getId(),
                    activeTeam.getId(),
                    TeamRole.ADMIN
            );

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), ownerUser.getId()))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), ownerUser.getId())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(selfUpdateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Team owner cannot update their own role, please contact an admin ");

            verify(securityHelper).isSelfOperation(ownerUser.getId(), ownerUser.getId());
            verify(securityHelper, never()).roleTransitionValidation(any(), any(), any());
            verify(teamMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidRoleTransitionException when new role is same as current role")
        void shouldThrowInvalidRoleTransitionExceptionWhenSameRole() {
            // Arrange
            UpdateMemberRoleDto sameRoleDto = new UpdateMemberRoleDto(
                    memberIdToUpdate,
                    activeTeam.getId(),
                    TeamRole.MEMBER
            );

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doThrow(new InvalidRoleTransitionException("New role must be different from current role "))
                    .when(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.MEMBER, TeamRole.MEMBER);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(sameRoleDto))
                    .isInstanceOf(InvalidRoleTransitionException.class)
                    .hasMessage("New role must be different from current role ");

            verify(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.MEMBER, TeamRole.MEMBER);
            verify(teamMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidRoleTransitionException when demoting last owner")
        void shouldThrowInvalidRoleTransitionExceptionWhenDemotingLastOwner() {
            // Arrange
            existingMember.setRole(TeamRole.OWNER);
            UpdateMemberRoleDto demoteOwnerDto = new UpdateMemberRoleDto(
                    memberIdToUpdate,
                    activeTeam.getId(),
                    TeamRole.MEMBER
            );

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doThrow(new InvalidRoleTransitionException("Cannot demote the last owner "))
                    .when(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.OWNER, TeamRole.MEMBER);

            // Act & Assert
            assertThatThrownBy(() -> teamMemberService.updateMemberRole(demoteOwnerDto))
                    .isInstanceOf(InvalidRoleTransitionException.class)
                    .hasMessage("Cannot demote the last owner ");

            verify(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.OWNER, TeamRole.MEMBER);
            verify(teamMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should preserve old role before updating")
        void shouldPreserveOldRoleBeforeUpdating() {
            // Arrange
            TeamRole oldRole = TeamRole.MEMBER;
            existingMember.setRole(oldRole);

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doNothing().when(securityHelper).roleTransitionValidation(
                    activeTeam.getId(), oldRole, TeamRole.ADMIN);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(existingMember);
            when(securityHelper.getUserById(memberIdToUpdate)).thenReturn(memberUser);
            when(teamMemberMapper.toDto(existingMember, memberUser)).thenReturn(teamMemberResponseDto);

            // Act
            teamMemberService.updateMemberRole(updateMemberRoleDto);

            // Assert
            verify(securityHelper).roleTransitionValidation(activeTeam.getId(), oldRole, TeamRole.ADMIN);
        }

        @Test
        @DisplayName("Should verify method execution order")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doNothing().when(securityHelper).roleTransitionValidation(
                    activeTeam.getId(), TeamRole.MEMBER, TeamRole.ADMIN);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(existingMember);
            when(securityHelper.getUserById(memberIdToUpdate)).thenReturn(memberUser);
            when(teamMemberMapper.toDto(existingMember, memberUser)).thenReturn(teamMemberResponseDto);

            // Act
            teamMemberService.updateMemberRole(updateMemberRoleDto);

            // Assert - verify execution order
            var inOrder = inOrder(securityHelper, teamMemberRepository, teamMemberMapper);
            inOrder.verify(securityHelper).getCurrentUser();
            inOrder.verify(securityHelper).isUserActive(ownerUser);
            inOrder.verify(securityHelper).teamExistsAndActiveCheck(activeTeam.getId());
            inOrder.verify(securityHelper).isOwner(ownerUser.getId(), activeTeam.getId());
            inOrder.verify(teamMemberRepository).findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate);
            inOrder.verify(securityHelper).isSelfOperation(ownerUser.getId(), memberIdToUpdate);
            inOrder.verify(securityHelper).roleTransitionValidation(activeTeam.getId(), TeamRole.MEMBER, TeamRole.ADMIN);
            inOrder.verify(teamMemberRepository).save(any(TeamMember.class));
            inOrder.verify(securityHelper).getUserById(memberIdToUpdate);
            inOrder.verify(teamMemberMapper).toDto(existingMember, memberUser);
        }

        @Test
        @DisplayName("Should fetch user details after updating role")
        void shouldFetchUserDetailsAfterUpdating() {
            // Arrange
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(activeTeam.getId())).thenReturn(activeTeam);
            when(securityHelper.isOwner(ownerUser.getId(), activeTeam.getId())).thenReturn(true);
            when(teamMemberRepository.findByTeamIdAndUserId(activeTeam.getId(), memberIdToUpdate))
                    .thenReturn(Optional.of(existingMember));
            when(securityHelper.isSelfOperation(ownerUser.getId(), memberIdToUpdate)).thenReturn(false);
            doNothing().when(securityHelper).roleTransitionValidation(
                    activeTeam.getId(), TeamRole.MEMBER, TeamRole.ADMIN);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(existingMember);
            when(securityHelper.getUserById(memberIdToUpdate)).thenReturn(memberUser);
            when(teamMemberMapper.toDto(existingMember, memberUser)).thenReturn(teamMemberResponseDto);

            // Act
            teamMemberService.updateMemberRole(updateMemberRoleDto);

            // Assert
            verify(securityHelper).getUserById(memberIdToUpdate);
            verify(teamMemberMapper).toDto(existingMember, memberUser);
        }
    }
}


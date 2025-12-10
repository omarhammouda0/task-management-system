package com.taskmanagement.team.service;

import com.taskmanagement.common.exception.types.Exceptions.AccessDeniedException;
import com.taskmanagement.common.exception.types.Exceptions.TeamNameAlreadyExistsException;
import com.taskmanagement.common.exception.types.Exceptions.TeamNotFoundException;
import com.taskmanagement.common.exception.types.Exceptions.UserNotActiveException;
import com.taskmanagement.common.exception.types.Exceptions.UserNotFoundException;
import com.taskmanagement.team.dto.TeamCreateDto;
import com.taskmanagement.team.dto.TeamResponseDto;
import com.taskmanagement.team.dto.TeamUpdateDto;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.team.mapper.TeamMapper;
import com.taskmanagement.team.mapper.TeamMemberMapper;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.team.repository.TeamRepository;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.repository.UserRepository;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for TeamServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases for createTeam method.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamServiceImplementation Unit Tests")
class TeamServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @Mock
    private TeamMemberRepository teamMemberRepository;


    @InjectMocks
    private TeamServiceImplementation teamService;

    private User activeUser;
    private User suspendedUser;
    private User inactiveUser;
    private User deletedUser;
    private TeamCreateDto teamCreateDto;
    private Team team;
    private TeamResponseDto teamResponseDto;
    private TeamMember teamMember;

    @BeforeEach
    void setUp() {
        // Setup active user
        activeUser = User.builder()
                .email("user@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        activeUser.setId(1L);

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
        suspendedUser.setId(2L);

        // Setup inactive user
        inactiveUser = User.builder()
                .email("inactive@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Inactive")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.INACTIVE)
                .emailVerified(true)
                .build();
        inactiveUser.setId(3L);

        // Setup deleted user
        deletedUser = User.builder()
                .email("deleted@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Deleted")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.DELETED)
                .emailVerified(true)
                .build();
        deletedUser.setId(4L);

        // Setup team create DTO
        teamCreateDto = new TeamCreateDto(
                "Development Team",
                "A team for software development"
        );

        // Setup team entity
        team = Team.builder()
                .name("Development Team")
                .description("A team for software development")
                .owner(activeUser)
                .status(TeamStatus.ACTIVE)
                .build();
        team.setId(100L);
        team.setCreatedAt(Instant.now());

        // Setup team response DTO
        teamResponseDto = new TeamResponseDto(
                100L,
                "Development Team",
                "A team for software development",
                1L,
                TeamStatus.ACTIVE,
                Instant.now()
        );

        // Setup team member with proper JPA relationships
        teamMember = TeamMember.builder()
                .team(team)
                .user(activeUser)
                .role(TeamRole.OWNER)
                .status(TeamMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
        teamMember.setId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper method to setup authentication context
     */
    private void setupAuthentication(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Helper method to setup anonymous authentication
     */
    private void setupAnonymousAuthentication() {
        Authentication authentication = new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Helper method to clear authentication
     */
    private void clearAuthentication() {
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("CreateTeam Tests")
    class CreateTeamTests {

        @Test
        @DisplayName("Should successfully create team when all conditions are met")
        void shouldCreateTeamSuccessfully() {
            // Arrange
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(teamCreateDto.name()))
                    .thenReturn(false);
            when(teamMapper.toEntity(teamCreateDto)).thenReturn(team);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMemberMapper.toOwnerEntity(any(Team.class), any(User.class))).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.createTeam(teamCreateDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.name()).isEqualTo("Development Team");
            assertThat(result.description()).isEqualTo("A team for software development");
            assertThat(result.ownerId()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo(TeamStatus.ACTIVE);

            // Verify interactions
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verify(teamRepository).existsByNameIgnoreCase(teamCreateDto.name());
            verify(teamMapper).toEntity(teamCreateDto);
            verify(teamRepository).save(any(Team.class));
            verify(teamMemberMapper).toOwnerEntity(any(Team.class), eq(activeUser));
            verify(teamMemberRepository).save(any(TeamMember.class));
            verify(teamMapper).toDto(team);

            // Verify team owner was set
            ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getOwner()).isEqualTo(activeUser);

            // Verify team member was created with correct properties
            ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
            verify(teamMemberRepository).save(memberCaptor.capture());
            TeamMember savedMember = memberCaptor.getValue();
            // Check JPA relationships since derived columns (teamId, userId) are null in unit tests
            assertThat(savedMember.getTeam().getId()).isEqualTo(team.getId());
            assertThat(savedMember.getUser().getId()).isEqualTo(activeUser.getId());
            assertThat(savedMember.getRole()).isEqualTo(TeamRole.OWNER);
            assertThat(savedMember.getJoinedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw NullPointerException when TeamCreateDto is null")
        void shouldThrowNullPointerExceptionWhenTeamCreateDtoIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team can not be null");

            // Verify no repository calls were made (null check happens before any repository access)
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is null")
        void shouldThrowAccessDeniedExceptionWhenAuthenticationIsNull() {
            // Arrange
            clearAuthentication();

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            // Verify no repository calls were made
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is anonymous")
        void shouldThrowAccessDeniedExceptionWhenUserIsAnonymous() {
            // Arrange
            setupAnonymousAuthentication();

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            // Verify no repository calls were made
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when current user is not found")
        void shouldThrowUserNotFoundExceptionWhenCurrentUserNotFound() {
            // Arrange
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with email 'Current user not found' not found");

            // Verify interactions
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is SUSPENDED")
        void shouldThrowUserNotActiveExceptionWhenUserIsSuspended() {
            // Arrange
            setupAuthentication(suspendedUser);
            when(userRepository.findByEmailIgnoreCase(suspendedUser.getEmail()))
                    .thenReturn(Optional.of(suspendedUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(suspendedUser.getEmail());

            // Verify interactions
            verify(userRepository).findByEmailIgnoreCase(suspendedUser.getEmail());
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is INACTIVE")
        void shouldThrowUserNotActiveExceptionWhenUserIsInactive() {
            // Arrange
            setupAuthentication(inactiveUser);
            when(userRepository.findByEmailIgnoreCase(inactiveUser.getEmail()))
                    .thenReturn(Optional.of(inactiveUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(inactiveUser.getEmail());

            // Verify interactions
            verify(userRepository).findByEmailIgnoreCase(inactiveUser.getEmail());
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is DELETED")
        void shouldThrowUserNotActiveExceptionWhenUserIsDeleted() {
            // Arrange
            setupAuthentication(deletedUser);
            when(userRepository.findByEmailIgnoreCase(deletedUser.getEmail()))
                    .thenReturn(Optional.of(deletedUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(deletedUser.getEmail());

            // Verify interactions
            verify(userRepository).findByEmailIgnoreCase(deletedUser.getEmail());
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw TeamNameAlreadyExistsException when team name already exists")
        void shouldThrowTeamNameAlreadyExistsExceptionWhenTeamNameExists() {
            // Arrange
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(teamCreateDto.name()))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(teamCreateDto))
                    .isInstanceOf(TeamNameAlreadyExistsException.class)
                    .hasMessageContaining(teamCreateDto.name());

            // Verify interactions
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verify(teamRepository).existsByNameIgnoreCase(teamCreateDto.name());
            verify(teamRepository, never()).save(any(Team.class));
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should verify team name check is case-insensitive")
        void shouldVerifyTeamNameCheckIsCaseInsensitive() {
            // Arrange
            TeamCreateDto upperCaseDto = new TeamCreateDto(
                    "DEVELOPMENT TEAM",
                    "Description"
            );
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(upperCaseDto.name()))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teamService.createTeam(upperCaseDto))
                    .isInstanceOf(TeamNameAlreadyExistsException.class);

            // Verify case-insensitive check was called
            verify(teamRepository).existsByNameIgnoreCase("DEVELOPMENT TEAM");
        }

        @Test
        @DisplayName("Should create team with null description")
        void shouldCreateTeamWithNullDescription() {
            // Arrange
            TeamCreateDto dtoWithNullDescription = new TeamCreateDto(
                    "New Team",
                    null
            );
            Team teamWithNullDescription = Team.builder()
                    .name("New Team")
                    .description(null)
                    .owner(activeUser)
                    .status(TeamStatus.ACTIVE)
                    .build();
            teamWithNullDescription.setId(200L);

            TeamResponseDto responseWithNullDescription = new TeamResponseDto(
                    200L,
                    "New Team",
                    null,
                    1L,
                    TeamStatus.ACTIVE,
                    Instant.now()
            );

            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(dtoWithNullDescription.name()))
                    .thenReturn(false);
            when(teamMapper.toEntity(dtoWithNullDescription)).thenReturn(teamWithNullDescription);
            when(teamRepository.save(any(Team.class))).thenReturn(teamWithNullDescription);
            when(teamMemberMapper.toOwnerEntity(any(Team.class), any(User.class))).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMapper.toDto(teamWithNullDescription)).thenReturn(responseWithNullDescription);

            // Act
            TeamResponseDto result = teamService.createTeam(dtoWithNullDescription);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.description()).isNull();
            assertThat(result.name()).isEqualTo("New Team");

            // Verify all operations completed
            verify(teamRepository).save(any(Team.class));
            verify(teamMemberRepository).save(any(TeamMember.class));
        }

        @Test
        @DisplayName("Should create team for user with different roles (ADMIN, MANAGER, MEMBER)")
        void shouldCreateTeamForUserWithDifferentRoles() {
            // Test for ADMIN role
            User adminUser = User.builder()
                    .email("admin@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            adminUser.setId(10L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.existsByNameIgnoreCase(teamCreateDto.name()))
                    .thenReturn(false);
            when(teamMapper.toEntity(teamCreateDto)).thenReturn(team);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMemberMapper.toOwnerEntity(any(Team.class), any(User.class))).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.createTeam(teamCreateDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.ownerId()).isEqualTo(1L); // Owner ID from team entity

            // Verify team was created
            verify(teamRepository).save(any(Team.class));
            verify(teamMemberRepository).save(any(TeamMember.class));
        }

        @Test
        @DisplayName("Should verify transactional behavior - all operations or none")
        void shouldVerifyTransactionalBehavior() {
            // Arrange
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(teamCreateDto.name()))
                    .thenReturn(false);
            when(teamMapper.toEntity(teamCreateDto)).thenReturn(team);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMemberMapper.toOwnerEntity(any(Team.class), any(User.class))).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.createTeam(teamCreateDto);

            // Assert - verify both team and team member were saved
            assertThat(result).isNotNull();
            verify(teamRepository).save(any(Team.class));
            verify(teamMemberRepository).save(any(TeamMember.class));

            // Verify team member was saved AFTER team (due to team ID requirement)
            var inOrder = inOrder(teamRepository, teamMemberRepository);
            inOrder.verify(teamRepository).save(any(Team.class));
            inOrder.verify(teamMemberRepository).save(any(TeamMember.class));
        }

        @Test
        @DisplayName("Should set owner ID correctly from current user")
        void shouldSetOwnerIdCorrectlyFromCurrentUser() {
            // Arrange
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(teamCreateDto.name()))
                    .thenReturn(false);
            when(teamMapper.toEntity(teamCreateDto)).thenReturn(team);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMemberMapper.toOwnerEntity(any(Team.class), any(User.class))).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            teamService.createTeam(teamCreateDto);

            // Assert - verify owner was set
            ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(teamCaptor.capture());
            Team capturedTeam = teamCaptor.getValue();
            assertThat(capturedTeam.getOwner()).isEqualTo(activeUser);
        }

        @Test
        @DisplayName("Should create team member with OWNER role")
        void shouldCreateTeamMemberWithOwnerRole() {
            // Arrange
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.existsByNameIgnoreCase(teamCreateDto.name()))
                    .thenReturn(false);
            when(teamMapper.toEntity(teamCreateDto)).thenReturn(team);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMemberMapper.toOwnerEntity(any(Team.class), any(User.class))).thenReturn(teamMember);
            when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            teamService.createTeam(teamCreateDto);

            // Assert - verify team member mapper was called with correct arguments
            verify(teamMemberMapper).toOwnerEntity(any(Team.class), eq(activeUser));
            verify(teamMemberRepository).save(any(TeamMember.class));
        }
    }

    @Nested
    @DisplayName("GetTeamById Tests")
    class GetTeamByIdTests {

        @Test
        @DisplayName("Should successfully retrieve team when all conditions are met")
        void shouldGetTeamByIdSuccessfully() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, activeUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.getTeamById(teamId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.name()).isEqualTo("Development Team");
            assertThat(result.status()).isEqualTo(TeamStatus.ACTIVE);

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, activeUser.getId());
            verify(teamMapper).toDto(team);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenTeamDoesNotExist() {
            // Arrange
            Long teamId = 999L;
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(TeamNotFoundException.class)
                    .hasMessage("Team with ID '" + teamId + "' not found");

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team status is INACTIVE")
        void shouldThrowTeamNotFoundExceptionWhenTeamIsInactive() {
            // Arrange
            Long teamId = 100L;
            Team inactiveTeam = Team.builder()
                    .name("Inactive Team")
                    .description("An inactive team")
                    .owner(activeUser)
                    .status(TeamStatus.INACTIVE)
                    .build();
            inactiveTeam.setId(teamId);

            when(teamRepository.findById(teamId)).thenReturn(Optional.of(inactiveTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(TeamNotFoundException.class)
                    .hasMessage("Team with ID '" + teamId + "' not found");

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team status is DELETED")
        void shouldThrowTeamNotFoundExceptionWhenTeamIsDeleted() {
            // Arrange
            Long teamId = 100L;
            Team deletedTeam = Team.builder()
                    .name("Deleted Team")
                    .description("A deleted team")
                    .owner(activeUser)
                    .status(TeamStatus.DELETED)
                    .build();
            deletedTeam.setId(teamId);

            when(teamRepository.findById(teamId)).thenReturn(Optional.of(deletedTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(TeamNotFoundException.class)
                    .hasMessage("Team with ID '" + teamId + "' not found");

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is null")
        void shouldThrowAccessDeniedExceptionWhenAuthenticationIsNull() {
            // Arrange
            Long teamId = 100L;
            clearAuthentication();
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            // Verify team was checked first
            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is anonymous")
        void shouldThrowAccessDeniedExceptionWhenUserIsAnonymous() {
            // Arrange
            Long teamId = 100L;
            setupAnonymousAuthentication();
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            // Verify team was checked first
            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when current user is not found")
        void shouldThrowUserNotFoundExceptionWhenCurrentUserNotFound() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with email 'Current user not found' not found");

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is SUSPENDED")
        void shouldThrowUserNotActiveExceptionWhenUserIsSuspended() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(suspendedUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(suspendedUser.getEmail()))
                    .thenReturn(Optional.of(suspendedUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(suspendedUser.getEmail());

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(suspendedUser.getEmail());
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is INACTIVE")
        void shouldThrowUserNotActiveExceptionWhenUserIsInactive() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(inactiveUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(inactiveUser.getEmail()))
                    .thenReturn(Optional.of(inactiveUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(inactiveUser.getEmail());

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(inactiveUser.getEmail());
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is DELETED")
        void shouldThrowUserNotActiveExceptionWhenUserIsDeleted() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(deletedUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(deletedUser.getEmail()))
                    .thenReturn(Optional.of(deletedUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(deletedUser.getEmail());

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(deletedUser.getEmail());
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not a member of the team")
        void shouldThrowAccessDeniedExceptionWhenUserIsNotMember() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, activeUser.getId()))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(teamId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Members can only access teams they are already within");

            // Verify interactions
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, activeUser.getId());
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should verify method execution order: team check -> auth -> user active -> membership check")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, activeUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            teamService.getTeamById(teamId);

            // Assert - verify execution order
            var inOrder = inOrder(teamRepository, userRepository, teamMemberRepository, teamMapper);
            inOrder.verify(teamRepository).findById(teamId);
            inOrder.verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            inOrder.verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, activeUser.getId());
            inOrder.verify(teamMapper).toDto(team);
        }

        @Test
        @DisplayName("Should allow team owner to access their team")
        void shouldAllowTeamOwnerToAccessTheirTeam() {
            // Arrange
            Long teamId = 100L;
            User ownerUser = User.builder()
                    .email("owner@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Owner")
                    .lastName("User")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            ownerUser.setId(1L);

            Team ownedTeam = Team.builder()
                    .name("Owned Team")
                    .description("Team owned by user")
                    .owner(ownerUser)
                    .status(TeamStatus.ACTIVE)
                    .build();
            ownedTeam.setId(teamId);

            TeamResponseDto ownerTeamResponse = new TeamResponseDto(
                    teamId,
                    "Owned Team",
                    "Team owned by user",
                    ownerUser.getId(),
                    TeamStatus.ACTIVE,
                    Instant.now()
            );

            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(ownedTeam));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(ownedTeam)).thenReturn(ownerTeamResponse);

            // Act
            TeamResponseDto result = teamService.getTeamById(teamId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.ownerId()).isEqualTo(ownerUser.getId());
            verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, ownerUser.getId());
        }

        @Test
        @DisplayName("Should allow regular team member to access team")
        void shouldAllowRegularTeamMemberToAccessTeam() {
            // Arrange
            Long teamId = 100L;
            User memberUser = User.builder()
                    .email("member@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Member")
                    .lastName("User")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            memberUser.setId(2L);

            setupAuthentication(memberUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(memberUser.getEmail()))
                    .thenReturn(Optional.of(memberUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.getTeamById(teamId);

            // Assert
            assertThat(result).isNotNull();
            verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, memberUser.getId());
        }

        @Test
        @DisplayName("Should throw NullPointerException when teamId is null")
        void shouldThrowNullPointerExceptionWhenTeamIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team Id can not be null");

            // Verify interactions - repository should never be called
            verifyNoInteractions(teamRepository);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should handle transactional read-only properly")
        void shouldHandleTransactionalReadOnly() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, activeUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.getTeamById(teamId);

            // Assert - verify no save operations were attempted
            assertThat(result).isNotNull();
            verify(teamRepository, never()).save(any(Team.class));
            verify(teamMemberRepository, never()).save(any(TeamMember.class));
        }
    }

    @Nested
    @DisplayName("GetAllTeamsForAdmin Tests")
    class GetAllTeamsForAdminTests {

        private User adminUser;
        private User regularUser;

        @BeforeEach
        void setUpAdminTests() {
            adminUser = User.builder()
                    .email("admin@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            adminUser.setId(100L);

            regularUser = User.builder()
                    .email("regular@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Regular")
                    .lastName("User")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            regularUser.setId(200L);
        }

        @Test
        @DisplayName("Should successfully retrieve all teams when admin requests")
        void shouldGetAllTeamsForAdminSuccessfully() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Team team1 = Team.builder()
                    .name("Team 1")
                    .description("First team")
                    .owner(activeUser)
                    .status(TeamStatus.ACTIVE)
                    .build();
            team1.setId(1L);

            Team team2 = Team.builder()
                    .name("Team 2")
                    .description("Second team")
                    .owner(adminUser)
                    .status(TeamStatus.ACTIVE)
                    .build();
            team2.setId(2L);

            Page<Team> teamPage = new PageImpl<>(List.of(team1, team2), pageable, 2L);

            TeamResponseDto response1 = new TeamResponseDto(
                    1L, "Team 1", "First team", 1L, TeamStatus.ACTIVE, Instant.now()
            );
            TeamResponseDto response2 = new TeamResponseDto(
                    2L, "Team 2", "Second team", 2L, TeamStatus.ACTIVE, Instant.now()
            );

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findAll(pageable)).thenReturn(teamPage);
            when(teamMapper.toDto(team1)).thenReturn(response1);
            when(teamMapper.toDto(team2)).thenReturn(response2);

            // Act
            Page<TeamResponseDto> result = teamService.getAllTeamsForAdmin(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).name()).isEqualTo("Team 1");
            assertThat(result.getContent().get(1).name()).isEqualTo("Team 2");

            verify(userRepository).findByEmailIgnoreCase(adminUser.getEmail());
            verify(teamRepository).findAll(pageable);
            verify(teamMapper, times(2)).toDto(any(Team.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-admin requests")
        void shouldThrowAccessDeniedExceptionWhenNonAdmin() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            setupAuthentication(regularUser);
            when(userRepository.findByEmailIgnoreCase(regularUser.getEmail()))
                    .thenReturn(Optional.of(regularUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getAllTeamsForAdmin(pageable))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only admins can access all teams");

            verify(userRepository).findByEmailIgnoreCase(regularUser.getEmail());
            verify(teamRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is missing")
        void shouldThrowAccessDeniedExceptionWhenAuthMissing() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            clearAuthentication();

            // Act & Assert
            assertThatThrownBy(() -> teamService.getAllTeamsForAdmin(pageable))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(teamRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when admin is not active")
        void shouldThrowUserNotActiveExceptionWhenAdminNotActive() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            adminUser.setStatus(UserStatus.SUSPENDED);
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getAllTeamsForAdmin(pageable))
                    .isInstanceOf(UserNotActiveException.class);

            verify(userRepository).findByEmailIgnoreCase(adminUser.getEmail());
            verify(teamRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no teams exist")
        void shouldReturnEmptyPageWhenNoTeamsExist() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Team> emptyPage = new PageImpl<>(List.of(), pageable, 0L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            Page<TeamResponseDto> result = teamService.getAllTeamsForAdmin(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(teamRepository).findAll(pageable);
            verify(teamMapper, never()).toDto(any(Team.class));
        }
    }

    @Nested
    @DisplayName("GetMyTeams Tests")
    class GetMyTeamsTests {

        @Test
        @DisplayName("Should successfully retrieve user's active teams")
        void shouldGetMyTeamsSuccessfully() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Team> teams = List.of(team);
            Page<Team> teamPage = new PageImpl<>(teams, pageable, teams.size());
            
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findActiveTeamsByUserId(activeUser.getId(), pageable))
                    .thenReturn(teamPage);
            when(teamMapper.toDto(any(Team.class))).thenReturn(teamResponseDto);

            // Act
            Page<TeamResponseDto> result = teamService.getMyTeams(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verify(teamRepository).findActiveTeamsByUserId(activeUser.getId(), pageable);
        }

        @Test
        @DisplayName("Should return empty page when user has no teams")
        void shouldReturnEmptyPageWhenNoTeams() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Team> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findActiveTeamsByUserId(activeUser.getId(), pageable))
                    .thenReturn(emptyPage);

            // Act
            Page<TeamResponseDto> result = teamService.getMyTeams(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(teamRepository).findActiveTeamsByUserId(activeUser.getId(), pageable);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is missing")
        void shouldThrowAccessDeniedExceptionWhenAuthMissing() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            clearAuthentication();

            // Act & Assert
            assertThatThrownBy(() -> teamService.getMyTeams(pageable))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            verify(teamRepository, never()).findActiveTeamsByUserId(any(), any());
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is not active")
        void shouldThrowUserNotActiveExceptionWhenUserNotActive() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            setupAuthentication(suspendedUser);
            when(userRepository.findByEmailIgnoreCase(suspendedUser.getEmail()))
                    .thenReturn(Optional.of(suspendedUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getMyTeams(pageable))
                    .isInstanceOf(UserNotActiveException.class);

            verify(teamRepository, never()).findActiveTeamsByUserId(any(), any());
        }
    }

    @Nested
    @DisplayName("GetTeamsByOwner Tests")
    class GetTeamsByOwnerTests {

        private User ownerUser;
        private User adminUser;
        private User otherUser;

        @BeforeEach
        void setUpOwnerTests() {
            ownerUser = User.builder()
                    .email("owner@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            ownerUser.setId(10L);

            adminUser = User.builder()
                    .email("admin@example.com")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            adminUser.setId(20L);

            otherUser = User.builder()
                    .email("other@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            otherUser.setId(30L);
        }

        @Test
        @DisplayName("Should retrieve owner's own active teams")
        void shouldGetOwnTeams() {
            // Arrange
            Long ownerId = ownerUser.getId();
            Pageable pageable = PageRequest.of(0, 10);
            List<Team> teams = List.of(team);
            Page<Team> teamPage = new PageImpl<>(teams, pageable, teams.size());
            
            setupAuthentication(ownerUser);
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamRepository.findByOwnerIdAndStatusActive(ownerId, pageable))
                    .thenReturn(teamPage);
            when(teamMapper.toDto(any(Team.class))).thenReturn(teamResponseDto);

            // Act
            Page<TeamResponseDto> result = teamService.getTeamsByOwner(ownerId, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(teamRepository).findByOwnerIdAndStatusActive(ownerId, pageable);
            verify(teamRepository, never()).findByOwnerId(any(), any());
        }

        @Test
        @DisplayName("Should retrieve all teams when admin requests any owner")
        void shouldGetAllTeamsWhenAdmin() {
            // Arrange
            Long targetOwnerId = 99L;
            Pageable pageable = PageRequest.of(0, 10);
            List<Team> teams = List.of(team);
            Page<Team> teamPage = new PageImpl<>(teams, pageable, teams.size());
            
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findByOwnerId(targetOwnerId, pageable))
                    .thenReturn(teamPage);
            when(teamMapper.toDto(any(Team.class))).thenReturn(teamResponseDto);

            // Act
            Page<TeamResponseDto> result = teamService.getTeamsByOwner(targetOwnerId, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository).findByOwnerId(targetOwnerId, pageable);
            verify(teamRepository, never()).findByOwnerIdAndStatusActive(any(), any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-admin tries to access other's teams")
        void shouldThrowAccessDeniedExceptionForOtherOwner() {
            // Arrange
            Long targetOwnerId = 99L;
            Pageable pageable = PageRequest.of(0, 10);
            
            setupAuthentication(otherUser);
            when(userRepository.findByEmailIgnoreCase(otherUser.getEmail()))
                    .thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamsByOwner(targetOwnerId, pageable))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only admins can access teams of other users");

            verify(teamRepository, never()).findByOwnerId(any(), any());
            verify(teamRepository, never()).findByOwnerIdAndStatusActive(any(), any());
        }

        @Test
        @DisplayName("Should throw NullPointerException when ownerId is null")
        void shouldThrowNullPointerExceptionWhenOwnerIdNull() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamsByOwner(null, pageable))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Owner ID can not be null");

            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(teamRepository, never()).findByOwnerId(any(), any());
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is not active")
        void shouldThrowUserNotActiveException() {
            // Arrange
            Long ownerId = ownerUser.getId();
            Pageable pageable = PageRequest.of(0, 10);
            ownerUser.setStatus(UserStatus.INACTIVE);
            
            setupAuthentication(ownerUser);
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamsByOwner(ownerId, pageable))
                    .isInstanceOf(UserNotActiveException.class);

            verify(teamRepository, never()).findByOwnerId(any(), any());
        }
    }

    @Nested
    @DisplayName("GetTeamByName Tests")
    class GetTeamByNameTests {

        @BeforeEach
        void setUpNameTests() {
            team.setId(100L);
        }

        @Test
        @DisplayName("Should successfully retrieve team by name")
        void shouldGetTeamByNameSuccessfully() {
            // Arrange
            String teamName = "Development Team";
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findByNameIgnoreCase(teamName))
                    .thenReturn(Optional.of(team));
            when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), activeUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.getTeamByName(teamName);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository).findByNameIgnoreCase(teamName);
            verify(teamMemberRepository).existsByTeamIdAndUserId(team.getId(), activeUser.getId());
        }

        @Test
        @DisplayName("Should trim team name before search")
        void shouldTrimTeamName() {
            // Arrange
            String teamNameWithSpaces = "  Development Team  ";
            String trimmedName = "Development Team";
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findByNameIgnoreCase(trimmedName))
                    .thenReturn(Optional.of(team));
            when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), activeUser.getId()))
                    .thenReturn(true);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            teamService.getTeamByName(teamNameWithSpaces);

            // Assert
            verify(teamRepository).findByNameIgnoreCase(trimmedName);
        }

        @Test
        @DisplayName("Should throw NullPointerException when team name is null")
        void shouldThrowNullPointerExceptionWhenNameNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamByName(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team name can not be null");

            verify(teamRepository, never()).findByNameIgnoreCase(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when team name is empty")
        void shouldThrowIllegalArgumentExceptionWhenNameEmpty() {
            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamByName("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Team name can not be empty");

            verify(teamRepository, never()).findByNameIgnoreCase(any());
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenNotExists() {
            // Arrange
            String teamName = "NonExistent Team";
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findByNameIgnoreCase(teamName))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamByName(teamName))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(teamRepository).findByNameIgnoreCase(teamName);
            verify(teamMemberRepository, never()).existsByTeamIdAndUserId(any(), any());
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team is not active")
        void shouldThrowTeamNotFoundExceptionWhenNotActive() {
            // Arrange
            String teamName = "Development Team";
            Team deletedTeam = Team.builder()
                    .name(teamName)
                    .description("A deleted team")
                    .owner(activeUser)
                    .status(TeamStatus.DELETED)
                    .build();
            deletedTeam.setId(100L);

            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findByNameIgnoreCase(teamName))
                    .thenReturn(Optional.of(deletedTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamByName(teamName))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(teamRepository).findByNameIgnoreCase(teamName);
            verify(teamMemberRepository, never()).existsByTeamIdAndUserId(any(), any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not a member")
        void shouldThrowAccessDeniedExceptionWhenNotMember() {
            // Arrange
            String teamName = "Development Team";
            setupAuthentication(activeUser);
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamRepository.findByNameIgnoreCase(teamName))
                    .thenReturn(Optional.of(team));
            when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), activeUser.getId()))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamByName(teamName))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Members can only access teams they are already within");

            verify(teamMemberRepository).existsByTeamIdAndUserId(team.getId(), activeUser.getId());
        }
    }

    @Nested
    @DisplayName("DeleteTeam Tests")
    class DeleteTeamTests {

        private User ownerUser;
        private User nonOwnerUser;

        @BeforeEach
        void setUpDeleteTests() {
            ownerUser = User.builder()
                    .email("owner@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            ownerUser.setId(10L);

            nonOwnerUser = User.builder()
                    .email("member@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            nonOwnerUser.setId(20L);

            team.setId(100L);
            team.setStatus(TeamStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should successfully delete team when owner deletes")
        void shouldDeleteTeamSuccessfully() {
            // Arrange
            Long teamId = team.getId();
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, ownerUser.getId(), TeamRole.OWNER))
                    .thenReturn(true);
            when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(teamMember));
            when(teamRepository.save(any(Team.class))).thenReturn(team);

            // Act
            teamService.deleteTeam(teamId);

            // Assert
            ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getStatus()).isEqualTo(TeamStatus.DELETED);
            verify(teamMemberRepository).findByTeamId(teamId);
        }

        @Test
        @DisplayName("Should throw NullPointerException when teamId is null")
        void shouldThrowNullPointerExceptionWhenTeamIdNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamService.deleteTeam(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team ID can not be null");

            verify(teamRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenNotExists() {
            // Arrange
            Long teamId = 999L;
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.deleteTeam(teamId))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(teamRepository).findById(teamId);
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team is not active")
        void shouldThrowTeamNotFoundExceptionWhenNotActive() {
            // Arrange
            Long teamId = 100L;
            Team deletedTeam = Team.builder()
                    .name("Deleted Team")
                    .description("A deleted team")
                    .owner(ownerUser)
                    .status(TeamStatus.DELETED)
                    .build();
            deletedTeam.setId(teamId);

            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(deletedTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.deleteTeam(teamId))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(teamRepository).findById(teamId);
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not owner")
        void shouldThrowAccessDeniedExceptionWhenNotOwner() {
            // Arrange
            Long teamId = team.getId();
            setupAuthentication(nonOwnerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(nonOwnerUser.getEmail()))
                    .thenReturn(Optional.of(nonOwnerUser));
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, nonOwnerUser.getId(), TeamRole.OWNER))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.deleteTeam(teamId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only team owners can delete the team");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is suspended")
        void shouldThrowUserNotActiveExceptionWhenSuspended() {
            // Arrange
            Long teamId = team.getId();
            ownerUser.setStatus(UserStatus.SUSPENDED);
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.deleteTeam(teamId))
                    .isInstanceOf(UserNotActiveException.class);

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update team members status to INACTIVE")
        void shouldUpdateMembersStatusToInactive() {
            // Arrange
            Long teamId = team.getId();
            TeamMember member = TeamMember.builder()
                    .team(team)
                    .user(ownerUser)
                    .role(TeamRole.OWNER)
                    .status(TeamMemberStatus.ACTIVE)
                    .build();
            member.setId(1L);

            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, ownerUser.getId(), TeamRole.OWNER))
                    .thenReturn(true);
            when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(member));
            when(teamRepository.save(any(Team.class))).thenReturn(team);

            // Act
            teamService.deleteTeam(teamId);

            // Assert
            verify(teamMemberRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("RestoreTeam Tests")
    class RestoreTeamTests {

        private User adminUser;
        private User regularUser;

        @BeforeEach
        void setUpRestoreTests() {
            adminUser = User.builder()
                    .email("admin@example.com")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            adminUser.setId(1L);

            regularUser = User.builder()
                    .email("regular@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            regularUser.setId(2L);

            team.setId(100L);
        }

        @Test
        @DisplayName("Should successfully restore deleted team when admin restores")
        void shouldRestoreDeletedTeamSuccessfully() {
            // Arrange
            Long teamId = team.getId();
            team.setStatus(TeamStatus.DELETED);
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(teamMember));
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(any(Team.class))).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.restoreTeam(teamId);

            // Assert
            assertThat(result).isNotNull();
            ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getStatus()).isEqualTo(TeamStatus.ACTIVE);
            verify(teamMemberRepository).findByTeamId(teamId);
        }

        @Test
        @DisplayName("Should successfully restore inactive team when admin restores")
        void shouldRestoreInactiveTeamSuccessfully() {
            // Arrange
            Long teamId = team.getId();
            team.setStatus(TeamStatus.INACTIVE);
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(teamMember));
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(any(Team.class))).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.restoreTeam(teamId);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when teamId is null")
        void shouldThrowNullPointerExceptionWhenTeamIdNull() {
            // Act & Assert
            assertThatThrownBy(() -> teamService.restoreTeam(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team ID can not be null");

            verify(teamRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-admin tries to restore")
        void shouldThrowAccessDeniedExceptionWhenNonAdmin() {
            // Arrange
            Long teamId = team.getId();
            setupAuthentication(regularUser);
            when(userRepository.findByEmailIgnoreCase(regularUser.getEmail()))
                    .thenReturn(Optional.of(regularUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.restoreTeam(teamId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only admins can restore deleted or inactive teams");

            verify(teamRepository, never()).findById(any());
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenNotExists() {
            // Arrange
            Long teamId = 999L;
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.restoreTeam(teamId))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(teamRepository).findById(teamId);
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw TeamAlreadyActive when team is already active")
        void shouldThrowTeamAlreadyActiveWhenAlreadyActive() {
            // Arrange
            Long teamId = team.getId();
            team.setStatus(TeamStatus.ACTIVE);
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // Act & Assert
            assertThatThrownBy(() -> teamService.restoreTeam(teamId))
                    .isInstanceOf(com.taskmanagement.common.exception.types.Exceptions.TeamAlreadyActive.class);

            verify(teamRepository).findById(teamId);
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when admin is not active")
        void shouldThrowUserNotActiveExceptionWhenAdminNotActive() {
            // Arrange
            Long teamId = team.getId();
            adminUser.setStatus(UserStatus.SUSPENDED);
            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.restoreTeam(teamId))
                    .isInstanceOf(UserNotActiveException.class);

            verify(teamRepository, never()).findById(any());
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update team members status to ACTIVE")
        void shouldUpdateMembersStatusToActive() {
            // Arrange
            Long teamId = team.getId();
            team.setStatus(TeamStatus.DELETED);

            User memberUser = User.builder()
                    .email("member@example.com")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            memberUser.setId(10L);

            TeamMember member = TeamMember.builder()
                    .team(team)
                    .user(memberUser)
                    .role(TeamRole.MEMBER)
                    .status(TeamMemberStatus.INACTIVE)
                    .build();
            member.setId(1L);

            setupAuthentication(adminUser);
            when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeamId(teamId)).thenReturn(List.of(member));
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(any(Team.class))).thenReturn(teamResponseDto);

            // Act
            teamService.restoreTeam(teamId);

            // Assert
            verify(teamMemberRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("UpdateTeam Tests")
    class UpdateTeamTests {

        private TeamUpdateDto teamUpdateDto;
        private User ownerUser;
        private User adminMemberUser;
        private User regularMemberUser;

        @BeforeEach
        void setUpUpdateTests() {
            teamUpdateDto = new TeamUpdateDto(
                    "Updated Team Name",
                    "Updated description",
                    null
            );

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

            // Setup admin member user
            adminMemberUser = User.builder()
                    .email("admin@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Admin")
                    .lastName("Member")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            adminMemberUser.setId(2L);

            // Setup regular member user
            regularMemberUser = User.builder()
                    .email("regular@example.com")
                    .passwordHash("$2a$10$hashedPassword")
                    .firstName("Regular")
                    .lastName("Member")
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            regularMemberUser.setId(3L);
        }

        @Test
        @DisplayName("Should successfully update team when owner updates name and description")
        void shouldUpdateTeamSuccessfullyAsOwner() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId))
                    .thenReturn(false);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.updateTeam(teamId, teamUpdateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(ownerUser.getEmail());
            verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, ownerUser.getId());
            verify(teamMemberRepository).existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN));
            verify(teamRepository).existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId);
            verify(teamRepository).save(any(Team.class));
            verify(teamMapper).toDto(team);
        }

        @Test
        @DisplayName("Should successfully update team when admin member updates")
        void shouldUpdateTeamSuccessfullyAsAdminMember() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(adminMemberUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(adminMemberUser.getEmail()))
                    .thenReturn(Optional.of(adminMemberUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, adminMemberUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, adminMemberUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId))
                    .thenReturn(false);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.updateTeam(teamId, teamUpdateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(teamMemberRepository).existsByTeamIdAndUserIdAndRoleIn(
                    teamId, adminMemberUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN));
        }

        @Test
        @DisplayName("Should throw NullPointerException when TeamUpdateDto is null")
        void shouldThrowNullPointerExceptionWhenTeamUpdateDtoIsNull() {
            // Arrange
            Long teamId = 100L;

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Team can not be null");

            verifyNoInteractions(teamRepository);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
            verifyNoInteractions(teamMapper);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team does not exist")
        void shouldThrowTeamNotFoundExceptionWhenTeamDoesNotExist() {
            // Arrange
            Long teamId = 999L;
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(TeamNotFoundException.class)
                    .hasMessage("Team with ID '" + teamId + "' not found");

            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team status is INACTIVE")
        void shouldThrowTeamNotFoundExceptionWhenTeamIsInactive() {
            // Arrange
            Long teamId = 100L;
            Team inactiveTeam = Team.builder()
                    .name("Inactive Team")
                    .owner(activeUser)
                    .status(TeamStatus.INACTIVE)
                    .build();
            inactiveTeam.setId(teamId);

            when(teamRepository.findById(teamId)).thenReturn(Optional.of(inactiveTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(TeamNotFoundException.class)
                    .hasMessage("Team with ID '" + teamId + "' not found");

            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when authentication is null")
        void shouldThrowAccessDeniedExceptionWhenAuthenticationIsNull() {
            // Arrange
            Long teamId = 100L;
            clearAuthentication();
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Authentication required");

            verify(teamRepository).findById(teamId);
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when current user is not found")
        void shouldThrowUserNotFoundExceptionWhenCurrentUserNotFound() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with email 'Current user not found' not found");

            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verifyNoInteractions(teamMemberRepository);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user status is SUSPENDED")
        void shouldThrowUserNotActiveExceptionWhenUserIsSuspended() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(suspendedUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(suspendedUser.getEmail()))
                    .thenReturn(Optional.of(suspendedUser));

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(suspendedUser.getEmail());

            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(suspendedUser.getEmail());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not a member of the team")
        void shouldThrowAccessDeniedExceptionWhenUserIsNotMember() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(activeUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(activeUser.getEmail()))
                    .thenReturn(Optional.of(activeUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, activeUser.getId()))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Members can only access teams they are already within");

            verify(teamRepository).findById(teamId);
            verify(userRepository).findByEmailIgnoreCase(activeUser.getEmail());
            verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, activeUser.getId());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when regular member tries to update")
        void shouldThrowAccessDeniedExceptionWhenRegularMemberTriesToUpdate() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(regularMemberUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(regularMemberUser.getEmail()))
                    .thenReturn(Optional.of(regularMemberUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, regularMemberUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, regularMemberUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only team owners or admins can perform this action");

            verify(teamMemberRepository).existsByTeamIdAndUserIdAndRoleIn(
                    teamId, regularMemberUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN));
        }

        @Test
        @DisplayName("Should throw TeamNameAlreadyExistsException when updated name already exists")
        void shouldThrowTeamNameAlreadyExistsExceptionWhenNameExists() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, teamUpdateDto))
                    .isInstanceOf(TeamNameAlreadyExistsException.class)
                    .hasMessageContaining("Updated Team Name");

            verify(teamRepository).existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId);
            verify(teamRepository, never()).save(any(Team.class));
        }

        @Test
        @DisplayName("Should update only name when description is null")
        void shouldUpdateOnlyNameWhenDescriptionIsNull() {
            // Arrange
            Long teamId = 100L;
            TeamUpdateDto updateOnlyName = new TeamUpdateDto("New Name", null, null);
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.existsByNameIgnoreCaseAndIdNot("New Name", teamId))
                    .thenReturn(false);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.updateTeam(teamId, updateOnlyName);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository).existsByNameIgnoreCaseAndIdNot("New Name", teamId);
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should update only description when name is null")
        void shouldUpdateOnlyDescriptionWhenNameIsNull() {
            // Arrange
            Long teamId = 100L;
            TeamUpdateDto updateOnlyDescription = new TeamUpdateDto(null, "New Description", null);
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.updateTeam(teamId, updateOnlyDescription);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository, never()).existsByNameIgnoreCaseAndIdNot(anyString(), anyLong());
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should not update name when name is blank")
        void shouldNotUpdateNameWhenNameIsBlank() {
            // Arrange
            Long teamId = 100L;
            TeamUpdateDto updateWithBlankName = new TeamUpdateDto("   ", "New Description", null);
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.updateTeam(teamId, updateWithBlankName);

            // Assert
            assertThat(result).isNotNull();
            verify(teamRepository, never()).existsByNameIgnoreCaseAndIdNot(anyString(), anyLong());
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should update team status when owner changes it")
        void shouldUpdateTeamStatusWhenOwnerChangesIt() {
            // Arrange
            Long teamId = 100L;
            TeamUpdateDto updateStatus = new TeamUpdateDto(null, null, TeamStatus.INACTIVE);
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, ownerUser.getId(), TeamRole.OWNER))
                    .thenReturn(true);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            TeamResponseDto result = teamService.updateTeam(teamId, updateStatus);

            // Assert
            assertThat(result).isNotNull();
            verify(teamMemberRepository).existsByTeamIdAndUserIdAndRole(teamId, ownerUser.getId(), TeamRole.OWNER);
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when admin tries to change status")
        void shouldThrowAccessDeniedExceptionWhenAdminTriesToChangeStatus() {
            // Arrange
            Long teamId = 100L;
            TeamUpdateDto updateStatus = new TeamUpdateDto(null, null, TeamStatus.INACTIVE);
            setupAuthentication(adminMemberUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(adminMemberUser.getEmail()))
                    .thenReturn(Optional.of(adminMemberUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, adminMemberUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, adminMemberUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, adminMemberUser.getId(), TeamRole.OWNER))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(teamId, updateStatus))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only team owners can change the team status");

            verify(teamMemberRepository).existsByTeamIdAndUserIdAndRole(teamId, adminMemberUser.getId(), TeamRole.OWNER);
            verify(teamRepository, never()).save(any(Team.class));
        }

        @Test
        @DisplayName("Should verify method execution order")
        void shouldVerifyMethodExecutionOrder() {
            // Arrange
            Long teamId = 100L;
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            when(teamRepository.existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId))
                    .thenReturn(false);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            teamService.updateTeam(teamId, teamUpdateDto);

            // Assert - verify execution order
            var inOrder = inOrder(teamRepository, userRepository, teamMemberRepository);
            inOrder.verify(teamRepository).findById(teamId);
            inOrder.verify(userRepository).findByEmailIgnoreCase(ownerUser.getEmail());
            inOrder.verify(teamMemberRepository).existsByTeamIdAndUserId(teamId, ownerUser.getId());
            inOrder.verify(teamMemberRepository).existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN));
            inOrder.verify(teamRepository).existsByNameIgnoreCaseAndIdNot("Updated Team Name", teamId);
            inOrder.verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should check uniqueness with untrimmed name but save trimmed values")
        void shouldCheckUniquenessWithUntrimmedNameButSaveTrimmedValues() {
            // Arrange
            Long teamId = 100L;
            TeamUpdateDto updateWithSpaces = new TeamUpdateDto("  New Team Name  ", "  New Description  ", null);
            setupAuthentication(ownerUser);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(userRepository.findByEmailIgnoreCase(ownerUser.getEmail()))
                    .thenReturn(Optional.of(ownerUser));
            when(teamMemberRepository.existsByTeamIdAndUserId(teamId, ownerUser.getId()))
                    .thenReturn(true);
            when(teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                    teamId, ownerUser.getId(), List.of(TeamRole.OWNER, TeamRole.ADMIN)))
                    .thenReturn(true);
            // Implementation checks uniqueness with the untrimmed name from DTO
            when(teamRepository.existsByNameIgnoreCaseAndIdNot("  New Team Name  ", teamId))
                    .thenReturn(false);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMapper.toDto(team)).thenReturn(teamResponseDto);

            // Act
            teamService.updateTeam(teamId, updateWithSpaces);

            // Assert - verify uniqueness check uses untrimmed name
            verify(teamRepository).existsByNameIgnoreCaseAndIdNot("  New Team Name  ", teamId);

            // Verify team was saved with trimmed values
            ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getName()).isEqualTo("New Team Name");
            assertThat(savedTeam.getDescription()).isEqualTo("New Description");
        }
    }
}


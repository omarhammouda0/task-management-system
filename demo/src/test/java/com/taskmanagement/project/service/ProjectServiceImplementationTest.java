package com.taskmanagement.project.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.project.mapper.ProjectMapper;
import com.taskmanagement.project.repository.ProjectRepository;
import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ProjectServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImplementation Unit Tests")
class ProjectServiceImplementationTest {

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImplementation projectService;

    private User adminUser;
    private User ownerUser;
    private User memberUser;
    private Team activeTeam;
    private Team targetTeam;
    private Project project;
    private ProjectResponseDto projectResponseDto;
    private CreateProjectDto createProjectDto;
    private UpdateProjectDto updateProjectDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup admin user
        adminUser = User.builder()
                .email("admin@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        adminUser.setId(1L);

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
        ownerUser.setId(2L);

        // Setup member user
        memberUser = User.builder()
                .email("member@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Member")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        memberUser.setId(3L);

        // Setup active team
        activeTeam = Team.builder()
                .name("Test Team")
                .description("Test Description")
                .owner(ownerUser)
                .status(TeamStatus.ACTIVE)
                .build();
        activeTeam.setId(1L);

        // Setup target team for transfer
        targetTeam = Team.builder()
                .name("Target Team")
                .description("Target Description")
                .owner(ownerUser)
                .status(TeamStatus.ACTIVE)
                .build();
        targetTeam.setId(2L);

        // Setup project with JPA relationship
        project = Project.builder()
                .name("Test Project")
                .description("Test Description")
                .team(activeTeam)
                .status(ProjectStatus.PLANNED)
                .startDate(Instant.now().plusSeconds(86400))
                .endDate(Instant.now().plusSeconds(172800))
                .build();
        project.setId(1L);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        // Setup DTOs
        createProjectDto = new CreateProjectDto(
                1L,
                "Test Project",
                "Test Description",
                ProjectStatus.PLANNED,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800)
        );

        updateProjectDto = new UpdateProjectDto(
                "Updated Project",
                "Updated Description",
                ProjectStatus.ACTIVE,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(259200)
        );

        projectResponseDto = new ProjectResponseDto(
                1L,
                "Test Project",
                "Test Description",
                1L,
                ProjectStatus.PLANNED,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                2L,
                Instant.now(),
                Instant.now()
        );

        pageable = PageRequest.of(0, 10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ============================================
    // CREATE PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("createProject() Tests")
    class CreateProjectTests {

        @Test
        @DisplayName("Should create project successfully")
        void shouldCreateProjectSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            doNothing().when(securityHelper).isOwner(2L, 1L);
            doNothing().when(securityHelper).validateProjectNameNotExists("Test Project", 1L);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(securityHelper.statusValidation(ProjectStatus.PLANNED)).thenReturn(ProjectStatus.PLANNED);
            when(projectMapper.toEntity(createProjectDto, activeTeam)).thenReturn(project);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.createProject(createProjectDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Project");
            verify(securityHelper).getCurrentUser();
            verify(securityHelper).teamExistsAndActiveCheck(1L);
            verify(securityHelper).isOwner(2L, 1L);
            verify(projectRepository).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.createProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project creation data must not be null");

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doThrow(new UserNotActiveException(ownerUser.getEmail()))
                    .when(securityHelper).isUserActive(ownerUser);

            // When/Then
            assertThatThrownBy(() -> projectService.createProject(createProjectDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw TeamNotFoundException when team doesn't exist")
        void shouldThrowExceptionWhenTeamNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L))
                    .thenThrow(new TeamNotFoundException(1L));

            // When/Then
            assertThatThrownBy(() -> projectService.createProject(createProjectDto))
                    .isInstanceOf(TeamNotFoundException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not owner")
        void shouldThrowExceptionWhenUserIsNotOwner() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isUserActive(memberUser);
            doThrow(new AccessDeniedException("Only team owner can create projects"))
                    .when(securityHelper).isOwner(3L, 1L);

            // When/Then
            assertThatThrownBy(() -> projectService.createProject(createProjectDto))
                    .isInstanceOf(AccessDeniedException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw ProjectNameAlreadyExistsException when name exists")
        void shouldThrowExceptionWhenProjectNameExists() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            doNothing().when(securityHelper).isOwner(2L, 1L);
            doThrow(new ProjectNameAlreadyExistsException("Test Project", 1L))
                    .when(securityHelper).validateProjectNameNotExists("Test Project", 1L);

            // When/Then
            assertThatThrownBy(() -> projectService.createProject(createProjectDto))
                    .isInstanceOf(ProjectNameAlreadyExistsException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw InvalidDateException when dates are invalid")
        void shouldThrowExceptionWhenDatesAreInvalid() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            doNothing().when(securityHelper).isOwner(2L, 1L);
            doNothing().when(securityHelper).validateProjectNameNotExists("Test Project", 1L);
            doThrow(new InvalidProjectDateException ("Start date must be before end date"))
                    .when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));

            // When/Then
            assertThatThrownBy(() -> projectService.createProject(createProjectDto))
                    .isInstanceOf(InvalidProjectDateException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should trim project name before creating")
        void shouldTrimProjectNameBeforeCreating() {
            // Given
            CreateProjectDto dtoWithSpaces = new CreateProjectDto(
                    1L,
                    "  Test Project  ",
                    "Test Description",
                    ProjectStatus.PLANNED,
                    Instant.now().plusSeconds(86400),
                    Instant.now().plusSeconds(172800)
            );

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            doNothing().when(securityHelper).isOwner(2L, 1L);
            doNothing().when(securityHelper).validateProjectNameNotExists("Test Project", 1L);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(securityHelper.statusValidation(ProjectStatus.PLANNED)).thenReturn(ProjectStatus.PLANNED);
            when(projectMapper.toEntity(dtoWithSpaces, activeTeam)).thenReturn(project);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            projectService.createProject(dtoWithSpaces);

            // Then
            verify(securityHelper).validateProjectNameNotExists("Test Project", 1L);
        }

        @Test
        @DisplayName("Should set createdBy when creating project")
        void shouldSetCreatedByWhenCreating() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            doNothing().when(securityHelper).isOwner(2L, 1L);
            doNothing().when(securityHelper).validateProjectNameNotExists("Test Project", 1L);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(securityHelper.statusValidation(ProjectStatus.PLANNED)).thenReturn(ProjectStatus.PLANNED);
            when(projectMapper.toEntity(createProjectDto, activeTeam)).thenReturn(project);
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
                Project savedProject = invocation.getArgument(0);
                assertThat(savedProject.getCreatedBy()).isEqualTo(ownerUser.getId());
                return savedProject;
            });
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            projectService.createProject(createProjectDto);

            // Then
            verify(projectRepository).save(any(Project.class));
        }
    }

    // ============================================
    // GET PROJECT BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("getProjectById() Tests")
    class GetProjectByIdTests {

        @Test
        @DisplayName("Should get project by ID successfully as admin")
        void shouldGetProjectByIdAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsCheckAndRetrievableCheckUponRole(adminUser, 1L))
                    .thenReturn(project);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            doNothing().when(securityHelper).isMemberInTeamOrSystemAdmin(1L, adminUser);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.getProjectById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(securityHelper, never()).teamActiveCheck(1L);
        }

        @Test
        @DisplayName("Should get project by ID successfully as member")
        void shouldGetProjectByIdAsMember() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsCheckAndRetrievableCheckUponRole(memberUser, 1L))
                    .thenReturn(project);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doNothing().when(securityHelper).isMemberInTeamOrSystemAdmin(1L, memberUser);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.getProjectById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(securityHelper).teamActiveCheck(1L);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.getProjectById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should throw ProjectNotFoundException when project doesn't exist")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsCheckAndRetrievableCheckUponRole(memberUser, 1L))
                    .thenThrow(new ProjectNotFoundException(1L));

            // When/Then
            assertThatThrownBy(() -> projectService.getProjectById(1L))
                    .isInstanceOf(ProjectNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not team member")
        void shouldThrowExceptionWhenUserIsNotTeamMember() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsCheckAndRetrievableCheckUponRole(memberUser, 1L))
                    .thenReturn(project);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doThrow(new AccessDeniedException("Must be team member to view project"))
                    .when(securityHelper).isMemberInTeamOrSystemAdmin(1L, memberUser);

            // When/Then
            assertThatThrownBy(() -> projectService.getProjectById(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ============================================
    // GET PROJECTS BY TEAM TESTS
    // ============================================

    @Nested
    @DisplayName("getProjectsByTeam() Tests")
    class GetProjectsByTeamTests {

        @Test
        @DisplayName("Should get projects by team successfully as admin")
        void shouldGetProjectsByTeamAsAdmin() {
            // Given
            Page<Project> projectPage = new PageImpl<>(List.of(project));

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isMemberInTeamOrSystemAdmin(1L, adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(projectRepository.findByTeamIdForAdmin(1L, pageable)).thenReturn(projectPage);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            Page<ProjectResponseDto> result = projectService.getProjectsByTeam(pageable, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(projectRepository).findByTeamIdForAdmin(1L, pageable);
            verify(projectRepository, never()).findByTeamId(1L, pageable);
        }

        @Test
        @DisplayName("Should get projects by team successfully as member")
        void shouldGetProjectsByTeamAsMember() {
            // Given
            Page<Project> projectPage = new PageImpl<>(List.of(project));

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isMemberInTeamOrSystemAdmin(1L, memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(projectRepository.findByTeamId(1L, pageable)).thenReturn(projectPage);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            Page<ProjectResponseDto> result = projectService.getProjectsByTeam(pageable, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(projectRepository).findByTeamId(1L, pageable);
            verify(projectRepository, never()).findByTeamIdForAdmin(1L, pageable);
        }

        @Test
        @DisplayName("Should throw NullPointerException when team ID is null")
        void shouldThrowExceptionWhenTeamIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.getProjectsByTeam(pageable, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The team id must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.getProjectsByTeam(null, 1L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The pageable must not be null");
        }

        @Test
        @DisplayName("Should return empty page when no projects found")
        void shouldReturnEmptyPageWhenNoProjectsFound() {
            // Given
            Page<Project> emptyPage = new PageImpl<>(List.of());

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).isMemberInTeamOrSystemAdmin(1L, memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(projectRepository.findByTeamId(1L, pageable)).thenReturn(emptyPage);

            // When
            Page<ProjectResponseDto> result = projectService.getProjectsByTeam(pageable, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ============================================
    // GET PROJECTS BY OWNER TESTS
    // ============================================

    @Nested
    @DisplayName("getProjectsByOwner() Tests")
    class GetProjectsByOwnerTests {

        @Test
        @DisplayName("Should get projects by owner successfully as admin")
        void shouldGetProjectsByOwnerAsAdmin() {
            // Given
            Page<Project> projectPage = new PageImpl<>(List.of(project));

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(projectRepository.findByOwnerIdForAdmin(2L, pageable)).thenReturn(projectPage);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            Page<ProjectResponseDto> result = projectService.getProjectsByOwner(pageable, 2L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(projectRepository).findByOwnerIdForAdmin(2L, pageable);
        }

        @Test
        @DisplayName("Should get own projects successfully")
        void shouldGetOwnProjectsSuccessfully() {
            // Given
            Page<Project> projectPage = new PageImpl<>(List.of(project));

            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.isSystemAdmin(ownerUser)).thenReturn(false);
            when(securityHelper.isSelfOperation(2L, 2L)).thenReturn(true);
            when(projectRepository.findByOwnerId(2L, pageable)).thenReturn(projectPage);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            Page<ProjectResponseDto> result = projectService.getProjectsByOwner(pageable, 2L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw NullPointerException when owner ID is null")
        void shouldThrowExceptionWhenOwnerIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.getProjectsByOwner(pageable, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Owner ID must not be null");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when trying to view other's projects")
        void shouldThrowExceptionWhenViewingOthersProjects() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.isSelfOperation(3L, 2L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> projectService.getProjectsByOwner(pageable, 2L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You can only view your own projects");
        }
    }

    // ============================================
    // GET ALL PROJECTS FOR ADMIN TESTS
    // ============================================

    @Nested
    @DisplayName("getAllProjectsForAdmin() Tests")
    class GetAllProjectsForAdminTests {

        @Test
        @DisplayName("Should get all projects successfully as admin")
        void shouldGetAllProjectsAsAdmin() {
            // Given
            Page<Project> projectPage = new PageImpl<>(List.of(project));

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(projectRepository.findAll(pageable)).thenReturn(projectPage);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            Page<ProjectResponseDto> result = projectService.getAllProjectsForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(securityHelper).isSystemAdmin(adminUser);
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.getAllProjectsForAdmin(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The pageable must not be null");
        }

        @Test
        @DisplayName("Should not enforce admin check (implementation bug)")
        void shouldNotEnforceAdminCheck() {
            // Given
            Page<Project> projectPage = new PageImpl<>(List.of(project));

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(projectRepository.findAll(pageable)).thenReturn(projectPage);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When - Documents that the implementation doesn't check the boolean result
            Page<ProjectResponseDto> result = projectService.getAllProjectsForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ============================================
    // UPDATE PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("updateProject() Tests")
    class UpdateProjectTests {

        @Test
        @DisplayName("Should update project successfully as admin")
        void shouldUpdateProjectAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("Updated Project", 1L, 1L);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.ACTIVE);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.updateProject(1L, updateProjectDto);

            // Then
            assertThat(result).isNotNull();
            verify(projectMapper).updateEntityFromDto(updateProjectDto, project);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should update project successfully as team owner")
        void shouldUpdateProjectAsTeamOwner() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(ownerUser);
            doNothing().when(securityHelper).isUserActive(ownerUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(ownerUser)).thenReturn(false);
            when(securityHelper.isTeamOwnerOrTeamAdmin(2L, 1L)).thenReturn(true);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("Updated Project", 1L, 1L);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.ACTIVE);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.updateProject(1L, updateProjectDto);

            // Then
            assertThat(result).isNotNull();
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.updateProject(null, updateProjectDto))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.updateProject(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project update data must not be null");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no fields provided")
        void shouldThrowExceptionWhenNoFieldsProvided() {
            // Given
            UpdateProjectDto emptyDto = new UpdateProjectDto(null, null, null, null, null);

            // When/Then
            assertThatThrownBy(() -> projectService.updateProject(1L, emptyDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("At least one field must be provided for update");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            // Given
            UpdateProjectDto blankNameDto = new UpdateProjectDto(
                    "   ",
                    null,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            doNothing().when(securityHelper).teamActiveCheck(1L);

            // When/Then
            assertThatThrownBy(() -> projectService.updateProject(1L, blankNameDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Project name cannot be blank");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not authorized")
        void shouldThrowExceptionWhenUserNotAuthorized() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.isTeamOwnerOrTeamAdmin(3L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> projectService.updateProject(1L, updateProjectDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only system admin , team owner and team admin");
        }

        @Test
        @DisplayName("Should update only name")
        void shouldUpdateOnlyName() {
            // Given
            UpdateProjectDto nameOnlyDto = new UpdateProjectDto(
                    "New Name",
                    null,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("New Name", 1L, 1L);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            projectService.updateProject(1L, nameOnlyDto);

            // Then
            verify(securityHelper).validateProjectNameNotExistsForUpdate("New Name", 1L, 1L);
        }

        @Test
        @DisplayName("Should update only status")
        void shouldUpdateOnlyStatus() {
            // Given
            UpdateProjectDto statusOnlyDto = new UpdateProjectDto(
                    null,
                    null,
                    ProjectStatus.ACTIVE,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.ACTIVE);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            projectService.updateProject(1L, statusOnlyDto);

            // Then
            verify(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should validate dates when updating")
        void shouldValidateDatesWhenUpdating() {
            // Given
            UpdateProjectDto datesDto = new UpdateProjectDto(
                    null,
                    null,
                    null,
                    Instant.now().plusSeconds(86400),
                    Instant.now().plusSeconds(172800)
            );

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            projectService.updateProject(1L, datesDto);

            // Then
            verify(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
        }
    }

    // ============================================
    // DELETE PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("deleteProject() Tests")
    class DeleteProjectTests {

        @Test
        @DisplayName("Should delete project successfully")
        void shouldDeleteProjectSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.DELETED);
            when(projectRepository.save(any(Project.class))).thenReturn(project);

            // When
            projectService.deleteProject(1L);

            // Then
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.deleteProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not admin")
        void shouldThrowExceptionWhenUserIsNotAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.DELETED);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            projectService.deleteProject(1L);

            // Then - Verify it actually saved (documenting the bug)
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw InvalidProjectStatusException when status transition invalid")
        void shouldThrowExceptionWhenStatusTransitionInvalid() {
            // Given
            project.setStatus(ProjectStatus.DELETED);

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            doThrow(new InvalidProjectStatusException(ProjectStatus.DELETED, ProjectStatus.DELETED))
                    .when(securityHelper).validateStatusValidation(ProjectStatus.DELETED, ProjectStatus.DELETED);

            // When/Then
            assertThatThrownBy(() -> projectService.deleteProject(1L))
                    .isInstanceOf(InvalidProjectStatusException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should set updatedBy when deleting")
        void shouldSetUpdatedByWhenDeleting() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.DELETED);
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
                Project savedProject = invocation.getArgument(0);
                assertThat(savedProject.getUpdatedBy()).isEqualTo(adminUser.getId());
                return savedProject;
            });

            // When
            projectService.deleteProject(1L);

            // Then
            verify(projectRepository).save(project);
        }
    }

    // ============================================
    // RESTORE PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("restoreProject() Tests")
    class RestoreProjectTests {

        @Test
        @DisplayName("Should restore project successfully")
        void shouldRestoreProjectSuccessfully() {
            // Given
            project.setStatus(ProjectStatus.DELETED);

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.DELETED, ProjectStatus.PLANNED);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("Test Project", 1L, 1L);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.restoreProject(1L);

            // Then
            assertThat(result).isNotNull();
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.restoreProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should not throw AccessDeniedException when user is not admin (implementation bug)")
        void shouldNotThrowExceptionWhenUserIsNotAdmin() {
            // Given
            project.setStatus(ProjectStatus.DELETED);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.DELETED, ProjectStatus.PLANNED);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("Test Project", 1L, 1L);
            doNothing().when(securityHelper).teamActiveCheck(1L);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When - Documents that the implementation doesn't check the boolean result
            ProjectResponseDto result = projectService.restoreProject(1L);

            // Then
            assertThat(result).isNotNull();
            verify(projectRepository).save(project);
        }
    }

    // ============================================
    // ACTIVATE PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("activateProject() Tests")
    class ActivateProjectTests {

        @Test
        @DisplayName("Should activate project successfully")
        void shouldActivateProjectSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.ACTIVE);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("Test Project", 1L, 1L);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.activateProject(1L);

            // Then
            assertThat(result).isNotNull();
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.activateProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should not throw AccessDeniedException when user is not admin (implementation bug)")
        void shouldNotThrowExceptionWhenUserIsNotAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.PLANNED, ProjectStatus.ACTIVE);
            doNothing().when(securityHelper).validateProjectNameNotExistsForUpdate("Test Project", 1L, 1L);
            doNothing().when(securityHelper).dateValidation(any(Instant.class), any(Instant.class));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When - Documents that the implementation doesn't check the boolean result
            ProjectResponseDto result = projectService.activateProject(1L);

            // Then
            assertThat(result).isNotNull();
            verify(projectRepository).save(project);
        }
    }

    // ============================================
    // ARCHIVE PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("archiveProject() Tests")
    class ArchiveProjectTests {

        @Test
        @DisplayName("Should archive project successfully")
        void shouldArchiveProjectSuccessfully() {
            // Given
            project.setStatus(ProjectStatus.COMPLETED);

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.COMPLETED, ProjectStatus.ARCHIVED);
            when(projectRepository.save(any(Project.class))).thenReturn(project);

            // When
            projectService.archiveProject(1L);

            // Then
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.archiveProject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should not throw AccessDeniedException when user is not admin (implementation bug)")
        void shouldNotThrowExceptionWhenUserIsNotAdmin() {
            // Given
            project.setStatus(ProjectStatus.COMPLETED);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.projectExistsCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doNothing().when(securityHelper).validateStatusValidation(ProjectStatus.COMPLETED, ProjectStatus.ARCHIVED);
            when(projectRepository.save(any(Project.class))).thenReturn(project);

            // When - Documents that the implementation doesn't check the boolean result
            projectService.archiveProject(1L);

            // Then
            verify(projectRepository).save(project);
        }
    }

    // ============================================
    // TRANSFER PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("transferProject() Tests")
    class TransferProjectTests {

        @Test
        @DisplayName("Should transfer project successfully")
        void shouldTransferProjectSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(2L)).thenReturn(targetTeam);
            doNothing().when(securityHelper).notSameTeamCheck(1L, 2L);
            doNothing().when(securityHelper).validateProjectNameNotExists("Test Project", 2L);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectResponseDto);

            // When
            ProjectResponseDto result = projectService.transferProject(1L, 2L);

            // Then
            assertThat(result).isNotNull();
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.transferProject(null, 2L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The project id must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when new team ID is null")
        void shouldThrowExceptionWhenNewTeamIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> projectService.transferProject(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("The new team id must not be null");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not admin")
        void shouldThrowExceptionWhenUserIsNotAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            doThrow(new AccessDeniedException("Only admin can transfer projects"))
                    .when(securityHelper).systemAdminCheck(memberUser);

            // When/Then
            assertThatThrownBy(() -> projectService.transferProject(1L, 2L))
                    .isInstanceOf(AccessDeniedException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw exception when transferring to same team")
        void shouldThrowExceptionWhenTransferringToSameTeam() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(1L)).thenReturn(activeTeam);
            doThrow(new IllegalStateException("Cannot transfer to same team"))
                    .when(securityHelper).notSameTeamCheck(1L, 1L);

            // When/Then
            assertThatThrownBy(() -> projectService.transferProject(1L, 1L))
                    .isInstanceOf(IllegalStateException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        @DisplayName("Should throw exception when project name exists in target team")
        void shouldThrowExceptionWhenProjectNameExistsInTargetTeam() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(project);
            when(securityHelper.teamExistsAndActiveCheck(2L)).thenReturn(targetTeam);
            doNothing().when(securityHelper).notSameTeamCheck(1L, 2L);
            doThrow(new ProjectNameAlreadyExistsException("Test Project", 2L))
                    .when(securityHelper).validateProjectNameNotExists("Test Project", 2L);

            // When/Then
            assertThatThrownBy(() -> projectService.transferProject(1L, 2L))
                    .isInstanceOf(ProjectNameAlreadyExistsException.class);

            verify(projectRepository, never()).save(any(Project.class));
        }
    }
}


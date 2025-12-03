package com.taskmanagement.task.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.task.dto.AssignTaskDto;
import com.taskmanagement.task.dto.CreateTaskDto;
import com.taskmanagement.task.dto.TaskResponseDto;
import com.taskmanagement.task.dto.UpdateTaskDto;
import com.taskmanagement.task.entity.Task;
import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import com.taskmanagement.task.mapper.TaskMapper;
import com.taskmanagement.task.repository.TaskRepository;
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
 * Comprehensive unit tests for TaskServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImplementation Unit Tests")
class TaskServiceImplementationTest {

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImplementation taskService;

    private User adminUser;
    private User memberUser;
    private User assigneeUser;
    private Project activeProject;
    private Task task;
    private TaskResponseDto taskResponseDto;
    private CreateTaskDto createTaskDto;
    private UpdateTaskDto updateTaskDto;
    private AssignTaskDto assignTaskDto;
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
        memberUser.setId(2L);

        // Setup assignee user
        assigneeUser = User.builder()
                .email("assignee@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Assignee")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        assigneeUser.setId(3L);

        // Setup active project
        activeProject = Project.builder()
                .name("Test Project")
                .description("Test Description")
                .teamId(1L)
                .status(ProjectStatus.ACTIVE)
                .build();
        activeProject.setId(1L);

        // Setup task
        task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MEDIUM)
                .projectId(1L)
                .assignedTo(null)
                .dueDate(Instant.now().plusSeconds(86400))
                .build();
        task.setId(1L);
        task.setCreatedBy(1L);

        // Setup DTOs
        createTaskDto = new CreateTaskDto(
                "Test Task",
                "Test Description",
                1L,
                TaskPriority.HIGH,
                TaskStatus.TO_DO,
                null,
                Instant.now().plusSeconds(86400)
        );

        updateTaskDto = new UpdateTaskDto(
                "Updated Task",
                "Updated Description",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                null,
                Instant.now().plusSeconds(172800)
        );

        assignTaskDto = new AssignTaskDto(3L);

        taskResponseDto = new TaskResponseDto(
                1L,
                "Test Task",
                "Test Description",
                TaskStatus.TO_DO,
                TaskPriority.MEDIUM,
                1L,
                null,
                Instant.now().plusSeconds(86400),
                null,
                1L,
                null,
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
    // CREATE TASK TESTS
    // ============================================

    @Nested
    @DisplayName("createTask() Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully")
        void shouldCreateTaskSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndActiveCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(memberUser, 1L);
            doNothing().when(securityHelper).validateTaskTitleNotExists("Test Task", 1L);
            when(taskMapper.toEntity(createTaskDto)).thenReturn(task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.createTask(createTaskDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Test Task");
            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(memberUser);
            verify(securityHelper).projectExistsAndActiveCheck(1L);
            verify(securityHelper).canCreateTaskInProject(memberUser, 1L);
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("Should create task with assignee successfully")
        void shouldCreateTaskWithAssignee() {
            // Given
            CreateTaskDto dtoWithAssignee = new CreateTaskDto(
                    "Test Task",
                    "Test Description",
                    1L,
                    TaskPriority.HIGH,
                    TaskStatus.TO_DO,
                    3L,
                    Instant.now().plusSeconds(86400)
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndActiveCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(memberUser, 1L);
            doNothing().when(securityHelper).validateTaskTitleNotExists("Test Task", 1L);
            when(securityHelper.userExistsAndActiveCheck(3L)).thenReturn(assigneeUser);
            doNothing().when(securityHelper).canAssignTask(memberUser, 1L, 3L);
            when(taskMapper.toEntity(dtoWithAssignee)).thenReturn(task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.createTask(dtoWithAssignee);

            // Then
            assertThat(result).isNotNull();
            verify(securityHelper).userExistsAndActiveCheck(3L);
            verify(securityHelper).canAssignTask(memberUser, 1L, 3L);
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.createTask(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task creation data must not be null");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doThrow(new UserNotActiveException(memberUser.getEmail()))
                    .when(securityHelper).isUserActive(memberUser);

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(createTaskDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw ProjectNotFoundException when project doesn't exist")
        void shouldThrowExceptionWhenProjectNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndActiveCheck(1L))
                    .thenThrow(new ProjectNotFoundException(1L));

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(createTaskDto))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot create task in project")
        void shouldThrowExceptionWhenUserCannotCreateTask() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndActiveCheck(1L)).thenReturn(activeProject);
            doThrow(new AccessDeniedException("You must be a team member to create tasks in this project"))
                    .when(securityHelper).canCreateTaskInProject(memberUser, 1L);

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(createTaskDto))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw TaskTitleAlreadyExistsException when title already exists")
        void shouldThrowExceptionWhenTitleExists() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndActiveCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(memberUser, 1L);
            doThrow(new TaskTitleAlreadyExistsException("Test Task", 1L))
                    .when(securityHelper).validateTaskTitleNotExists("Test Task", 1L);

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(createTaskDto))
                    .isInstanceOf(TaskTitleAlreadyExistsException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should trim task title before creating")
        void shouldTrimTaskTitleBeforeCreating() {
            // Given
            CreateTaskDto dtoWithSpaces = new CreateTaskDto(
                    "  Test Task  ",
                    "Test Description",
                    1L,
                    TaskPriority.HIGH,
                    TaskStatus.TO_DO,
                    null,
                    Instant.now().plusSeconds(86400)
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndActiveCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(memberUser, 1L);
            doNothing().when(securityHelper).validateTaskTitleNotExists("Test Task", 1L);
            when(taskMapper.toEntity(dtoWithSpaces)).thenReturn(task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            taskService.createTask(dtoWithSpaces);

            // Then
            verify(securityHelper).validateTaskTitleNotExists("Test Task", 1L);
        }
    }

    // ============================================
    // GET TASK BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("getTaskById() Tests")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Should get task by ID successfully as admin")
        void shouldGetTaskByIdAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.taskExistsCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(adminUser, task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.getTaskById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(securityHelper).taskExistsCheck(1L);
            verify(securityHelper, never()).taskExistsAndNotDeletedCheck(1L);
        }

        @Test
        @DisplayName("Should get task by ID successfully as member")
        void shouldGetTaskByIdAsMember() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(memberUser, task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.getTaskById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(securityHelper).taskExistsAndNotDeletedCheck(1L);
            verify(securityHelper, never()).taskExistsCheck(1L);
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.getTaskById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task doesn't exist")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L))
                    .thenThrow(new TaskNotFoundException(1L));

            // When/Then
            assertThatThrownBy(() -> taskService.getTaskById(1L))
                    .isInstanceOf(TaskNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot access task")
        void shouldThrowExceptionWhenUserCannotAccessTask() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doThrow(new AccessDeniedException("You must be a team member to access this task"))
                    .when(securityHelper).canAccessTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.getTaskById(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ============================================
    // GET TASKS BY PROJECT TESTS
    // ============================================

    @Nested
    @DisplayName("getTasksByProject() Tests")
    class GetTasksByProjectTests {

        @Test
        @DisplayName("Should get tasks by project successfully as admin")
        void shouldGetTasksByProjectAsAdmin() {
            // Given
            Page<Task> taskPage = new PageImpl<>(List.of(task));

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(adminUser, 1L);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(taskRepository.findByProjectId(1L, pageable)).thenReturn(taskPage);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            Page<TaskResponseDto> result = taskService.getTasksByProject(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(taskRepository).findByProjectId(1L, pageable);
            verify(taskRepository, never()).findByProjectIdAndNotDeleted(1L, pageable);
        }

        @Test
        @DisplayName("Should get tasks by project successfully as member")
        void shouldGetTasksByProjectAsMember() {
            // Given
            Page<Task> taskPage = new PageImpl<>(List.of(task));

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(memberUser, 1L);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(taskRepository.findByProjectIdAndNotDeleted(1L, pageable)).thenReturn(taskPage);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            Page<TaskResponseDto> result = taskService.getTasksByProject(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(taskRepository).findByProjectIdAndNotDeleted(1L, pageable);
            verify(taskRepository, never()).findByProjectId(1L, pageable);
        }

        @Test
        @DisplayName("Should throw NullPointerException when project ID is null")
        void shouldThrowExceptionWhenProjectIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.getTasksByProject(null, pageable))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Project ID must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.getTasksByProject(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should return empty page when no tasks found")
        void shouldReturnEmptyPageWhenNoTasksFound() {
            // Given
            Page<Task> emptyPage = new PageImpl<>(List.of());

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.projectExistsAndNotDeletedCheck(1L)).thenReturn(activeProject);
            doNothing().when(securityHelper).canCreateTaskInProject(memberUser, 1L);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(taskRepository.findByProjectIdAndNotDeleted(1L, pageable)).thenReturn(emptyPage);

            // When
            Page<TaskResponseDto> result = taskService.getTasksByProject(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ============================================
    // UPDATE TASK TESTS
    // ============================================

    @Nested
    @DisplayName("updateTask() Tests")
    class UpdateTaskTests {

        @Test
        @DisplayName("Should update task successfully")
        void shouldUpdateTaskSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            doNothing().when(securityHelper).validateTaskTitleNotExistsForUpdate("Updated Task", 1L, 1L);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.updateTask(1L, updateTaskDto);

            // Then
            assertThat(result).isNotNull();
            verify(taskMapper).updateEntityFromDto(updateTaskDto, task);
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should update only title")
        void shouldUpdateOnlyTitle() {
            // Given
            UpdateTaskDto titleOnlyDto = new UpdateTaskDto(
                    "New Title",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            doNothing().when(securityHelper).validateTaskTitleNotExistsForUpdate("New Title", 1L, 1L);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            taskService.updateTask(1L, titleOnlyDto);

            // Then
            verify(securityHelper).validateTaskTitleNotExistsForUpdate("New Title", 1L, 1L);
        }

        @Test
        @DisplayName("Should update only status")
        void shouldUpdateOnlyStatus() {
            // Given
            UpdateTaskDto statusOnlyDto = new UpdateTaskDto(
                    null,
                    null,
                    TaskStatus.IN_PROGRESS,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            taskService.updateTask(1L, statusOnlyDto);

            // Then
            verify(taskMapper).updateEntityFromDto(statusOnlyDto, task);
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(null, updateTaskDto))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when update DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Update data must not be null");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no fields provided")
        void shouldThrowExceptionWhenNoFieldsProvided() {
            // Given
            UpdateTaskDto emptyDto = new UpdateTaskDto(null, null, null, null, null, null);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, emptyDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("At least one field must be provided for update");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when title is blank")
        void shouldThrowExceptionWhenTitleIsBlank() {
            // Given
            UpdateTaskDto blankTitleDto = new UpdateTaskDto(
                    "   ",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, blankTitleDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Task title cannot be blank");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when status is same as current")
        void shouldThrowExceptionWhenStatusIsSame() {
            // Given
            UpdateTaskDto sameStatusDto = new UpdateTaskDto(
                    null,
                    null,
                    TaskStatus.TO_DO,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, sameStatusDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Task is already in TO_DO status");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when transitioning deleted task")
        void shouldThrowExceptionWhenTransitioningDeletedTask() {
            // Given
            task.setStatus(TaskStatus.DELETED);
            UpdateTaskDto statusDto = new UpdateTaskDto(
                    null,
                    null,
                    TaskStatus.IN_PROGRESS,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, statusDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot change status of a deleted task");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when transitioning to DELETED")
        void shouldThrowExceptionWhenTransitioningToDeleted() {
            // Given
            UpdateTaskDto deletedDto = new UpdateTaskDto(
                    null,
                    null,
                    TaskStatus.DELETED,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, deletedDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Use deleteTask() method to delete a task");
        }

        @Test
        @DisplayName("Should allow valid status transition from TO_DO to IN_PROGRESS")
        void shouldAllowValidTransitionFromToDoToInProgress() {
            // Given
            UpdateTaskDto statusDto = new UpdateTaskDto(
                    null,
                    null,
                    TaskStatus.IN_PROGRESS,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            taskService.updateTask(1L, statusDto);

            // Then
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should throw IllegalStateException for invalid status transition")
        void shouldThrowExceptionForInvalidStatusTransition() {
            // Given
            UpdateTaskDto statusDto = new UpdateTaskDto(
                    null,
                    null,
                    TaskStatus.DONE,
                    null,
                    null,
                    null
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, statusDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition from TODO to DONE");
        }
    }

    // ============================================
    // DELETE TASK TESTS
    // ============================================

    @Nested
    @DisplayName("deleteTask() Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task successfully")
        void shouldDeleteTaskSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canDeleteTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            taskService.deleteTask(1L);

            // Then
            verify(taskRepository).save(task);
            verify(securityHelper).canDeleteTask(memberUser, task);
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.deleteTask(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when task is already deleted")
        void shouldThrowExceptionWhenTaskAlreadyDeleted() {
            // Given
            task.setStatus(TaskStatus.DELETED);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canDeleteTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.deleteTask(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Task is already deleted");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot delete task")
        void shouldThrowExceptionWhenUserCannotDeleteTask() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doThrow(new AccessDeniedException("Only team owner/admin or system admin can delete tasks"))
                    .when(securityHelper).canDeleteTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.deleteTask(1L))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should set updatedBy when deleting task")
        void shouldSetUpdatedByWhenDeletingTask() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canDeleteTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task savedTask = invocation.getArgument(0);
                assertThat(savedTask.getUpdatedBy()).isEqualTo(memberUser.getId());
                return savedTask;
            });

            // When
            taskService.deleteTask(1L);

            // Then
            verify(taskRepository).save(task);
        }
    }

    // ============================================
    // ASSIGN TASK TESTS
    // ============================================

    @Nested
    @DisplayName("assignTask() Tests")
    class AssignTaskTests {

        @Test
        @DisplayName("Should assign task successfully")
        void shouldAssignTaskSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            when(securityHelper.userExistsAndActiveCheck(3L)).thenReturn(assigneeUser);
            doNothing().when(securityHelper).canAssignTask(memberUser, 1L, 3L);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.assignTask(1L, assignTaskDto);

            // Then
            assertThat(result).isNotNull();
            verify(taskRepository).save(task);
            verify(securityHelper).canAssignTask(memberUser, 1L, 3L);
        }

        @Test
        @DisplayName("Should reassign task successfully")
        void shouldReassignTaskSuccessfully() {
            // Given
            task.setAssignedTo(2L); // Already assigned to someone else

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            when(securityHelper.userExistsAndActiveCheck(3L)).thenReturn(assigneeUser);
            doNothing().when(securityHelper).canAssignTask(memberUser, 1L, 3L);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.assignTask(1L, assignTaskDto);

            // Then
            assertThat(result).isNotNull();
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.assignTask(null, assignTaskDto))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.assignTask(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Assignment data must not be null");
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when assignee doesn't exist")
        void shouldThrowExceptionWhenAssigneeNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            when(securityHelper.userExistsAndActiveCheck(3L))
                    .thenThrow(new UserNotFoundException(3L));

            // When/Then
            assertThatThrownBy(() -> taskService.assignTask(1L, assignTaskDto))
                    .isInstanceOf(UserNotFoundException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot assign task")
        void shouldThrowExceptionWhenUserCannotAssignTask() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            when(securityHelper.userExistsAndActiveCheck(3L)).thenReturn(assigneeUser);
            doThrow(new AccessDeniedException("Can only assign tasks to team members"))
                    .when(securityHelper).canAssignTask(memberUser, 1L, 3L);

            // When/Then
            assertThatThrownBy(() -> taskService.assignTask(1L, assignTaskDto))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    // ============================================
    // UNASSIGN TASK TESTS
    // ============================================

    @Nested
    @DisplayName("unassignTask() Tests")
    class UnassignTaskTests {

        @Test
        @DisplayName("Should unassign task successfully")
        void shouldUnassignTaskSuccessfully() {
            // Given
            task.setAssignedTo(3L);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            TaskResponseDto result = taskService.unassignTask(1L);

            // Then
            assertThat(result).isNotNull();
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.unassignTask(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when task is already unassigned")
        void shouldThrowExceptionWhenTaskAlreadyUnassigned() {
            // Given
            task.setAssignedTo(null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);

            // When/Then
            assertThatThrownBy(() -> taskService.unassignTask(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Task is already unassigned");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot modify task")
        void shouldThrowExceptionWhenUserCannotModifyTask() {
            // Given
            task.setAssignedTo(3L);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doThrow(new AccessDeniedException("You don't have permission to modify this task"))
                    .when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.unassignTask(1L))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    // ============================================
    // GET MY TASKS TESTS
    // ============================================

    @Nested
    @DisplayName("getMyTasks() Tests")
    class GetMyTasksTests {

        @Test
        @DisplayName("Should get my tasks successfully")
        void shouldGetMyTasksSuccessfully() {
            // Given
            Page<Task> taskPage = new PageImpl<>(List.of(task));

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(taskRepository.findByAssignedToAndNotDeleted(memberUser.getId(), pageable))
                    .thenReturn(taskPage);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            Page<TaskResponseDto> result = taskService.getMyTasks(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(taskRepository).findByAssignedToAndNotDeleted(memberUser.getId(), pageable);
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.getMyTasks(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should return empty page when user has no tasks")
        void shouldReturnEmptyPageWhenUserHasNoTasks() {
            // Given
            Page<Task> emptyPage = new PageImpl<>(List.of());

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(taskRepository.findByAssignedToAndNotDeleted(memberUser.getId(), pageable))
                    .thenReturn(emptyPage);

            // When
            Page<TaskResponseDto> result = taskService.getMyTasks(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ============================================
    // GET ALL TASKS FOR ADMIN TESTS
    // ============================================

    @Nested
    @DisplayName("getAllTasksForAdmin() Tests")
    class GetAllTasksForAdminTests {

        @Test
        @DisplayName("Should get all tasks successfully as admin")
        void shouldGetAllTasksAsAdmin() {
            // Given
            Page<Task> taskPage = new PageImpl<>(List.of(task));

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(taskRepository.findAll(pageable)).thenReturn(taskPage);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When
            Page<TaskResponseDto> result = taskService.getAllTasksForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(securityHelper).systemAdminCheck(adminUser);
            verify(taskRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> taskService.getAllTasksForAdmin(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not admin")
        void shouldThrowExceptionWhenUserIsNotAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            doThrow(new AccessDeniedException("Only system admin can perform this operation"))
                    .when(securityHelper).systemAdminCheck(memberUser);

            // When/Then
            assertThatThrownBy(() -> taskService.getAllTasksForAdmin(pageable))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).findAll(any(Pageable.class));
        }
    }

    // ============================================
    // STATUS TRANSITION TESTS
    // ============================================

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should allow TO_DO to IN_PROGRESS transition")
        void shouldAllowToDoToInProgress() {
            // Given
            task.setStatus(TaskStatus.TO_DO);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.IN_PROGRESS, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow TO_DO to BLOCKED transition")
        void shouldAllowToDoToBlocked() {
            // Given
            task.setStatus(TaskStatus.TO_DO);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.BLOCKED, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not allow TO_DO to DONE transition")
        void shouldNotAllowToDoToDone() {
            // Given
            task.setStatus(TaskStatus.TO_DO);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.DONE, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition from TODO to DONE");
        }

        @Test
        @DisplayName("Should allow IN_PROGRESS to IN_REVIEW transition")
        void shouldAllowInProgressToInReview() {
            // Given
            task.setStatus(TaskStatus.IN_PROGRESS);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.IN_REVIEW, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow IN_PROGRESS to DONE transition")
        void shouldAllowInProgressToDone() {
            // Given
            task.setStatus(TaskStatus.IN_PROGRESS);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.DONE, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow IN_REVIEW to DONE transition")
        void shouldAllowInReviewToDone() {
            // Given
            task.setStatus(TaskStatus.IN_REVIEW);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.DONE, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow DONE to TO_DO transition (reopen)")
        void shouldAllowDoneToToDo() {
            // Given
            task.setStatus(TaskStatus.DONE);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.TO_DO, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow BLOCKED to TO_DO transition")
        void shouldAllowBlockedToToDo() {
            // Given
            task.setStatus(TaskStatus.BLOCKED);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.TO_DO, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toDto(task)).thenReturn(taskResponseDto);

            // When/Then
            assertThatCode(() -> taskService.updateTask(1L, dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not allow DONE to IN_REVIEW transition")
        void shouldNotAllowDoneToInReview() {
            // Given
            task.setStatus(TaskStatus.DONE);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.IN_REVIEW, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition from DONE");
        }

        @Test
        @DisplayName("Should not allow BLOCKED to DONE transition")
        void shouldNotAllowBlockedToDone() {
            // Given
            task.setStatus(TaskStatus.BLOCKED);
            UpdateTaskDto dto = new UpdateTaskDto(null, null, TaskStatus.DONE, null, null, null);

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canModifyTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> taskService.updateTask(1L, dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition from BLOCKED");
        }
    }
}


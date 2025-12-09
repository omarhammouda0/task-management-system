package com.taskmanagement.comment.service;

import com.taskmanagement.comment.dto.CommentResponseDto;
import com.taskmanagement.comment.dto.CreateCommentDto;
import com.taskmanagement.comment.dto.UpdateCommentDto;
import com.taskmanagement.comment.entity.Comment;
import com.taskmanagement.comment.enums.CommentStatus;
import com.taskmanagement.comment.mapper.CommentMapper;
import com.taskmanagement.comment.repository.CommentRepository;
import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.task.entity.Task;
import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CommentServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImplementation Unit Tests")
class CommentServiceImplementationTest {

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImplementation commentService;

    private User adminUser;
    private User memberUser;
    private User otherUser;
    private User inactiveUser;
    private Task task;
    private Comment comment;
    private Comment deletedComment;
    private CommentResponseDto commentResponseDto;
    private CreateCommentDto createCommentDto;
    private UpdateCommentDto updateCommentDto;
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

        // Setup other user (for access denial tests)
        otherUser = User.builder()
                .email("other@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Other")
                .lastName("User")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        otherUser.setId(3L);

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
        inactiveUser.setId(4L);

        // Setup task
        task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TO_DO)
                .priority(TaskPriority.MEDIUM)
                .projectId(1L)
                .build();
        task.setId(1L);
        task.setCreatedBy(2L);

        // Setup comment
        comment = Comment.builder()
                .content("Test Comment Content")
                .task(task)
                .taskId(1L)
                .user(memberUser)
                .userId(2L)
                .status(CommentStatus.ACTIVE)
                .build();
        comment.setId(1L);
        comment.setCreatedBy(2L);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        // Setup deleted comment
        deletedComment = Comment.builder()
                .content("Deleted Comment Content")
                .task(task)
                .taskId(1L)
                .user(memberUser)
                .userId(2L)
                .status(CommentStatus.DELETED)
                .build();
        deletedComment.setId(2L);
        deletedComment.setCreatedBy(2L);

        // Setup DTOs
        createCommentDto = new CreateCommentDto(1L, "Test Comment Content");
        updateCommentDto = new UpdateCommentDto("Updated Comment Content");

        commentResponseDto = new CommentResponseDto(
                1L,
                "Test Comment Content",
                1L,
                2L,
                CommentStatus.ACTIVE,
                2L,
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
    // CREATE COMMENT TESTS
    // ============================================

    @Nested
    @DisplayName("createComment() Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Should create comment successfully")
        void shouldCreateCommentSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canCreateCommentOnTask(memberUser, task);
            when(commentMapper.toEntity(createCommentDto, task, memberUser)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.createComment(createCommentDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Test Comment Content");
            assertThat(result.taskId()).isEqualTo(1L);
            assertThat(result.userId()).isEqualTo(2L);
            verify(securityHelper).getCurrentUser();
            verify(securityHelper).isUserActive(memberUser);
            verify(securityHelper).taskExistsAndNotDeletedCheck(1L);
            verify(securityHelper).canCreateCommentOnTask(memberUser, task);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should create comment as admin successfully")
        void shouldCreateCommentAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canCreateCommentOnTask(adminUser, task);
            when(commentMapper.toEntity(createCommentDto, task, adminUser)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.createComment(createCommentDto);

            // Then
            assertThat(result).isNotNull();
            verify(securityHelper).canCreateCommentOnTask(adminUser, task);
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.createComment(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Comment creation data must not be null");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(inactiveUser);
            doThrow(new UserNotActiveException(inactiveUser.getEmail()))
                    .when(securityHelper).isUserActive(inactiveUser);

            // When/Then
            assertThatThrownBy(() -> commentService.createComment(createCommentDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task doesn't exist")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L))
                    .thenThrow(new TaskNotFoundException(1L));

            // When/Then
            assertThatThrownBy(() -> commentService.createComment(createCommentDto))
                    .isInstanceOf(TaskNotFoundException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot create comment on task")
        void shouldThrowExceptionWhenUserCannotCreateComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doThrow(new AccessDeniedException("You must be a team member to create comments on this task"))
                    .when(securityHelper).canCreateCommentOnTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> commentService.createComment(createCommentDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("team member");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should set createdBy field when creating comment")
        void shouldSetCreatedByFieldWhenCreatingComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canCreateCommentOnTask(memberUser, task);
            when(commentMapper.toEntity(createCommentDto, task, memberUser)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            commentService.createComment(createCommentDto);

            // Then
            verify(commentRepository).save(argThat(savedComment ->
                savedComment.getCreatedBy().equals(memberUser.getId())
            ));
        }

        @Test
        @DisplayName("Should handle comment with whitespace content")
        void shouldHandleCommentWithWhitespaceContent() {
            // Given
            CreateCommentDto dtoWithWhitespace = new CreateCommentDto(1L, "  Content with spaces  ");
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canCreateCommentOnTask(memberUser, task);
            when(commentMapper.toEntity(dtoWithWhitespace, task, memberUser)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.createComment(dtoWithWhitespace);

            // Then
            assertThat(result).isNotNull();
            verify(commentMapper).toEntity(dtoWithWhitespace, task, memberUser);
        }

        @Test
        @DisplayName("Should handle long comment content")
        void shouldHandleLongCommentContent() {
            // Given
            String longContent = "A".repeat(2000);
            CreateCommentDto dtoWithLongContent = new CreateCommentDto(1L, longContent);
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canCreateCommentOnTask(memberUser, task);
            when(commentMapper.toEntity(dtoWithLongContent, task, memberUser)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.createComment(dtoWithLongContent);

            // Then
            assertThat(result).isNotNull();
        }
    }

    // ============================================
    // GET COMMENT BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("getCommentById() Tests")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Should get comment by ID successfully as admin")
        void shouldGetCommentByIdAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.commentExistsCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canAccessComment(adminUser, comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.getCommentById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(securityHelper).commentExistsCheck(1L);
            verify(securityHelper, never()).commentExistsAndNotDeletedCheck(1L);
        }

        @Test
        @DisplayName("Should get comment by ID successfully as member")
        void shouldGetCommentByIdAsMember() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canAccessComment(memberUser, comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.getCommentById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(securityHelper).commentExistsAndNotDeletedCheck(1L);
            verify(securityHelper, never()).commentExistsCheck(1L);
        }

        @Test
        @DisplayName("Admin should be able to view deleted comments")
        void adminShouldViewDeletedComments() {
            // Given
            CommentResponseDto deletedCommentDto = new CommentResponseDto(
                    2L, "Deleted Content", 1L, 2L, CommentStatus.DELETED,
                    2L, null, Instant.now(), Instant.now()
            );

            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(securityHelper.commentExistsCheck(2L)).thenReturn(deletedComment);
            doNothing().when(securityHelper).canAccessComment(adminUser, deletedComment);
            when(commentMapper.toDto(deletedComment)).thenReturn(deletedCommentDto);

            // When
            CommentResponseDto result = commentService.getCommentById(2L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(CommentStatus.DELETED);
        }

        @Test
        @DisplayName("Should throw NullPointerException when comment ID is null")
        void shouldThrowExceptionWhenCommentIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getCommentById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Comment ID must not be null");
        }

        @Test
        @DisplayName("Should throw CommentNotFoundException when comment doesn't exist")
        void shouldThrowExceptionWhenCommentNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(securityHelper.commentExistsAndNotDeletedCheck(999L))
                    .thenThrow(new CommentNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentById(999L))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot access comment")
        void shouldThrowExceptionWhenUserCannotAccessComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.isSystemAdmin(otherUser)).thenReturn(false);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doThrow(new AccessDeniedException("Cannot access comment"))
                    .when(securityHelper).canAccessComment(otherUser, comment);

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentById(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(inactiveUser);
            doThrow(new UserNotActiveException(inactiveUser.getEmail()))
                    .when(securityHelper).isUserActive(inactiveUser);

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentById(1L))
                    .isInstanceOf(UserNotActiveException.class);
        }
    }

    // ============================================
    // GET COMMENTS BY TASK TESTS
    // ============================================

    @Nested
    @DisplayName("getCommentsByTask() Tests")
    class GetCommentsByTaskTests {

        @Test
        @DisplayName("Should get comments by task as admin (includes deleted)")
        void shouldGetCommentsByTaskAsAdmin() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment, deletedComment));
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(adminUser, task);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(commentRepository.findByTaskId(1L, pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getCommentsByTask(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(commentRepository).findByTaskId(1L, pageable);
            verify(commentRepository, never()).findByTaskIdAndNotDeleted(anyLong(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get comments by task as member (excludes deleted)")
        void shouldGetCommentsByTaskAsMember() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment));
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(memberUser, task);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(commentRepository.findByTaskIdAndNotDeleted(1L, pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getCommentsByTask(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(commentRepository).findByTaskIdAndNotDeleted(1L, pageable);
            verify(commentRepository, never()).findByTaskId(anyLong(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no comments exist for task")
        void shouldReturnEmptyPageWhenNoComments() {
            // Given
            Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList());
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(memberUser, task);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(commentRepository.findByTaskIdAndNotDeleted(1L, pageable)).thenReturn(emptyPage);

            // When
            Page<CommentResponseDto> result = commentService.getCommentsByTask(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByTask(null, pageable))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByTask(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task doesn't exist")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(999L))
                    .thenThrow(new TaskNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByTask(999L, pageable))
                    .isInstanceOf(TaskNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot access task")
        void shouldThrowExceptionWhenUserCannotAccessTask() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doThrow(new AccessDeniedException("Cannot access task"))
                    .when(securityHelper).canAccessTask(otherUser, task);

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByTask(1L, pageable))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            Pageable secondPage = PageRequest.of(1, 5);
            Page<Comment> commentPage = new PageImpl<>(List.of(comment), secondPage, 10);
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(memberUser, task);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(commentRepository.findByTaskIdAndNotDeleted(1L, secondPage)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getCommentsByTask(1L, secondPage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    // ============================================
    // UPDATE COMMENT TESTS
    // ============================================

    @Nested
    @DisplayName("updateComment() Tests")
    class UpdateCommentTests {

        @Test
        @DisplayName("Should update comment successfully as owner")
        void shouldUpdateCommentSuccessfullyAsOwner() {
            // Given
            CommentResponseDto updatedDto = new CommentResponseDto(
                    1L, "Updated Comment Content", 1L, 2L, CommentStatus.ACTIVE,
                    2L, 2L, Instant.now(), Instant.now()
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canModifyComment(memberUser, comment);
            doNothing().when(commentMapper).updateEntityFromDto(updateCommentDto, comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(updatedDto);

            // When
            CommentResponseDto result = commentService.updateComment(1L, updateCommentDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Updated Comment Content");
            verify(securityHelper).canModifyComment(memberUser, comment);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should update comment successfully as admin")
        void shouldUpdateCommentSuccessfullyAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canModifyComment(adminUser, comment);
            doNothing().when(commentMapper).updateEntityFromDto(updateCommentDto, comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            CommentResponseDto result = commentService.updateComment(1L, updateCommentDto);

            // Then
            assertThat(result).isNotNull();
            verify(securityHelper).canModifyComment(adminUser, comment);
        }

        @Test
        @DisplayName("Should throw NullPointerException when comment ID is null")
        void shouldThrowExceptionWhenCommentIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(null, updateCommentDto))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Comment ID must not be null");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Update data must not be null");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw CommentNotFoundException when comment doesn't exist")
        void shouldThrowExceptionWhenCommentNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(999L))
                    .thenThrow(new CommentNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(999L, updateCommentDto))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot modify comment")
        void shouldThrowExceptionWhenUserCannotModifyComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doThrow(new AccessDeniedException("You can only edit your own comments"))
                    .when(securityHelper).canModifyComment(otherUser, comment);

            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(1L, updateCommentDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("own comments");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should set updatedBy field when updating comment")
        void shouldSetUpdatedByFieldWhenUpdatingComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canModifyComment(memberUser, comment);
            doNothing().when(commentMapper).updateEntityFromDto(updateCommentDto, comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toDto(comment)).thenReturn(commentResponseDto);

            // When
            commentService.updateComment(1L, updateCommentDto);

            // Then
            verify(commentRepository).save(argThat(savedComment ->
                savedComment.getUpdatedBy().equals(memberUser.getId())
            ));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(inactiveUser);
            doThrow(new UserNotActiveException(inactiveUser.getEmail()))
                    .when(securityHelper).isUserActive(inactiveUser);

            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(1L, updateCommentDto))
                    .isInstanceOf(UserNotActiveException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    // ============================================
    // DELETE COMMENT TESTS
    // ============================================

    @Nested
    @DisplayName("deleteComment() Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete comment successfully as owner")
        void shouldDeleteCommentSuccessfullyAsOwner() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canDeleteComment(memberUser, comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // When
            commentService.deleteComment(1L);

            // Then
            verify(securityHelper).canDeleteComment(memberUser, comment);
            verify(commentRepository).save(argThat(savedComment ->
                savedComment.getStatus() == CommentStatus.DELETED
            ));
        }

        @Test
        @DisplayName("Should delete comment successfully as admin")
        void shouldDeleteCommentSuccessfullyAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canDeleteComment(adminUser, comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // When
            commentService.deleteComment(1L);

            // Then
            verify(securityHelper).canDeleteComment(adminUser, comment);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when comment ID is null")
        void shouldThrowExceptionWhenCommentIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Comment ID must not be null");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw CommentNotFoundException when comment doesn't exist")
        void shouldThrowExceptionWhenCommentNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(999L))
                    .thenThrow(new CommentNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(999L))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot delete comment")
        void shouldThrowExceptionWhenUserCannotDeleteComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doThrow(new AccessDeniedException("Only comment author, team owner/admin, or system admin can delete comments"))
                    .when(securityHelper).canDeleteComment(otherUser, comment);

            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(1L))
                    .isInstanceOf(AccessDeniedException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when comment is already deleted")
        void shouldThrowExceptionWhenCommentAlreadyDeleted() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(2L)).thenReturn(deletedComment);
            doNothing().when(securityHelper).canDeleteComment(memberUser, deletedComment);

            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already deleted");

            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("Should set updatedBy field when deleting comment")
        void shouldSetUpdatedByFieldWhenDeletingComment() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.commentExistsAndNotDeletedCheck(1L)).thenReturn(comment);
            doNothing().when(securityHelper).canDeleteComment(memberUser, comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            // When
            commentService.deleteComment(1L);

            // Then
            verify(commentRepository).save(argThat(savedComment ->
                savedComment.getUpdatedBy().equals(memberUser.getId())
            ));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(inactiveUser);
            doThrow(new UserNotActiveException(inactiveUser.getEmail()))
                    .when(securityHelper).isUserActive(inactiveUser);

            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(1L))
                    .isInstanceOf(UserNotActiveException.class);

            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    // ============================================
    // GET MY COMMENTS TESTS
    // ============================================

    @Nested
    @DisplayName("getMyComments() Tests")
    class GetMyCommentsTests {

        @Test
        @DisplayName("Should get my comments successfully")
        void shouldGetMyCommentsSuccessfully() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment));
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(commentRepository.findByCreatedByAndNotDeleted(2L, pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getMyComments(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(commentRepository).findByCreatedByAndNotDeleted(2L, pageable);
        }

        @Test
        @DisplayName("Should return empty page when user has no comments")
        void shouldReturnEmptyPageWhenNoComments() {
            // Given
            Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList());
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(commentRepository.findByCreatedByAndNotDeleted(2L, pageable)).thenReturn(emptyPage);

            // When
            Page<CommentResponseDto> result = commentService.getMyComments(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getMyComments(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(inactiveUser);
            doThrow(new UserNotActiveException(inactiveUser.getEmail()))
                    .when(securityHelper).isUserActive(inactiveUser);

            // When/Then
            assertThatThrownBy(() -> commentService.getMyComments(pageable))
                    .isInstanceOf(UserNotActiveException.class);
        }

        @Test
        @DisplayName("Should exclude deleted comments from results")
        void shouldExcludeDeletedComments() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment));
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(commentRepository.findByCreatedByAndNotDeleted(2L, pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getMyComments(pageable);

            // Then
            assertThat(result.getContent()).allMatch(dto -> dto.status() != CommentStatus.DELETED);
            verify(commentRepository).findByCreatedByAndNotDeleted(anyLong(), any(Pageable.class));
        }
    }

    // ============================================
    // GET COMMENTS BY USER TESTS
    // ============================================

    @Nested
    @DisplayName("getCommentsByUser() Tests")
    class GetCommentsByUserTests {

        @Test
        @DisplayName("Should get comments by user successfully as admin")
        void shouldGetCommentsByUserAsAdmin() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment));
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(securityHelper.userExistsAndActiveCheck(2L)).thenReturn(memberUser);
            when(commentRepository.findByCreatedByAndNotDeleted(2L, pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getCommentsByUser(2L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(securityHelper).systemAdminCheck(adminUser);
            verify(securityHelper).userExistsAndActiveCheck(2L);
        }

        @Test
        @DisplayName("Should throw NullPointerException when user ID is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByUser(null, pageable))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByUser(2L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-admin tries to access")
        void shouldThrowExceptionWhenNonAdminAccesses() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            doThrow(new AccessDeniedException("Only system admin can perform this operation"))
                    .when(securityHelper).systemAdminCheck(memberUser);

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByUser(2L, pageable))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when target user doesn't exist")
        void shouldThrowExceptionWhenTargetUserNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(securityHelper.userExistsAndActiveCheck(999L))
                    .thenThrow(new UserNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByUser(999L, pageable))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when target user is inactive")
        void shouldThrowExceptionWhenTargetUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(securityHelper.userExistsAndActiveCheck(4L))
                    .thenThrow(new UserNotActiveException("inactive@example.com"));

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsByUser(4L, pageable))
                    .isInstanceOf(UserNotActiveException.class);
        }
    }

    // ============================================
    // GET ALL COMMENTS FOR ADMIN TESTS
    // ============================================

    @Nested
    @DisplayName("getAllCommentsForAdmin() Tests")
    class GetAllCommentsForAdminTests {

        @Test
        @DisplayName("Should get all comments successfully as admin")
        void shouldGetAllCommentsAsAdmin() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment, deletedComment));
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(commentRepository.findAll(pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getAllCommentsForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(securityHelper).systemAdminCheck(adminUser);
            verify(commentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> commentService.getAllCommentsForAdmin(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Pageable must not be null");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-admin tries to access")
        void shouldThrowExceptionWhenNonAdminAccesses() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            doThrow(new AccessDeniedException("Only system admin can perform this operation"))
                    .when(securityHelper).systemAdminCheck(memberUser);

            // When/Then
            assertThatThrownBy(() -> commentService.getAllCommentsForAdmin(pageable))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should return empty page when no comments exist")
        void shouldReturnEmptyPageWhenNoCommentsExist() {
            // Given
            Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList());
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(commentRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<CommentResponseDto> result = commentService.getAllCommentsForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should include deleted comments in admin view")
        void shouldIncludeDeletedCommentsInAdminView() {
            // Given
            Page<Comment> commentPage = new PageImpl<>(List.of(comment, deletedComment));
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(commentRepository.findAll(pageable)).thenReturn(commentPage);
            when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDto);

            // When
            Page<CommentResponseDto> result = commentService.getAllCommentsForAdmin(pageable);

            // Then
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(commentRepository).findAll(pageable);
        }
    }
}


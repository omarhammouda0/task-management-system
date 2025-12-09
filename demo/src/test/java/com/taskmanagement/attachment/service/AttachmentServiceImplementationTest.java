package com.taskmanagement.attachment.service;

import com.taskmanagement.attachment.dto.AttachmentResponseDto;
import com.taskmanagement.attachment.entity.Attachment;
import com.taskmanagement.attachment.enums.AttachmentStatus;
import com.taskmanagement.attachment.mapper.AttachmentMapper;
import com.taskmanagement.attachment.repository.AttachmentRepository;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AttachmentServiceImplementation.
 * Tests all business logic, authorization, validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttachmentServiceImplementation Unit Tests")
class AttachmentServiceImplementationTest {

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private AttachmentMapper attachmentMapper;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private AttachmentServiceImplementation attachmentService;

    private User adminUser;
    private User memberUser;
    private User otherUser;
    private User inactiveUser;
    private Task task;
    private Attachment attachment;
    private Attachment deletedAttachment;
    private AttachmentResponseDto attachmentResponseDto;
    private MockMultipartFile validFile;
    private MockMultipartFile emptyFile;
    private MockMultipartFile largeFile;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Set configuration values via reflection
        ReflectionTestUtils.setField(attachmentService, "maxFileSize", 10485760L); // 10MB
        ReflectionTestUtils.setField(attachmentService, "maxFilesPerTask", 10);
        ReflectionTestUtils.setField(attachmentService, "bucketName", "test-bucket");

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

        // Setup other user
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

        // Setup attachment
        attachment = Attachment.builder()
                .originalFilename("test-file.pdf")
                .storedFilename("uuid-test-file.pdf")
                .bucketName("test-bucket")
                .objectKey("attachments/uuid-test-file.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .task(task)
                .taskId(1L)
                .user(memberUser)
                .userId(2L)
                .status(AttachmentStatus.ACTIVE)
                .build();
        attachment.setId(1L);
        attachment.setCreatedBy(2L);
        attachment.setCreatedAt(Instant.now());
        attachment.setUpdatedAt(Instant.now());

        // Setup deleted attachment
        deletedAttachment = Attachment.builder()
                .originalFilename("deleted-file.pdf")
                .storedFilename("uuid-deleted-file.pdf")
                .bucketName("test-bucket")
                .objectKey("attachments/uuid-deleted-file.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .task(task)
                .taskId(1L)
                .user(memberUser)
                .userId(2L)
                .status(AttachmentStatus.DELETED)
                .build();
        deletedAttachment.setId(2L);
        deletedAttachment.setCreatedBy(2L);

        // Setup DTOs
        attachmentResponseDto = new AttachmentResponseDto(
                1L, "test-file.pdf", "uuid-test-file.pdf", 1024L,
                "application/pdf", 1L, 2L, AttachmentStatus.ACTIVE,
                "/api/attachments/1/download", 2L, null, Instant.now(), Instant.now()
        );

        // Setup files
        validFile = new MockMultipartFile(
                "file",
                "test-file.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        emptyFile = new MockMultipartFile(
                "file",
                "empty-file.txt",
                "text/plain",
                new byte[0]
        );

        largeFile = new MockMultipartFile(
                "file",
                "large-file.pdf",
                "application/pdf",
                new byte[11 * 1024 * 1024] // 11MB, exceeds 10MB limit
        );

        pageable = PageRequest.of(0, 10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ============================================
    // UPLOAD ATTACHMENT TESTS
    // ============================================

    @Nested
    @DisplayName("uploadAttachment() Tests")
    class UploadAttachmentTests {

        @Test
        @DisplayName("Should upload attachment successfully")
        void shouldUploadAttachmentSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);
            doNothing().when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());
            when(minioService.generateStoredFilename("test-file.pdf")).thenReturn("uuid-test-file.pdf");
            when(minioService.uploadFile(any(MultipartFile.class), anyString()))
                    .thenReturn("attachments/uuid-test-file.pdf");
            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
            when(attachmentMapper.toDto(attachment)).thenReturn(attachmentResponseDto);

            // When
            AttachmentResponseDto result = attachmentService.uploadAttachment(1L, validFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.originalFilename()).isEqualTo("test-file.pdf");
            verify(securityHelper).canUploadToTask(memberUser, task);
            verify(minioService).uploadFile(any(MultipartFile.class), anyString());
            verify(attachmentRepository).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(null, validFile))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("File must not be null");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, emptyFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File cannot be empty");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when file size exceeds maximum")
        void shouldThrowExceptionWhenFileTooLarge() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, largeFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File size exceeds maximum");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when file has no name")
        void shouldThrowExceptionWhenFileHasNoName() {
            // Given
            MockMultipartFile fileWithNoName = new MockMultipartFile(
                    "file", null, "text/plain", "content".getBytes()
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);
            doNothing().when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, fileWithNoName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File must have a name");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when file has blank name")
        void shouldThrowExceptionWhenFileHasBlankName() {
            // Given
            MockMultipartFile fileWithBlankName = new MockMultipartFile(
                    "file", "   ", "text/plain", "content".getBytes()
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);
            doNothing().when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, fileWithBlankName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("File must have a name");

            verify(attachmentRepository, never()).save(any(Attachment.class));
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
            assertThatThrownBy(() -> attachmentService.uploadAttachment(999L, validFile))
                    .isInstanceOf(TaskNotFoundException.class);

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot upload to task")
        void shouldThrowExceptionWhenUserCannotUpload() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doThrow(new AccessDeniedException("Cannot upload to this task"))
                    .when(securityHelper).canUploadToTask(otherUser, task);

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, validFile))
                    .isInstanceOf(AccessDeniedException.class);

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when max files per task exceeded")
        void shouldThrowExceptionWhenMaxFilesExceeded() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);
            doThrow(new IllegalStateException("Maximum 10 attachments allowed per task"))
                    .when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, validFile))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Maximum");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(inactiveUser);
            doThrow(new UserNotActiveException(inactiveUser.getEmail()))
                    .when(securityHelper).isUserActive(inactiveUser);

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(1L, validFile))
                    .isInstanceOf(UserNotActiveException.class);

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should use default content type when not provided")
        void shouldUseDefaultContentTypeWhenNotProvided() {
            // Given
            MockMultipartFile fileWithNoContentType = new MockMultipartFile(
                    "file", "test.bin", null, "content".getBytes()
            );

            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);
            doNothing().when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());
            when(minioService.generateStoredFilename("test.bin")).thenReturn("uuid-test.bin");
            when(minioService.uploadFile(any(MultipartFile.class), anyString()))
                    .thenReturn("attachments/uuid-test.bin");
            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
            when(attachmentMapper.toDto(attachment)).thenReturn(attachmentResponseDto);

            // When
            attachmentService.uploadAttachment(1L, fileWithNoContentType);

            // Then
            verify(attachmentRepository).save(argThat(savedAttachment ->
                    savedAttachment.getContentType().equals("application/octet-stream")
            ));
        }

        @Test
        @DisplayName("Should set createdBy field when uploading")
        void shouldSetCreatedByFieldWhenUploading() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canUploadToTask(memberUser, task);
            doNothing().when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());
            when(minioService.generateStoredFilename(anyString())).thenReturn("uuid-test-file.pdf");
            when(minioService.uploadFile(any(MultipartFile.class), anyString()))
                    .thenReturn("attachments/uuid-test-file.pdf");
            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
            when(attachmentMapper.toDto(attachment)).thenReturn(attachmentResponseDto);

            // When
            attachmentService.uploadAttachment(1L, validFile);

            // Then
            verify(attachmentRepository).save(argThat(savedAttachment ->
                    savedAttachment.getCreatedBy().equals(memberUser.getId())
            ));
        }

        @Test
        @DisplayName("Should handle various file extensions")
        void shouldHandleVariousFileExtensions() {
            // Given
            String[] extensions = {".pdf", ".jpg", ".png", ".docx", ".xlsx", ".txt"};

            for (String ext : extensions) {
                MockMultipartFile file = new MockMultipartFile(
                        "file", "test" + ext, "application/octet-stream", "content".getBytes()
                );

                when(securityHelper.getCurrentUser()).thenReturn(memberUser);
                doNothing().when(securityHelper).isUserActive(memberUser);
                when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
                doNothing().when(securityHelper).canUploadToTask(memberUser, task);
                doNothing().when(securityHelper).validateMaxFilesPerTask(eq(1L), anyLong());
                when(minioService.generateStoredFilename(anyString())).thenReturn("uuid-test" + ext);
                when(minioService.uploadFile(any(MultipartFile.class), anyString()))
                        .thenReturn("attachments/uuid-test" + ext);
                when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
                when(attachmentMapper.toDto(attachment)).thenReturn(attachmentResponseDto);

                // When/Then - should not throw
                assertThatCode(() -> attachmentService.uploadAttachment(1L, file))
                        .doesNotThrowAnyException();
            }
        }
    }

    // ============================================
    // GET ATTACHMENT BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("getAttachmentById() Tests")
    class GetAttachmentByIdTests {

        @Test
        @DisplayName("Should get attachment by ID successfully")
        void shouldGetAttachmentByIdSuccessfully() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doNothing().when(securityHelper).canAccessAttachment(memberUser, attachment);
            when(attachmentMapper.toDto(attachment)).thenReturn(attachmentResponseDto);

            // When
            AttachmentResponseDto result = attachmentService.getAttachmentById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.originalFilename()).isEqualTo("test-file.pdf");
            verify(securityHelper).canAccessAttachment(memberUser, attachment);
        }

        @Test
        @DisplayName("Should throw NullPointerException when attachment ID is null")
        void shouldThrowExceptionWhenAttachmentIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.getAttachmentById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Attachment ID must not be null");
        }

        @Test
        @DisplayName("Should throw AttachmentNotFoundException when attachment doesn't exist")
        void shouldThrowExceptionWhenAttachmentNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(999L))
                    .thenThrow(new AttachmentNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> attachmentService.getAttachmentById(999L))
                    .isInstanceOf(AttachmentNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot access attachment")
        void shouldThrowExceptionWhenUserCannotAccess() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doThrow(new AccessDeniedException("Cannot access attachment"))
                    .when(securityHelper).canAccessAttachment(otherUser, attachment);

            // When/Then
            assertThatThrownBy(() -> attachmentService.getAttachmentById(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ============================================
    // GET ATTACHMENTS BY TASK TESTS
    // ============================================

    @Nested
    @DisplayName("getAttachmentsByTask() Tests")
    class GetAttachmentsByTaskTests {

        @Test
        @DisplayName("Should get attachments by task as admin (includes deleted)")
        void shouldGetAttachmentsByTaskAsAdmin() {
            // Given
            Page<Attachment> attachmentPage = new PageImpl<>(List.of(attachment, deletedAttachment));
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(adminUser, task);
            when(securityHelper.isSystemAdmin(adminUser)).thenReturn(true);
            when(attachmentRepository.findByTaskId(1L, pageable)).thenReturn(attachmentPage);
            when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(attachmentResponseDto);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getAttachmentsByTask(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(attachmentRepository).findByTaskId(1L, pageable);
        }

        @Test
        @DisplayName("Should get attachments by task as member (excludes deleted)")
        void shouldGetAttachmentsByTaskAsMember() {
            // Given
            Page<Attachment> attachmentPage = new PageImpl<>(List.of(attachment));
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(memberUser, task);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(attachmentRepository.findByTaskIdAndNotDeleted(1L, pageable)).thenReturn(attachmentPage);
            when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(attachmentResponseDto);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getAttachmentsByTask(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(attachmentRepository).findByTaskIdAndNotDeleted(1L, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no attachments exist")
        void shouldReturnEmptyPageWhenNoAttachments() {
            // Given
            Page<Attachment> emptyPage = new PageImpl<>(Collections.emptyList());
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.taskExistsAndNotDeletedCheck(1L)).thenReturn(task);
            doNothing().when(securityHelper).canAccessTask(memberUser, task);
            when(securityHelper.isSystemAdmin(memberUser)).thenReturn(false);
            when(attachmentRepository.findByTaskIdAndNotDeleted(1L, pageable)).thenReturn(emptyPage);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getAttachmentsByTask(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should throw NullPointerException when task ID is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.getAttachmentsByTask(null, pageable))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Task ID must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.getAttachmentsByTask(1L, null))
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
            assertThatThrownBy(() -> attachmentService.getAttachmentsByTask(999L, pageable))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    // ============================================
    // DOWNLOAD ATTACHMENT TESTS
    // ============================================

    @Nested
    @DisplayName("downloadAttachment() Tests")
    class DownloadAttachmentTests {

        @Test
        @DisplayName("Should download attachment successfully")
        void shouldDownloadAttachmentSuccessfully() {
            // Given
            InputStream inputStream = new ByteArrayInputStream("file content".getBytes());
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doNothing().when(securityHelper).canAccessAttachment(memberUser, attachment);
            when(minioService.downloadFile("attachments/uuid-test-file.pdf")).thenReturn(inputStream);

            // When
            ResponseEntity<InputStreamResource> result = attachmentService.downloadAttachment(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
            assertThat(result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                    .contains("test-file.pdf");
            verify(minioService).downloadFile("attachments/uuid-test-file.pdf");
        }

        @Test
        @DisplayName("Should throw NullPointerException when attachment ID is null")
        void shouldThrowExceptionWhenAttachmentIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.downloadAttachment(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Attachment ID must not be null");
        }

        @Test
        @DisplayName("Should throw AttachmentNotFoundException when attachment doesn't exist")
        void shouldThrowExceptionWhenAttachmentNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(999L))
                    .thenThrow(new AttachmentNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> attachmentService.downloadAttachment(999L))
                    .isInstanceOf(AttachmentNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot access attachment")
        void shouldThrowExceptionWhenUserCannotAccess() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doThrow(new AccessDeniedException("Cannot access attachment"))
                    .when(securityHelper).canAccessAttachment(otherUser, attachment);

            // When/Then
            assertThatThrownBy(() -> attachmentService.downloadAttachment(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should handle RuntimeException from MinIO")
        void shouldHandleMinioException() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doNothing().when(securityHelper).canAccessAttachment(memberUser, attachment);
            when(minioService.downloadFile(anyString()))
                    .thenThrow(new RuntimeException("MinIO connection failed"));

            // When/Then
            assertThatThrownBy(() -> attachmentService.downloadAttachment(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("MinIO");
        }
    }

    // ============================================
    // DELETE ATTACHMENT TESTS
    // ============================================

    @Nested
    @DisplayName("deleteAttachment() Tests")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("Should delete attachment successfully as owner")
        void shouldDeleteAttachmentSuccessfullyAsOwner() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doNothing().when(securityHelper).canDeleteAttachment(memberUser, attachment);
            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

            // When
            attachmentService.deleteAttachment(1L);

            // Then
            verify(securityHelper).canDeleteAttachment(memberUser, attachment);
            verify(attachmentRepository).save(argThat(savedAttachment ->
                    savedAttachment.getStatus() == AttachmentStatus.DELETED
            ));
        }

        @Test
        @DisplayName("Should delete attachment successfully as admin")
        void shouldDeleteAttachmentSuccessfullyAsAdmin() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doNothing().when(securityHelper).canDeleteAttachment(adminUser, attachment);
            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

            // When
            attachmentService.deleteAttachment(1L);

            // Then
            verify(securityHelper).canDeleteAttachment(adminUser, attachment);
            verify(attachmentRepository).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when attachment ID is null")
        void shouldThrowExceptionWhenAttachmentIdIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.deleteAttachment(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Attachment ID must not be null");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw AttachmentNotFoundException when attachment doesn't exist")
        void shouldThrowExceptionWhenAttachmentNotFound() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(999L))
                    .thenThrow(new AttachmentNotFoundException(999L));

            // When/Then
            assertThatThrownBy(() -> attachmentService.deleteAttachment(999L))
                    .isInstanceOf(AttachmentNotFoundException.class);

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user cannot delete attachment")
        void shouldThrowExceptionWhenUserCannotDelete() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(otherUser);
            doNothing().when(securityHelper).isUserActive(otherUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doThrow(new AccessDeniedException("Cannot delete attachment"))
                    .when(securityHelper).canDeleteAttachment(otherUser, attachment);

            // When/Then
            assertThatThrownBy(() -> attachmentService.deleteAttachment(1L))
                    .isInstanceOf(AccessDeniedException.class);

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when attachment is already deleted")
        void shouldThrowExceptionWhenAttachmentAlreadyDeleted() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(2L)).thenReturn(deletedAttachment);
            doNothing().when(securityHelper).canDeleteAttachment(memberUser, deletedAttachment);

            // When/Then
            assertThatThrownBy(() -> attachmentService.deleteAttachment(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already deleted");

            verify(attachmentRepository, never()).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Should set updatedBy field when deleting")
        void shouldSetUpdatedByFieldWhenDeleting() {
            // Given
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(securityHelper.attachmentExistsAndNotDeletedCheck(1L)).thenReturn(attachment);
            doNothing().when(securityHelper).canDeleteAttachment(memberUser, attachment);
            when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

            // When
            attachmentService.deleteAttachment(1L);

            // Then
            verify(attachmentRepository).save(argThat(savedAttachment ->
                    savedAttachment.getUpdatedBy().equals(memberUser.getId())
            ));
        }
    }

    // ============================================
    // GET MY ATTACHMENTS TESTS
    // ============================================

    @Nested
    @DisplayName("getMyAttachments() Tests")
    class GetMyAttachmentsTests {

        @Test
        @DisplayName("Should get my attachments successfully")
        void shouldGetMyAttachmentsSuccessfully() {
            // Given
            Page<Attachment> attachmentPage = new PageImpl<>(List.of(attachment));
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(attachmentRepository.findByCreatedByAndNotDeleted(2L, pageable)).thenReturn(attachmentPage);
            when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(attachmentResponseDto);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getMyAttachments(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(attachmentRepository).findByCreatedByAndNotDeleted(2L, pageable);
        }

        @Test
        @DisplayName("Should return empty page when user has no attachments")
        void shouldReturnEmptyPageWhenNoAttachments() {
            // Given
            Page<Attachment> emptyPage = new PageImpl<>(Collections.emptyList());
            when(securityHelper.getCurrentUser()).thenReturn(memberUser);
            doNothing().when(securityHelper).isUserActive(memberUser);
            when(attachmentRepository.findByCreatedByAndNotDeleted(2L, pageable)).thenReturn(emptyPage);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getMyAttachments(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.getMyAttachments(null))
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
            assertThatThrownBy(() -> attachmentService.getMyAttachments(pageable))
                    .isInstanceOf(UserNotActiveException.class);
        }
    }

    // ============================================
    // GET ALL ATTACHMENTS FOR ADMIN TESTS
    // ============================================

    @Nested
    @DisplayName("getAllAttachmentsForAdmin() Tests")
    class GetAllAttachmentsForAdminTests {

        @Test
        @DisplayName("Should get all attachments successfully as admin")
        void shouldGetAllAttachmentsAsAdmin() {
            // Given
            Page<Attachment> attachmentPage = new PageImpl<>(List.of(attachment, deletedAttachment));
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(attachmentRepository.findAll(pageable)).thenReturn(attachmentPage);
            when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(attachmentResponseDto);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getAllAttachmentsForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(securityHelper).systemAdminCheck(adminUser);
            verify(attachmentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should throw NullPointerException when pageable is null")
        void shouldThrowExceptionWhenPageableIsNull() {
            // When/Then
            assertThatThrownBy(() -> attachmentService.getAllAttachmentsForAdmin(null))
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
            assertThatThrownBy(() -> attachmentService.getAllAttachmentsForAdmin(pageable))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should return empty page when no attachments exist")
        void shouldReturnEmptyPageWhenNoAttachments() {
            // Given
            Page<Attachment> emptyPage = new PageImpl<>(Collections.emptyList());
            when(securityHelper.getCurrentUser()).thenReturn(adminUser);
            doNothing().when(securityHelper).isUserActive(adminUser);
            doNothing().when(securityHelper).systemAdminCheck(adminUser);
            when(attachmentRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<AttachmentResponseDto> result = attachmentService.getAllAttachmentsForAdmin(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}


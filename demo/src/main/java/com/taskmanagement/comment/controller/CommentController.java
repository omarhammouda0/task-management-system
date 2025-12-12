package com.taskmanagement.comment.controller;

import com.taskmanagement.comment.dto.CommentResponseDto;
import com.taskmanagement.comment.dto.CreateCommentDto;
import com.taskmanagement.comment.dto.UpdateCommentDto;
import com.taskmanagement.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Task comment management - add comments and discussions to tasks")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "Create a new comment",
            description = """
                    Adds a new comment to a task for discussions, updates, and feedback.
                    
                    **📋 Required Fields:**
                    - `taskId` (Long) - ID of the task to comment on ✅ REQUIRED
                    - `content` (String, 1-2000 chars) - Comment text ✅ REQUIRED
                    
                    **Business Logic:**
                    - Comment is created with ACTIVE status by default
                    - User must be a member of the task's project team to comment
                    - System admins can comment on any task
                    - Task must not be deleted
                    - Comments are automatically timestamped
                    
                    **Validations:**
                    - Task ID: Required, must be a positive number
                    - Content: Required, max 2000 characters
                    - Task must exist and not be deleted
                    - User must be an active team member of the project
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not a member of the project's team"),
            @ApiResponse(responseCode = "404", description = "Task not found or deleted")
    })
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Comment creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create Comment",
                                    value = """
                                            {
                                                "taskId": 1,
                                                "content": "Great progress on this task! Just a few more items to complete."
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateCommentDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.createComment(dto));
    }

    @Operation(
            summary = "Get comment by ID",
            description = """
                    Retrieves a specific comment by its ID.
                    
                    **Business Logic:**
                    - User must be a member of the project's team to view the comment
                    - System admins can view any comment (including deleted ones)
                    - Regular users cannot see deleted comments
                    - Returns complete comment details including author and metadata
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not authorized to view this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found or deleted")
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> getCommentById(
            @Parameter(description = "Comment ID", required = true, example = "1")
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @Operation(
            summary = "Get comments by task",
            description = """
                    Retrieves all comments for a specific task with pagination support.
                    
                    **Business Logic:**
                    - Returns paginated list of comments for the task
                    - Comments are ordered by creation time (newest first by default)
                    - User must be a member of the project's team
                    - System admins can see all comments including deleted ones
                    - Regular users only see active comments
                    - Task must not be deleted
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc
                    
                    **Authorization:**
                    - Team members of the project containing the task
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not a member of the project's team"),
            @ApiResponse(responseCode = "404", description = "Task not found or deleted")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId, pageable));
    }

    @Operation(
            summary = "Update a comment",
            description = """
                    Updates an existing comment content.
                    
                    **📋 Required Fields:**
                    - `content` (String, 1-2000 chars) - New comment text ✅ REQUIRED
                    
                    **Business Logic:**
                    - Only the comment author can update their own comment
                    - System admins can update any comment
                    - Comment must not be deleted
                    - Content is trimmed of leading/trailing whitespace
                    - Updated timestamp is automatically set
                    
                    **Validations:**
                    - Content: Required, max 2000 characters
                    - Comment must exist and not be deleted
                    - User must be the comment author or system admin
                    
                    **Authorization:**
                    - Comment author (must be the user who created the comment)
                    - System admins (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only comment author can update"),
            @ApiResponse(responseCode = "404", description = "Comment not found or deleted")
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @Parameter(description = "Comment ID", required = true, example = "1")
            @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Comment update details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Comment",
                                    value = """
                                            {
                                                "content": "Updated comment content with more details."
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateCommentDto dto) {
        return ResponseEntity.ok(commentService.updateComment(commentId, dto));
    }

    @Operation(
            summary = "Delete a comment",
            description = """
                    Soft deletes a comment by setting its status to DELETED.
                    
                    **Business Logic:**
                    - Performs soft delete (status changed to DELETED)
                    - Comment data is preserved in the database
                    - Deleted comments are hidden from regular users
                    - System admins can still view deleted comments
                    - Cannot delete an already deleted comment
                    
                    **Authorization:**
                    - Comment author (the user who created the comment)
                    - Team owners and admins of the project
                    - System admins (ADMIN role)
                    - Note: Regular team members cannot delete other members' comments
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - comment is already deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only author, team owner/admin, or system admin can delete"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Comment ID", required = true, example = "1")
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get my comments",
            description = """
                    Retrieves all comments created by the currently authenticated user.
                    
                    **Business Logic:**
                    - Returns only comments created by the current user
                    - Excludes deleted comments
                    - Supports pagination and sorting
                    - User must be active
                    - Returns comments from all tasks across all projects
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc
                    
                    **Authorization:**
                    - Any authenticated active user
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user account is not active")
    })
    @GetMapping("/my-comments")
    public ResponseEntity<Page<CommentResponseDto>> getMyComments(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getMyComments(pageable));
    }

    @Operation(
            summary = "Get comments by user (Admin)",
            description = """
                    Retrieves all comments by a specific user. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Returns all comments created by the specified user
                    - Excludes deleted comments
                    - Supports pagination and sorting
                    - Only accessible by system administrators
                    - Target user must exist and be active
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId, pageable));
    }

    @Operation(
            summary = "Get all comments (Admin)",
            description = """
                    Retrieves all comments in the system. **Requires ADMIN role.**
                    
                    **Business Logic:**
                    - Returns all comments from all tasks and projects
                    - Includes deleted comments
                    - Supports pagination and sorting
                    - Only accessible by system administrators
                    
                    **Pagination:**
                    - Default page size: 20
                    - Page numbers start from 0
                    - Sort example: ?sort=createdAt,desc
                    
                    **Authorization:**
                    - System admins only (ADMIN role)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CommentResponseDto>> getAllCommentsForAdmin(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getAllCommentsForAdmin(pageable));
    }
}

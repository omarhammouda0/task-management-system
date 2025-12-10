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
            description = "Adds a new comment to a task. Comments can be used for discussions, updates, and feedback."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
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
            description = "Retrieves a specific comment by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> getCommentById(
            @Parameter(description = "Comment ID", required = true, example = "1")
            @PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @Operation(
            summary = "Get comments by task",
            description = "Retrieves all comments for a specific task with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByTask(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId, pageable));
    }

    @Operation(
            summary = "Update a comment",
            description = "Updates an existing comment. Only the comment author can update their comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the comment author"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
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
            description = "Deletes a comment. Only the comment author or admin can delete a comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to delete this comment"),
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
            description = "Retrieves all comments created by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-comments")
    public ResponseEntity<Page<CommentResponseDto>> getMyComments(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getMyComments(pageable));
    }

    @Operation(
            summary = "Get comments by user (Admin)",
            description = "Retrieves all comments by a specific user. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsByUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId, pageable));
    }

    @Operation(
            summary = "Get all comments (Admin)",
            description = "Retrieves all comments in the system. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CommentResponseDto>> getAllCommentsForAdmin(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getAllCommentsForAdmin(pageable));
    }
}

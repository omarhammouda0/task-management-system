package com.taskmanagement.comment.service;

import com.taskmanagement.comment.dto.CommentResponseDto;
import com.taskmanagement.comment.dto.CreateCommentDto;
import com.taskmanagement.comment.dto.UpdateCommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponseDto createComment(CreateCommentDto dto);

    CommentResponseDto getCommentById(Long commentId);

    Page<CommentResponseDto> getCommentsByTask(Long taskId, Pageable pageable);

    CommentResponseDto updateComment(Long commentId, UpdateCommentDto dto);

    void deleteComment(Long commentId);

    Page<CommentResponseDto> getMyComments(Pageable pageable);

    Page<CommentResponseDto> getCommentsByUser(Long userId, Pageable pageable);

    Page<CommentResponseDto> getAllCommentsForAdmin(Pageable pageable);
}
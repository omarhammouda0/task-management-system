package com.taskmanagement.comment.service;

import com.taskmanagement.comment.dto.CommentResponseDto;
import com.taskmanagement.comment.dto.CreateCommentDto;
import com.taskmanagement.comment.dto.UpdateCommentDto;
import com.taskmanagement.comment.enums.CommentStatus;
import com.taskmanagement.comment.mapper.CommentMapper;
import com.taskmanagement.comment.repository.CommentRepository;
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
public class CommentServiceImplementation implements CommentService {

    private final SecurityHelper securityHelper;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentResponseDto createComment(CreateCommentDto dto) {

        Objects.requireNonNull(dto, "Comment creation data must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(dto.taskId());

        securityHelper.canCreateCommentOnTask(currentUser, task);

        var comment = commentMapper.toEntity(dto, task, currentUser);
        comment.setCreatedBy(currentUser.getId());

        var savedComment = commentRepository.save(comment);

        log.info("Comment (ID: {}) created on task {} by user {} (ID: {})",
                savedComment.getId(),
                task.getId(),
                currentUser.getEmail(),
                currentUser.getId());

        return commentMapper.toDto(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentById(Long commentId) {
        Objects.requireNonNull(commentId, "Comment ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var comment = securityHelper.isSystemAdmin(currentUser)
                ? securityHelper.commentExistsCheck(commentId)
                : securityHelper.commentExistsAndNotDeletedCheck(commentId);

        securityHelper.canAccessComment(currentUser, comment);

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByTask(Long taskId, Pageable pageable) {
        Objects.requireNonNull(taskId, "Task ID must not be null");
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        securityHelper.canAccessTask(currentUser, task);

        if (securityHelper.isSystemAdmin(currentUser)) {
            return commentRepository.findByTaskId(taskId, pageable)
                    .map(commentMapper::toDto);
        } else {
            return commentRepository.findByTaskIdAndNotDeleted(taskId, pageable)
                    .map(commentMapper::toDto);
        }
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, UpdateCommentDto dto) {
        Objects.requireNonNull(commentId, "Comment ID must not be null");
        Objects.requireNonNull(dto, "Update data must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var comment = securityHelper.commentExistsAndNotDeletedCheck(commentId);

        securityHelper.canModifyComment(currentUser, comment);

        commentMapper.updateEntityFromDto(dto, comment);
        comment.setUpdatedBy(currentUser.getId());

        var updatedComment = commentRepository.save(comment);

        log.info("Comment (ID: {}) updated by user {} (ID: {})",
                updatedComment.getId(),
                currentUser.getEmail(),
                currentUser.getId());

        return commentMapper.toDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Objects.requireNonNull(commentId, "Comment ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var comment = securityHelper.commentExistsAndNotDeletedCheck(commentId);

        securityHelper.canDeleteComment(currentUser, comment);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new IllegalStateException("Comment is already deleted");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdatedBy(currentUser.getId());

        commentRepository.save(comment);

        log.info("Comment (ID: {}) deleted by user {} (ID: {})",
                comment.getId(),
                currentUser.getEmail(),
                currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getMyComments(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        return commentRepository.findByCreatedByAndNotDeleted(currentUser.getId(), pageable)
                .map(commentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByUser(Long userId, Pageable pageable) {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        securityHelper.systemAdminCheck(currentUser);

        securityHelper.userExistsAndActiveCheck(userId);

        return commentRepository.findByCreatedByAndNotDeleted(userId, pageable)
                .map(commentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getAllCommentsForAdmin(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        securityHelper.systemAdminCheck(currentUser);

        return commentRepository.findAll(pageable)
                .map(commentMapper::toDto);
    }
}
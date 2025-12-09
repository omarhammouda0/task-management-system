package com.taskmanagement.comment.mapper;

import com.taskmanagement.comment.dto.CommentResponseDto;
import com.taskmanagement.comment.dto.CreateCommentDto;
import com.taskmanagement.comment.dto.UpdateCommentDto;
import com.taskmanagement.comment.entity.Comment;
import com.taskmanagement.comment.enums.CommentStatus;
import com.taskmanagement.task.entity.Task;
import com.taskmanagement.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public Comment toEntity(CreateCommentDto dto, Task task, User user) {
        if (dto == null) {
            return null;
        }

        return Comment.builder()

                .content(dto.content().trim())
                .task(task)
                .taskId(task.getId())
                .user(user)
                .userId(user.getId())
                .status(CommentStatus.ACTIVE)

                .build();
    }

    public CommentResponseDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getTaskId(),
                comment.getUserId(),
                comment.getStatus(),
                comment.getCreatedBy(),
                comment.getUpdatedBy(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public void updateEntityFromDto(UpdateCommentDto dto, Comment comment) {
        if (dto == null || comment == null) {
            return;
        }

        if (dto.content() != null) {
            comment.setContent(dto.content().trim());
        }
    }
}
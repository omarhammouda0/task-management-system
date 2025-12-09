package com.taskmanagement.comment.service;

import com.taskmanagement.comment.entity.Comment;
import com.taskmanagement.comment.enums.CommentStatus;
import com.taskmanagement.comment.repository.CommentRepository;
import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.repository.ProjectRepository;
import com.taskmanagement.task.entity.Task;
import com.taskmanagement.task.enums.TaskStatus;
import com.taskmanagement.task.repository.TaskRepository;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.team.repository.TeamMemberRepository;
import com.taskmanagement.user.entity.User;
import com.taskmanagement.user.enums.Role;
import com.taskmanagement.user.enums.UserStatus;
import com.taskmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component("commentSecurityHelper")
public class SecurityHelper {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CommentRepository commentRepository;

    protected User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication required");
        }

        String email = auth.getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    protected void isUserActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(user.getEmail());
        }
    }

    protected boolean isSystemAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    protected void systemAdminCheck(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only system admin can perform this operation");
        }
    }

    protected User userExistsAndActiveCheck(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(user.getEmail());
        }

        return user;
    }

    protected Task taskExistsAndNotDeletedCheck(Long taskId) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (task.getStatus() == TaskStatus.DELETED) {
            throw new TaskNotFoundException(taskId);
        }

        return task;
    }

    protected Comment commentExistsCheck(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    protected Comment commentExistsAndNotDeletedCheck(Long commentId) {
        return commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    protected void canAccessTask(User user, Task task) {
        if (isSystemAdmin(user)) {
            return;
        }

        var project = projectRepository.findById(task.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(task.getProjectId()));

        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), user.getId())) {
            throw new AccessDeniedException("You must be a team member to access this task");
        }
    }

    protected void canCreateCommentOnTask(User user, Task task) {

        if (isSystemAdmin(user)) {
            return;
        }

        canAccessTask(user, task);
    }

    protected void canAccessComment(User user, Comment comment) {

        if (isSystemAdmin(user)) {
            return;
        }

        var task = taskExistsAndNotDeletedCheck(comment.getTaskId());
        canAccessTask(user, task);
    }

    protected void canModifyComment(User user, Comment comment) {
        if (isSystemAdmin(user)) {
            return;
        }

        if (!comment.getCreatedBy().equals(user.getId())) {
            throw new AccessDeniedException("You can only edit your own comments");
        }
    }

    protected void canDeleteComment(User user, Comment comment) {
        if (isSystemAdmin(user)) {
            return;
        }

        if (comment.getCreatedBy().equals(user.getId())) {
            return;
        }

        var task = taskExistsAndNotDeletedCheck(comment.getTaskId());
        var project = projectRepository.findById(task.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(task.getProjectId()));

        if (teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                project.getTeamId(),
                user.getId(),
                List.of(TeamRole.OWNER, TeamRole.ADMIN))) {
            return;
        }

        throw new AccessDeniedException("Only comment author, team owner/admin, or system admin can delete comments");
    }
}
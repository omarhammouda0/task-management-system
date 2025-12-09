package com.taskmanagement.attachment.service;

import com.taskmanagement.attachment.entity.Attachment;
import com.taskmanagement.attachment.repository.AttachmentRepository;
import com.taskmanagement.common.exception.types.Exceptions.*;
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
@Component("attachmentSecurityHelper")
public class SecurityHelper {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AttachmentRepository attachmentRepository;

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

    protected Task taskExistsAndNotDeletedCheck(Long taskId) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (task.getStatus() == TaskStatus.DELETED) {
            throw new TaskNotFoundException(taskId);
        }

        return task;
    }

    protected Attachment attachmentExistsAndNotDeletedCheck(Long attachmentId) {

        return attachmentRepository.findByIdAndNotDeleted(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
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

    protected void canUploadToTask(User user, Task task) {
        canAccessTask(user, task);
    }

    protected void canAccessAttachment(User user, Attachment attachment) {
        if (isSystemAdmin(user)) {
            return;
        }

        var task = taskExistsAndNotDeletedCheck(attachment.getTaskId());
        canAccessTask(user, task);
    }

    protected void canDeleteAttachment(User user, Attachment attachment) {
        if (isSystemAdmin(user)) {
            return;
        }

        if (attachment.getCreatedBy().equals(user.getId())) {
            return;
        }

        var task = taskExistsAndNotDeletedCheck(attachment.getTaskId());
        var project = projectRepository.findById(task.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(task.getProjectId()));

        if (teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                project.getTeamId(),
                user.getId(),
                List.of(TeamRole.OWNER, TeamRole.ADMIN))) {
            return;
        }

        throw new AccessDeniedException("Only uploader, team owner/admin, or system admin can delete attachments");
    }

    protected void validateMaxFilesPerTask(Long taskId, long maxFiles) {
        long currentCount = attachmentRepository.countByTaskIdAndNotDeleted(taskId);
        if (currentCount >= maxFiles) {
            throw new IllegalStateException("Maximum " + maxFiles + " attachments allowed per task");
        }
    }
}
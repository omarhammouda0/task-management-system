package com.taskmanagement.task.service;

import com.taskmanagement.common.exception.types.Exceptions.*;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.project.repository.ProjectRepository;
import com.taskmanagement.task.entity.Task;
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
@Component("taskSecurityHelper")
public class SecurityHelper {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;

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

    protected Project projectExistsAndActiveCheck(Long projectId) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new ProjectNotFoundException(projectId);
        }

        if (project.getStatus ( ) != ProjectStatus.ACTIVE) {
            throw new InvalidProjectStatusException (

                    project.getStatus ( ) ,
                    ProjectStatus.ACTIVE
            );

        }

        return project;
    }

    protected Project projectExistsAndNotDeletedCheck(Long projectId) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new ProjectNotFoundException(projectId);
        }

        return project;
    }

    protected Task taskExistsCheck(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    protected Task taskExistsAndNotDeletedCheck(Long taskId) {
        return taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    protected void validateTaskTitleNotExists(String title, Long projectId) {
        if (taskRepository.existsByTitleIgnoreCaseAndProjectId(title, projectId)) {
            throw new TaskTitleAlreadyExistsException(title, projectId);
        }
    }

    protected void validateTaskTitleNotExistsForUpdate(String title, Long projectId, Long taskId) {
        if (taskRepository.existsByTitleIgnoreCaseAndProjectIdAndIdNot(title, projectId, taskId)) {
            throw new TaskTitleAlreadyExistsException(title, projectId);
        }
    }

    protected void canAccessTask(User user, Task task) {
        if (isSystemAdmin(user)) {
            return;
        }

        var project = projectExistsAndNotDeletedCheck(task.getProjectId());

        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), user.getId())) {
            throw new AccessDeniedException("You must be a team member to access this task");
        }
    }

    protected void canModifyTask(User user, Task task) {
        if (isSystemAdmin(user)) {
            return;
        }

        var project = projectExistsAndNotDeletedCheck(task.getProjectId());

        if (teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                project.getTeamId(),
                user.getId(),
                List.of(TeamRole.OWNER, TeamRole.ADMIN))) {
            return;
        }

        if (task.getAssignedTo() != null && task.getAssignedTo().equals(user.getId())) {
            return;
        }

        throw new AccessDeniedException("You don't have permission to modify this task");
    }

    protected void canDeleteTask(User user, Task task) {
        if (isSystemAdmin(user)) {
            return;
        }

        var project = projectExistsAndNotDeletedCheck(task.getProjectId());

        if (teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                project.getTeamId(),
                user.getId(),
                List.of(TeamRole.OWNER, TeamRole.ADMIN))) {
            return;
        }

        throw new AccessDeniedException("Only team owner/admin or system admin can delete tasks");
    }

    protected void canCreateTaskInProject(User user, Long projectId) {
        if (isSystemAdmin(user)) {
            return;
        }

        var project = projectExistsAndNotDeletedCheck(projectId);

        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), user.getId())) {
            throw new AccessDeniedException("You must be a team member to create tasks in this project");
        }
    }

    protected void canAssignTask(User user, Long projectId, Long assigneeUserId) {
        var project = projectExistsAndNotDeletedCheck(projectId);

        if (isSystemAdmin(user)) {
            return;
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), assigneeUserId)) {
            throw new AccessDeniedException("Can only assign tasks to team members");
        }

        boolean isOwnerOrAdmin = teamMemberRepository.existsByTeamIdAndUserIdAndRoleIn(
                project.getTeamId(),
                user.getId(),
                List.of(TeamRole.OWNER, TeamRole.ADMIN));

        boolean isSelfAssignment = user.getId().equals(assigneeUserId);

        if (!isOwnerOrAdmin && !isSelfAssignment) {
            throw new AccessDeniedException("You can only assign tasks to yourself unless you're a team owner/admin");
        }
    }
}
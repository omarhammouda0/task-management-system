package com.taskmanagement.task.service;

import com.taskmanagement.task.dto.AssignTaskDto;
import com.taskmanagement.task.dto.CreateTaskDto;
import com.taskmanagement.task.dto.TaskResponseDto;
import com.taskmanagement.task.dto.UpdateTaskDto;
import com.taskmanagement.task.enums.TaskStatus;
import com.taskmanagement.task.mapper.TaskMapper;
import com.taskmanagement.task.repository.TaskRepository;
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
public class TaskServiceImplementation implements TaskService {

    private final SecurityHelper securityHelper;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskResponseDto createTask(CreateTaskDto dto) {
        Objects.requireNonNull(dto, "Task creation data must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var project = securityHelper.projectExistsAndActiveCheck(dto.projectId());

        securityHelper.canCreateTaskInProject(currentUser, project.getId());

        String taskTitle = dto.title().trim();
        securityHelper.validateTaskTitleNotExists(taskTitle, project.getId());

        if (dto.assignedTo() != null) {
            var assignee = securityHelper.userExistsAndActiveCheck(dto.assignedTo());
            securityHelper.canAssignTask(currentUser, project.getId(), assignee.getId());
        }

        var task = taskMapper.toEntity(dto);
        task.setCreatedBy(currentUser.getId());

        var savedTask = taskRepository.save(task);

        log.info("Task '{}' (ID: {}) created in project {} by user {} (ID: {})",
                savedTask.getTitle(),
                savedTask.getId(),
                project.getId(),
                currentUser.getEmail(),
                currentUser.getId());

        return taskMapper.toDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long taskId) {
        Objects.requireNonNull(taskId, "Task ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.isSystemAdmin(currentUser)
                ? securityHelper.taskExistsCheck(taskId)
                : securityHelper.taskExistsAndNotDeletedCheck(taskId);

        securityHelper.canAccessTask(currentUser, task);

        return taskMapper.toDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByProject(Long projectId, Pageable pageable) {
        Objects.requireNonNull(projectId, "Project ID must not be null");
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var project = securityHelper.projectExistsAndNotDeletedCheck(projectId);

        securityHelper.canCreateTaskInProject(currentUser, project.getId());

        if (securityHelper.isSystemAdmin(currentUser)) {
            return taskRepository.findByProjectId(projectId, pageable)
                    .map(taskMapper::toDto);
        } else {
            return taskRepository.findByProjectIdAndNotDeleted(projectId, pageable)
                    .map(taskMapper::toDto);
        }
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(Long taskId, UpdateTaskDto dto) {
        Objects.requireNonNull(taskId, "Task ID must not be null");
        Objects.requireNonNull(dto, "Update data must not be null");

        if (dto.title() == null &&
                dto.description() == null &&
                dto.status() == null &&
                dto.priority() == null &&
                dto.dueDate() == null) {
            throw new IllegalStateException("At least one field must be provided for update");
        }

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        securityHelper.canModifyTask(currentUser, task);

        if (dto.title() != null) {
            String trimmedTitle = dto.title().trim();
            if (trimmedTitle.isEmpty()) {
                throw new IllegalArgumentException("Task title cannot be blank");
            }
            securityHelper.validateTaskTitleNotExistsForUpdate(
                    trimmedTitle,
                    task.getProjectId(),
                    taskId
            );
        }

        if (dto.status() != null) {
            validateStatusTransition(task.getStatus(), dto.status());
        }

        taskMapper.updateEntityFromDto ( dto, task);
        task.setUpdatedBy(currentUser.getId());

        var updatedTask = taskRepository.save(task);

        log.info("Task '{}' (ID: {}) updated by user {} (ID: {})",
                updatedTask.getTitle(),
                updatedTask.getId(),
                currentUser.getEmail(),
                currentUser.getId());

        return taskMapper.toDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Objects.requireNonNull(taskId, "Task ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        securityHelper.canDeleteTask(currentUser, task);

        if (task.getStatus() == TaskStatus.DELETED) {
            throw new IllegalStateException("Task is already deleted");
        }

        var oldStatus = task.getStatus();
        task.setStatus(TaskStatus.DELETED);
        task.setUpdatedBy(currentUser.getId());

        taskRepository.save(task);

        log.info("Task '{}' (ID: {}) deleted by user {} (ID: {}) from {} to DELETED",
                task.getTitle(),
                task.getId(),
                currentUser.getEmail(),
                currentUser.getId(),
                oldStatus);
    }

    @Override
    @Transactional
    public TaskResponseDto assignTask(Long taskId, AssignTaskDto dto) {
        Objects.requireNonNull(taskId, "Task ID must not be null");
        Objects.requireNonNull(dto, "Assignment data must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        var assignee = securityHelper.userExistsAndActiveCheck(dto.userId());

        securityHelper.canAssignTask(currentUser, task.getProjectId(), assignee.getId());

        var previousAssignee = task.getAssignedTo();
        task.setAssignedTo(assignee.getId());
        task.setUpdatedBy(currentUser.getId());

        var updatedTask = taskRepository.save(task);

        if (previousAssignee == null) {
            log.info("Task '{}' (ID: {}) assigned to user {} by user {} (ID: {})",
                    updatedTask.getTitle(),
                    updatedTask.getId(),
                    assignee.getEmail(),
                    currentUser.getEmail(),
                    currentUser.getId());
        } else {
            log.info("Task '{}' (ID: {}) reassigned from user {} to user {} by user {} (ID: {})",
                    updatedTask.getTitle(),
                    updatedTask.getId(),
                    previousAssignee,
                    assignee.getEmail(),
                    currentUser.getEmail(),
                    currentUser.getId());
        }

        return taskMapper.toDto(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto unassignTask(Long taskId) {
        Objects.requireNonNull(taskId, "Task ID must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        var task = securityHelper.taskExistsAndNotDeletedCheck(taskId);

        if (task.getAssignedTo() == null) {
            throw new IllegalStateException("Task is already unassigned");
        }

        securityHelper.canModifyTask(currentUser, task);

        var previousAssignee = task.getAssignedTo();
        task.setAssignedTo(null);
        task.setUpdatedBy(currentUser.getId());

        var updatedTask = taskRepository.save(task);

        log.info("Task '{}' (ID: {}) unassigned from user {} by user {} (ID: {})",
                updatedTask.getTitle(),
                updatedTask.getId(),
                previousAssignee,
                currentUser.getEmail(),
                currentUser.getId());

        return taskMapper.toDto(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getMyTasks(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        return taskRepository.findByAssignedToAndNotDeleted(currentUser.getId(), pageable)
                .map(taskMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasksForAdmin(Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable must not be null");

        var currentUser = securityHelper.getCurrentUser();
        securityHelper.isUserActive(currentUser);

        securityHelper.systemAdminCheck(currentUser);

        return taskRepository.findAll(pageable)
                .map(taskMapper::toDto);
    }

    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new IllegalStateException("Task is already in " + currentStatus + " status");
        }

        if (currentStatus == TaskStatus.DELETED) {
            throw new IllegalStateException("Cannot change status of a deleted task");
        }
        if (newStatus == TaskStatus.DELETED) {
            throw new IllegalStateException("Use deleteTask() method to delete a task");
        }

        switch (currentStatus) {
            case TO_DO:
                if (newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.BLOCKED) {
                    throw new IllegalStateException(
                            "Cannot transition from TODO to " + newStatus + ". Allowed: IN_PROGRESS, BLOCKED"
                    );
                }
                break;

            case IN_PROGRESS:
                if (newStatus != TaskStatus.IN_REVIEW &&
                        newStatus != TaskStatus.DONE &&
                        newStatus != TaskStatus.BLOCKED &&
                        newStatus != TaskStatus.TO_DO) {
                    throw new IllegalStateException(
                            "Cannot transition from IN_PROGRESS to " + newStatus
                    );
                }
                break;

            case IN_REVIEW:
                if (newStatus != TaskStatus.DONE &&
                        newStatus != TaskStatus.IN_PROGRESS &&
                        newStatus != TaskStatus.BLOCKED) {
                    throw new IllegalStateException(
                            "Cannot transition from IN_REVIEW to " + newStatus
                    );
                }
                break;

            case DONE:
                if (newStatus != TaskStatus.TO_DO && newStatus != TaskStatus.IN_PROGRESS) {
                    throw new IllegalStateException(
                            "Cannot transition from DONE to " + newStatus + ". Can only reopen to TODO or IN_PROGRESS"
                    );
                }
                break;

            case BLOCKED:
                if (newStatus != TaskStatus.TO_DO && newStatus != TaskStatus.IN_PROGRESS) {
                    throw new IllegalStateException(
                            "Cannot transition from BLOCKED to " + newStatus + ". Can only unblock to TODO or IN_PROGRESS"
                    );
                }
                break;

            default:
                throw new IllegalStateException("Unknown status: " + currentStatus);
        }
    }
}
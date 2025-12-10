package com.taskmanagement.task.mapper;

import com.taskmanagement.project.entity.Project;
import com.taskmanagement.task.dto.CreateTaskDto;
import com.taskmanagement.task.dto.TaskResponseDto;
import com.taskmanagement.task.dto.UpdateTaskDto;
import com.taskmanagement.task.entity.Task;
import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import com.taskmanagement.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {


    public Task toEntity(CreateTaskDto dto, Project project, User assignedUser) {
        if (dto == null) {
            return null;
        }

        return Task.builder()

                .title(dto.title().trim())
                .description(dto.description() != null ? dto.description().trim() : null)
                .project(project)
                .priority(dto.priority() != null ? dto.priority() : TaskPriority.MEDIUM)
                .status(TaskStatus.TO_DO)
                .assignedUser(assignedUser)
                .dueDate(dto.dueDate())
                .build();
    }

    public TaskResponseDto toDto(Task task) {
        if (task == null) {
            return null;
        }

        return new TaskResponseDto(

                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getProjectIdSafe(),
                task.getAssignedToSafe(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getCreatedBy(),
                task.getUpdatedBy(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public void updateEntityFromDto(UpdateTaskDto dto, Task task) {
        if (dto == null || task == null) {
            return;
        }

        if (dto.title() != null) {
            task.setTitle(dto.title().trim());
        }

        if (dto.description() != null) {
            task.setDescription(dto.description().trim());
        }

        if (dto.status() != null) {
            task.setStatus(dto.status());

            if (dto.status() == TaskStatus.DONE && task.getCompletedAt() == null) {
                task.setCompletedAt(java.time.Instant.now());
            }

            if (dto.status() != TaskStatus.DONE && task.getCompletedAt() != null) {
                task.setCompletedAt(null);
            }
        }

        if (dto.priority() != null) {
            task.setPriority(dto.priority());
        }

        if (dto.dueDate() != null) {
            task.setDueDate(dto.dueDate());
        }
    }


    public void updateAssignedUser(Task task, User assignedUser) {
        task.setAssignedUser(assignedUser);
    }
}
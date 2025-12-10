package com.taskmanagement.task.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
import com.taskmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TO_DO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "project_id", nullable = false, insertable = false, updatable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedUser;

    @Column(name = "assigned_to", insertable = false, updatable = false)
    private Long assignedTo;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;


    public Long getProjectIdSafe() {
        if (projectId != null) {
            return projectId;
        }
        return project != null ? project.getId() : null;
    }


    public Long getAssignedToSafe() {
        if (assignedTo != null) {
            return assignedTo;
        }
        return assignedUser != null ? assignedUser.getId() : null;
    }
}
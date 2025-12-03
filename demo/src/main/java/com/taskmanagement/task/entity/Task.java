package com.taskmanagement.task.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.task.enums.TaskPriority;
import com.taskmanagement.task.enums.TaskStatus;
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

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;


}
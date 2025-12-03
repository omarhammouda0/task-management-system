package com.taskmanagement.task.controller;

import com.taskmanagement.task.dto.AssignTaskDto;
import com.taskmanagement.task.dto.CreateTaskDto;
import com.taskmanagement.task.dto.TaskResponseDto;
import com.taskmanagement.task.dto.UpdateTaskDto;
import com.taskmanagement.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody CreateTaskDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(dto));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByProject(
            @PathVariable Long projectId,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, pageable));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskDto dto) {
        return ResponseEntity.ok(taskService.updateTask(taskId, dto));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponseDto> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskDto dto) {
        return ResponseEntity.ok(taskService.assignTask(taskId, dto));
    }

    @PostMapping("/{taskId}/unassign")
    public ResponseEntity<TaskResponseDto> unassignTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.unassignTask(taskId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<Page<TaskResponseDto>> getMyTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getMyTasks(pageable));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasksForAdmin(Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasksForAdmin(pageable));
    }
}

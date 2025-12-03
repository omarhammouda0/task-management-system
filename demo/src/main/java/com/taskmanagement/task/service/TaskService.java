package com.taskmanagement.task.service;

import com.taskmanagement.task.dto.AssignTaskDto;
import com.taskmanagement.task.dto.CreateTaskDto;
import com.taskmanagement.task.dto.TaskResponseDto;
import com.taskmanagement.task.dto.UpdateTaskDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {


    TaskResponseDto createTask(CreateTaskDto dto);

    TaskResponseDto getTaskById(Long taskId);

    Page<TaskResponseDto> getTasksByProject(Long projectId, Pageable pageable);

    TaskResponseDto updateTask(Long taskId, UpdateTaskDto dto);

    void deleteTask(Long taskId);


    TaskResponseDto assignTask(Long taskId, AssignTaskDto dto);

    TaskResponseDto unassignTask(Long taskId);


    Page<TaskResponseDto> getMyTasks(Pageable pageable);


    Page<TaskResponseDto> getAllTasksForAdmin(Pageable pageable);
}
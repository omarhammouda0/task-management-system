package com.taskmanagement.project.service;

import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponseDto createProject(CreateProjectDto requestDto);

    ProjectResponseDto restoreProject(Long projectId);

    ProjectResponseDto activateProject(Long projectId);

    void archiveProject(Long projectId);

    ProjectResponseDto transferProject(Long projectId, Long newTeamId);

    Page<ProjectResponseDto> getProjectsByTeam (Pageable pageable, Long teamId);

    ProjectResponseDto getProjectById(Long projectId);

    Page<ProjectResponseDto> getProjectsByOwner (Pageable pageable, Long ownerId);

    ProjectResponseDto updateProject(Long projectId, UpdateProjectDto requestDto);

    void deleteProject(Long projectId);

    Page<ProjectResponseDto> getAllProjectsForAdmin(Pageable pageable);


}


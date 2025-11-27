package com.taskmanagement.project.service;

import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.project.mapper.ProjectMapper;
import com.taskmanagement.project.repository.ProjectRepository;
import com.taskmanagement.user.enums.Role;
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

public class ProjectServiceImplementation implements ProjectService {

    private final SecurityHelper securityHelper;
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;


    @Override
    @Transactional

    public ProjectResponseDto createProject(CreateProjectDto requestDto) {

        Objects.requireNonNull(requestDto , "The project creation data must not be null");

        var currentUser = securityHelper.getCurrentUser();
        var team = securityHelper.teamExistsAndActiveCheck(requestDto.teamId ());
        String projectName = requestDto.name().trim();

        securityHelper.isUserActive(currentUser);
        securityHelper.isOwner(currentUser.getId() , team.getId());
        securityHelper.validateProjectNameNotExists ( projectName, team.getId() );
        securityHelper.dateValidation ( requestDto.startDate () , requestDto.endDate () );

        var status = securityHelper.statusValidation ( requestDto.status() );

        var toSaveProject = projectMapper.toEntity ( requestDto );

        toSaveProject.setStatus ( status );
        toSaveProject.setCreatedBy (  currentUser.getId() );

        var savedProject = projectRepository.save(toSaveProject );

        log.info ( "Project with id {} created by user with id {}" ,
                savedProject.getId ( ) , currentUser.getId ( ) );

        return projectMapper.toDto ( savedProject );


    }


    @Override
    @Transactional

    public ProjectResponseDto restoreProject(Long projectId) {

        Objects.requireNonNull(projectId, "The project id must not be null");

        var currentUser = securityHelper.getCurrentUser ( );
        var project = securityHelper.projectExistsCheck ( projectId );
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamId () );
        var newStatus = ProjectStatus.PLANNED;

        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );
        securityHelper.validateStatusValidation ( project.getStatus () , newStatus );
        securityHelper.validateProjectNameNotExistsForUpdate ( project.getName () , team.getId () , projectId );
        securityHelper.teamActiveCheck (  project.getTeamId () );

        project.setStatus ( newStatus);
        project.setUpdatedBy (   currentUser.getId () );

        var restoredProject = projectRepository.save ( project );

        log.info("Project '{}' (ID: {}) restored by admin {} (ID: {}) from DELETED to PLANNED",
                restoredProject.getName(),
                restoredProject.getId(),
                currentUser.getEmail(),
                currentUser.getId());

        return projectMapper.toDto ( restoredProject );

    }

    @Override
    public ProjectResponseDto activateProject(Long projectId) {
        return null;
    }

    @Override
    public void archiveProject(Long projectId) {

    }

    @Override
    public ProjectResponseDto transferProject(Long projectId , Long newTeamId) {
        return null;
    }

    @Override
    public Page<ProjectResponseDto> getProjectsByTeam(Pageable pageable , Long teamId) {
        return null;
    }

    @Override
    public ProjectResponseDto getProjectById(Long projectId) {
        return null;
    }

    @Override
    public Page<ProjectResponseDto> getProjectsByOwner(Pageable pageable , Long ownerId) {
        return null;
    }

    @Override
    public ProjectResponseDto updateProject(Long projectId , UpdateProjectDto requestDto) {
        return null;
    }

    @Override
    public void deleteProject(Long projectId) {

    }

    @Override
    public Page<ProjectResponseDto> getAllProjectsForAdmin(Pageable pageable) {
        return null;
    }
}

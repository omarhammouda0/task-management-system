package com.taskmanagement.project.service;

import com.taskmanagement.common.exception.types.Exceptions.AccessDeniedException;
import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.project.mapper.ProjectMapper;
import com.taskmanagement.project.repository.ProjectRepository;

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

        Objects.requireNonNull ( requestDto , "The project creation data must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        var team = securityHelper.teamExistsAndActiveCheck ( requestDto.teamId ( ) );
        String projectName = requestDto.name ( ).trim ( );

        securityHelper.isUserActive ( currentUser );
        securityHelper.isOwner ( currentUser.getId ( ) , team.getId ( ) );
        securityHelper.validateProjectNameNotExists ( projectName , team.getId ( ) );
        securityHelper.dateValidation ( requestDto.startDate ( ) , requestDto.endDate ( ) );

        var status = securityHelper.statusValidation ( requestDto.status ( ) );

        var toSaveProject = projectMapper.toEntity ( requestDto );

        toSaveProject.setStatus ( status );
        toSaveProject.setCreatedBy ( currentUser.getId ( ) );

        var savedProject = projectRepository.save ( toSaveProject );

        log.info ( "Project with id {} created by user with id {}" ,
                savedProject.getId ( ) , currentUser.getId ( ) );

        return projectMapper.toDto ( savedProject );


    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long projectId) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        var project = securityHelper.projectExistsCheckAndRetrievableCheckUponRole ( currentUser , projectId );

        if (!securityHelper.isSystemAdmin ( currentUser )) {
            securityHelper.teamActiveCheck ( project.getTeamId ( ) );
        }

        securityHelper.isMemberInTeamOrSystemAdmin ( project.getTeamId ( ) , currentUser );

        return projectMapper.toDto ( project );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getProjectsByTeam(Pageable pageable , Long teamId) {

        Objects.requireNonNull ( teamId, "The team id must not be null" );
        Objects.requireNonNull ( pageable , "The pageable must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );

        securityHelper.isUserActive ( currentUser );
        securityHelper.teamExistsAndActiveCheck ( teamId );
        securityHelper.isMemberInTeamOrSystemAdmin ( teamId , currentUser );

        if (securityHelper.isSystemAdmin (  currentUser ) ) {

            return projectRepository.findByTeamIdForAdmin ( teamId , pageable )
                    .map ( projectMapper::toDto );
        }

        return projectRepository.findByTeamId ( teamId , pageable )
                .map ( projectMapper::toDto );

    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getProjectsByOwner(Pageable pageable , Long ownerId) {

        Objects.requireNonNull ( ownerId , "Owner ID must not be null" );
        Objects.requireNonNull ( pageable , "The pageable must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        if (securityHelper.isSystemAdmin ( currentUser )) {
            return projectRepository.findByOwnerIdForAdmin ( ownerId , pageable )
                    .map ( projectMapper::toDto );
        }

        if (!securityHelper.isSelfOperation ( currentUser.getId ( ) , ownerId )) {
            throw new AccessDeniedException ( "Access denied: You can only view your own projects." );
        }

        return projectRepository.findByOwnerId ( ownerId , pageable )
                .map ( projectMapper::toDto );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getAllProjectsForAdmin(Pageable pageable) {

        Objects.requireNonNull ( pageable , "The pageable must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );

        return projectRepository.findAll (pageable)
                .map ( projectMapper::toDto );

    }

    @Override
    @Transactional
    public ProjectResponseDto restoreProject(Long projectId) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        var project = securityHelper.projectExistsCheck ( projectId );
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamId ( ) );
        var newStatus = ProjectStatus.PLANNED;

        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );
        securityHelper.validateStatusValidation ( project.getStatus ( ) , newStatus );
        securityHelper.validateProjectNameNotExistsForUpdate ( project.getName ( ) , team.getId ( ) , projectId );
        securityHelper.teamActiveCheck ( project.getTeamId ( ) );

        project.setStatus ( newStatus );
        project.setUpdatedBy ( currentUser.getId ( ) );

        var restoredProject = projectRepository.save ( project );

        log.info ( "Project '{}' (ID: {}) restored by admin {} (ID: {}) from DELETED to PLANNED" ,
                restoredProject.getName ( ) ,
                restoredProject.getId ( ) ,
                currentUser.getEmail ( ) ,
                currentUser.getId ( ) );

        return projectMapper.toDto ( restoredProject );

    }

    @Override
    @Transactional
    public ProjectResponseDto activateProject(Long projectId) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        var project = securityHelper.projectExistsCheck ( projectId );
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamId ( ) );
        var oldStatus = project.getStatus ( );
        var newStatus = ProjectStatus.ACTIVE;

        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );
        securityHelper.validateStatusValidation ( project.getStatus ( ) , newStatus );
        securityHelper.validateProjectNameNotExistsForUpdate ( project.getName ( ) , team.getId ( ) , projectId );
        securityHelper.dateValidation ( project.getStartDate ( ) , project.getEndDate ( ) );

        project.setStatus ( newStatus );
        project.setUpdatedBy ( currentUser.getId ( ) );

        var activatedProject = projectRepository.save ( project );

        log.info ( "Project '{}' (ID: {}) activated by admin {} (ID: {}) from {} to ACTIVE" ,
                activatedProject.getName ( ) ,
                activatedProject.getId ( ) ,
                currentUser.getEmail ( ) ,
                currentUser.getId ( ) ,
                oldStatus );

        return projectMapper.toDto ( activatedProject );
    }

    @Override
    @Transactional
    public void archiveProject(Long projectId) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        var project = securityHelper.projectExistsCheck ( projectId );
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamId ( ) );
        var oldStatus = project.getStatus ( );
        var newStatus = ProjectStatus.ARCHIVED;

        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );
        securityHelper.validateStatusValidation ( project.getStatus ( ) , newStatus );

        project.setStatus ( newStatus );
        project.setUpdatedBy ( currentUser.getId ( ) );
        var archivedProject = projectRepository.save ( project );

        log.info ( "Project '{}' (ID: {}) archived by admin {} (ID: {}) from {} to ARCHIVED" ,
                archivedProject.getName ( ) ,
                archivedProject.getId ( ) ,
                currentUser.getEmail ( ) ,
                currentUser.getId ( ) ,
                oldStatus );

    }

    @Override
    public ProjectResponseDto transferProject(Long projectId , Long newTeamId) {
        return null;
    }


    @Override
    public ProjectResponseDto updateProject(Long projectId , UpdateProjectDto requestDto) {
        return null;
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );

        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );

        var project = securityHelper.projectExistsCheck ( projectId );

        var oldStatus = project.getStatus ( );
        var newStatus = ProjectStatus.DELETED;

        securityHelper.validateStatusValidation ( project.getStatus ( ) , newStatus );

        project.setStatus ( newStatus );
        project.setUpdatedBy ( currentUser.getId ( ) );

        projectRepository.save ( project );

        log.info ( "Project '{}' (ID: {}) deleted by admin {} (ID: {}) from {} to DELETED" ,
                project.getName ( ) ,
                project.getId ( ) ,
                currentUser.getEmail ( ) ,
                currentUser.getId ( ) ,
                oldStatus );

    }





}


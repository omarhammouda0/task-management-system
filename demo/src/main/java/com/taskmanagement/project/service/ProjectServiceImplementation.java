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

        Objects.requireNonNull(requestDto, "The project creation data must not be null");

        var currentUser = securityHelper.getCurrentUser();
        var team = securityHelper.teamExistsAndActiveCheck(requestDto.teamId());
        String projectName = requestDto.name().trim();

        securityHelper.isUserActive(currentUser);
        securityHelper.isOwner(currentUser.getId(), team.getId());
        securityHelper.validateProjectNameNotExists(projectName, team.getId());
        securityHelper.dateValidation(requestDto.startDate(), requestDto.endDate());

        var status = securityHelper.statusValidation(requestDto.status());

        var toSaveProject = projectMapper.toEntity(requestDto, team);
        toSaveProject.setStatus(status);
        toSaveProject.setCreatedBy(currentUser.getId());

        var savedProject = projectRepository.save(toSaveProject);

        log.info("Project with id {} created by user with id {}",
                savedProject.getId(), currentUser.getId());

        return projectMapper.toDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long projectId) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        var project = securityHelper.projectExistsCheckAndRetrievableCheckUponRole ( currentUser , projectId );

        if (!securityHelper.isSystemAdmin ( currentUser )) {
            securityHelper.teamActiveCheck ( project.getTeamIdSafe() );
        }

        securityHelper.isMemberInTeamOrSystemAdmin ( project.getTeamIdSafe() , currentUser );

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
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamIdSafe() );
        var newStatus = ProjectStatus.PLANNED;

        securityHelper.isUserActive ( currentUser );
        securityHelper.isSystemAdmin ( currentUser );
        securityHelper.validateStatusValidation ( project.getStatus ( ) , newStatus );
        securityHelper.validateProjectNameNotExistsForUpdate ( project.getName ( ) , team.getId ( ) , projectId );
        securityHelper.teamActiveCheck ( project.getTeamIdSafe() );

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
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamIdSafe() );
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
        var team = securityHelper.teamExistsAndActiveCheck ( project.getTeamIdSafe() );
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
    @Transactional
    public ProjectResponseDto transferProject(Long projectId , Long newTeamId) {

        Objects.requireNonNull(projectId, "The project id must not be null");
        Objects.requireNonNull(newTeamId, "The new team id must not be null");

        var currentUser = securityHelper.getCurrentUser();

        securityHelper.isUserActive(currentUser);
        securityHelper.systemAdminCheck(currentUser);

        var project = securityHelper.projectExistsAndNotDeletedCheck(projectId);
        var oldTeamId = project.getTeamIdSafe();

        var newTeam = securityHelper.teamExistsAndActiveCheck(newTeamId);
        securityHelper.notSameTeamCheck(oldTeamId, newTeamId);
        securityHelper.validateProjectNameNotExists(project.getName(), newTeamId);

        project.setTeam(newTeam);
        project.setUpdatedBy(currentUser.getId());

        var transferredProject = projectRepository.save(project);

        log.info("Project '{}' (ID: {}) transferred by admin {} (ID: {}) from team {} to team {}",
                transferredProject.getName(),
                transferredProject.getId(),
                currentUser.getEmail(),
                currentUser.getId(),
                oldTeamId,
                newTeamId);

        return projectMapper.toDto(transferredProject);
    }


    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long projectId , UpdateProjectDto dto) {

        Objects.requireNonNull ( projectId , "The project id must not be null" );
        Objects.requireNonNull ( dto , "The project update data must not be null" );

        if (dto.name ( ) == null &&
                dto.description ( ) == null &&
                dto.status ( ) == null &&
                dto.startDate ( ) == null &&
                dto.endDate ( ) == null)

            throw new IllegalStateException ( "At least one field must be provided for update" );

        var currentUser = securityHelper.getCurrentUser ( );
        securityHelper.isUserActive ( currentUser );

        var project = securityHelper.projectExistsAndNotDeletedCheck ( projectId );

        if (!securityHelper.isSystemAdmin ( currentUser ) &&
                !securityHelper.isTeamOwnerOrTeamAdmin ( currentUser.getId ( ) , project.getTeamIdSafe() ))

            throw new AccessDeniedException ( "Access denied:" +
                    " Only system admin , team owner and team admin" +
                    " have permission to update this project." );

        securityHelper.teamActiveCheck ( project.getTeamIdSafe() );

        if (dto.name () != null) {

            var trimmedName = dto.name ( ).trim ( );

            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException ("Project name cannot be blank");
            }
            securityHelper.validateProjectNameNotExistsForUpdate(
                    trimmedName,
                    project.getTeamIdSafe(),
                    projectId
            );
        }

        if (dto.status() != null)
            securityHelper.validateStatusValidation(project.getStatus(), dto.status());



        if (dto.startDate() != null || dto.endDate() != null) {
            var finalStartDate = dto.startDate ( ) != null ? dto.startDate ( ) : project.getStartDate ( );
            var finalEndDate = dto.endDate ( ) != null ? dto.endDate ( ) : project.getEndDate ( );
            securityHelper.dateValidation ( finalStartDate , finalEndDate );
        }



        projectMapper.updateEntityFromDto ( dto , project );

        project.setUpdatedBy ( currentUser.getId ( ) );
        var updatedProject = projectRepository.save ( project );

        log.info ( "Project '{}' (ID: {}) updated by user {} (ID: {})" ,
                updatedProject.getName ( ) ,
                updatedProject.getId ( ) ,
                currentUser.getEmail ( ) ,
                currentUser.getId ( ) );

        return projectMapper.toDto ( updatedProject );

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


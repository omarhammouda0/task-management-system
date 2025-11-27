package com.taskmanagement.project.controller;

import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/projects")


public class ProjectController {

    private  final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject
            (@Valid @RequestBody CreateProjectDto dto ){

        return ResponseEntity.status( HttpStatus.CREATED).body ( projectService.createProject( dto ) );

    }

    @PostMapping ("/{projectId}/restore" )
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <ProjectResponseDto> restoreProject
            ( @PathVariable Long projectId ){

        return ResponseEntity.status( HttpStatus.OK).body ( projectService.restoreProject( projectId ) );

    }

    @PostMapping ("/{projectId}/activate" )
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <ProjectResponseDto> activateProject
            ( @PathVariable Long projectId ){

        return ResponseEntity.status( HttpStatus.OK).body ( projectService.activateProject( projectId ) );

    }

    @PostMapping("/{projectId}/archive" )
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity <Void> archiveProject
            ( @PathVariable Long projectId ){

        projectService.archiveProject( projectId );
        return ResponseEntity.status( HttpStatus.NO_CONTENT).build ( );

    }

}

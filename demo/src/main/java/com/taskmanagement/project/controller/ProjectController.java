package com.taskmanagement.project.controller;

import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.TransferProjectDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import com.taskmanagement.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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


    @PostMapping("/{projectId}/transfer")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<ProjectResponseDto> transferProject(
            @PathVariable Long projectId , @Valid @RequestBody TransferProjectDto transferProjectDto
    ) {
        return ResponseEntity.ok ( projectService
                .transferProject ( projectId , transferProjectDto.getNewTeamId ( ) ) );

    }


    @GetMapping("/{projectId}" )

    public ResponseEntity <ProjectResponseDto> getProjectById
            ( @PathVariable Long projectId ){

        return ResponseEntity.status( HttpStatus.OK).body ( projectService.getProjectById( projectId ) );

    }

    @GetMapping ("/owner/{ownerId}" )

    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByOwner
            ( @PathVariable Long ownerId ,
              @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            )  {

        return ResponseEntity.ok ( projectService.getProjectsByOwner ( pageable, ownerId ) );

    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<ProjectResponseDto>> getProjectsByTeam(
            @PathVariable Long teamId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.getProjectsByTeam(pageable, teamId));
    }

    @GetMapping("/admin/all")
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<Page<ProjectResponseDto>> getAllProjectsForAdmin(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.getAllProjectsForAdmin (pageable));
    }

    @PatchMapping ("/{projectId}" )

    public ResponseEntity<ProjectResponseDto> updateProject
            (@PathVariable Long projectId , @Valid @RequestBody UpdateProjectDto dto ){

        return ResponseEntity.ok ( projectService.updateProject( projectId ,dto ) ) ;

    }


    @DeleteMapping ("/delete/{projectId}" )
    @PreAuthorize ( "hasRole('ADMIN')" )

    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {

        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();

    }

}

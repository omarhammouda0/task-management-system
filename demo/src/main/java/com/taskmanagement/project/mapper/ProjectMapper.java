package com.taskmanagement.project.mapper;


import com.taskmanagement.project.dto.CreateProjectDto;
import com.taskmanagement.project.dto.ProjectResponseDto;
import com.taskmanagement.project.dto.UpdateProjectDto;
import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.team.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {


    public Project toEntity(CreateProjectDto dto, Team team) {
        return Project.builder()
                .team(team)
                .name(dto.name().trim())
                .description(dto.description() == null ? null : dto.description().trim())
                .status(dto.status() == null ? ProjectStatus.PLANNED : dto.status())
                .startDate(dto.startDate() == null ? null : dto.startDate())
                .endDate(dto.endDate() == null ? null : dto.endDate())
                .build();
    }

    public ProjectResponseDto toDto(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getTeamIdSafe(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }


    public ProjectResponseDto toDtoFromEntity(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getTeamIdSafe(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public void updateEntityFromDto(UpdateProjectDto dto, Project project) {
        if (dto.name() != null) {
            project.setName(dto.name().trim());
        }
        if (dto.description() != null) {
            project.setDescription(dto.description().trim());
        }
        if (dto.status() != null) {
            project.setStatus(dto.status());
        }
        if (dto.startDate() != null) {
            project.setStartDate(dto.startDate());
        }
        if (dto.endDate() != null) {
            project.setEndDate(dto.endDate());
        }
    }

}

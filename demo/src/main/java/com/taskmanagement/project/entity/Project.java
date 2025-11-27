package com.taskmanagement.project.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.project.enums.ProjectStatus;
import com.taskmanagement.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table (name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Project extends BaseEntity {

    @Column (nullable = false , length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column (name = "team_id" , nullable = false)
    private Long teamId;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column (name = "start_date")
    private Instant startDate;

    @Column (name = "end_date")
    private Instant endDate;

    @Column (nullable = false , name = "created_by")
    private Long createdBy;

}



package com.taskmanagement.team.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.team.enums.TeamStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Team extends BaseEntity {

    @Column (nullable = false , length = 100)
    private String name;

    @Column (length = 500)
    private String description;

    @Column (nullable = false , name = "owner_id")
    private Long ownerId;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamStatus status = TeamStatus.ACTIVE;

}

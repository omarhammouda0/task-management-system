package com.taskmanagement.team.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id" , "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TeamMember  extends BaseEntity {

    @Column (name = "team_id" , nullable = false)
    private Long teamId;

    @Column (name = "user_id" , nullable = false)
    private Long userId;

    @Column (nullable = false )
    @Enumerated(EnumType.STRING)
    private TeamRole role = TeamRole.MEMBER;

    @Column (nullable = false)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private TeamMemberStatus status = TeamMemberStatus.ACTIVE;

    @Column (name = "joined_at" , nullable = false)
    private Instant joinedAt;


}

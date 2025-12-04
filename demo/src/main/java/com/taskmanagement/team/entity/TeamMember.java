package com.taskmanagement.team.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import com.taskmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "team_id", nullable = false, insertable = false, updatable = false)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamRole role = TeamRole.MEMBER;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamMemberStatus status = TeamMemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private User updater;
}
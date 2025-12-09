package com.taskmanagement.team.entity;

import com.taskmanagement.common.entity.BaseEntity;
import com.taskmanagement.team.enums.TeamStatus;
import com.taskmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "owner_id", nullable = false, insertable = false, updatable = false)
    private Long ownerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamStatus status = TeamStatus.ACTIVE;


}
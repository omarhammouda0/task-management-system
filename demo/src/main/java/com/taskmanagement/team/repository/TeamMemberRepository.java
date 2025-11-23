package com.taskmanagement.team.repository;

import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamRole;
import org.antlr.v4.runtime.misc.MultiMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {


    boolean existsByTeamIdAndUserId (Long teamId , Long userId);

    boolean existsByTeamIdAndUserIdAndRoleIn(Long teamId , Long userId , Collection<TeamRole> roles);


    boolean existsByTeamIdAndUserIdAndRole(Long teamId , Long userId , TeamRole teamRole);

    List <TeamMember> findByTeamId(Long teamId);


}

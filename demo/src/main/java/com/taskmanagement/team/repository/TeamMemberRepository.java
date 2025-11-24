package com.taskmanagement.team.repository;

import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.entity.TeamMember;
import com.taskmanagement.team.enums.TeamMemberStatus;
import com.taskmanagement.team.enums.TeamRole;
import org.antlr.v4.runtime.misc.MultiMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {


    boolean existsByTeamIdAndUserId (Long teamId , Long userId);

    boolean existsByTeamIdAndUserIdAndRoleIn(Long teamId , Long userId , Collection<TeamRole> roles);


    boolean existsByTeamIdAndUserIdAndRole(Long teamId , Long userId , TeamRole teamRole);

    List <TeamMember> findByTeamId(Long teamId);

    @Query ("select tm from TeamMember tm " +
            "where tm.teamId = :teamId and tm.status = 'ACTIVE' ")

    Page <TeamMember> findByTeamIdAndStatusActive
            (@Param ( "teamId" ) Long teamId  , Pageable pageable);


    Optional <TeamMember> findByTeamIdAndUserId(Long teamId , Long userId);


    @Query("SELECT CASE WHEN COUNT(tm) <= 1 THEN true ELSE false END " +
            "FROM TeamMember tm " +
            "WHERE tm.teamId = :teamId " +
            "AND tm.role = 'OWNER' " +
            "AND tm.status = 'ACTIVE'")

    boolean isLastOwner(@Param("teamId") Long teamId);


    @Query (" SELECT CASE WHEN COUNT(tm) <= 1 THEN true ELSE false END " +
            "FROM TeamMember tm " +
            "WHERE tm.teamId = :teamId " +
            "AND tm.status = 'ACTIVE' ")

    boolean isLastActiveTeamMember (@Param ( "teamId" ) Long teamId);


    Long countByTeamId(Long teamId);

    @Query (" SELECT COUNT (tm) " +
            "FROM TeamMember tm " +
            "WHERE tm.teamId = :teamId " +
            "AND tm.status = 'ACTIVE' ")

    Long countByTeamIdAndStatusActive (@Param ( "teamId" ) Long teamId);

    boolean existsByTeamId(Long teamId);
}

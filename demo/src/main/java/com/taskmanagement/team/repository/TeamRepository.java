package com.taskmanagement.team.repository;


import com.taskmanagement.team.entity.Team;
import com.taskmanagement.team.enums.TeamStatus;
import org.antlr.v4.runtime.misc.MultiMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {



    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String teamName , Long teamId);

    @Query("SELECT t FROM Team t WHERE t.id IN " +
            "(SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId AND tm.status = 'ACTIVE') " +
            "AND t.status = 'ACTIVE'")

    Page<Team> findActiveTeamsByUserId (@Param ( "userId" ) Long userId ,
                                        Pageable pageable);

    Page<Team> findByOwnerId(Long ownerId , Pageable pageable);

    Optional <Team> findByNameIgnoreCase (String name );

    @Query ("SELECT t FROM Team t WHERE t.ownerId = :ownerId AND t.status = 'ACTIVE'")
    Page <Team> findByOwnerIdAndStatusActive ( @Param ( "ownerId" ) Long ownerId ,  Pageable pageable);


}

package com.taskmanagement.project.repository;

import com.taskmanagement.project.entity.Project;
import com.taskmanagement.project.enums.ProjectStatus;
import org.antlr.v4.runtime.misc.MultiMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByNameIgnoreCaseAndTeamId(String name , Long teamId);

    @Query ("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Project p " +
            "WHERE p.teamId = :teamId " +
            "AND LOWER(p.name) = LOWER(:teamName) " +
            "AND p.id <> :projectId")

    boolean existsByTeamIdAndNameIgnoreCaseAndIdNot(@Param ( "teamId" ) Long teamId ,
                                                    @Param ( "teamName" ) String name ,
                                                    @Param ( "projectId" ) Long id);

    @Query ("SELECT p FROM Project p WHERE p.id = :projectId AND p.status = 'ACTIVE'")
    Optional<Project> findByIdAndStatusActive(@Param ( "projectId" ) Long projectId);

    @Query("SELECT p FROM Project p WHERE p.id = :projectId AND p.status = com.taskmanagement.project.enums.ProjectStatus.ACTIVE")
    boolean existsByIdAndStatusActive(Long projectId);


    @Query ("select p from Project p " +
            "join TeamMember tm " +
            " on tm.teamId = p.teamId " +
            "where tm.userId = :ownerId" +
            " and p.status != com.taskmanagement.project.enums.ProjectStatus.DELETED " +
            "and tm.role ='OWNER'")

    Page<Project> findByOwnerId (@Param("ownerId") Long ownerId, Pageable pageable);


    @Query("select p from Project p " +
            "join TeamMember tm " +
            "on tm.teamId = p.teamId " +
            "where tm.userId = :ownerId " +
            "and tm.role ='OWNER'")

    Page<Project> findByOwnerIdForAdmin (
            @Param("ownerId") Long ownerId, Pageable pageable
    );

    @Query ("select p from Project p " +
            "where p.teamId = :teamId " )

    Page <Project> findByTeamIdForAdmin (@Param ("teamId") Long teamId , Pageable pageable);


    @Query ("select p from Project p " +
            "where p.teamId = :teamId " +
            "and p.status != com.taskmanagement.project.enums.ProjectStatus.DELETED" )

    Page <Project> findByTeamId ( Long teamId , Pageable pageable );
}

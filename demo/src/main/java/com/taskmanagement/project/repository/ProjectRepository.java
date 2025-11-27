package com.taskmanagement.project.repository;

import com.taskmanagement.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}

package com.taskmanagement.task.repository;

import com.taskmanagement.task.entity.Task;
import com.taskmanagement.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {


    boolean existsByTitleIgnoreCaseAndProjectId(String title, Long projectId);


    boolean existsByTitleIgnoreCaseAndProjectIdAndIdNot(String title, Long projectId, Long taskId);


    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.status != 'DELETED'")
    Optional<Task> findByIdAndNotDeleted(@Param("taskId") Long taskId);


    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.status != 'DELETED'")
    Page<Task> findByProjectIdAndNotDeleted(@Param("projectId") Long projectId, Pageable pageable);


    Page<Task> findByProjectId(Long projectId, Pageable pageable);


    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId AND t.status != 'DELETED'")
    Page<Task> findByAssignedToAndNotDeleted(@Param("userId") Long userId, Pageable pageable);


}
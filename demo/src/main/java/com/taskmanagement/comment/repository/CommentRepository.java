package com.taskmanagement.comment.repository;

import com.taskmanagement.comment.entity.Comment;
import com.taskmanagement.comment.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.status <> com.taskmanagement.comment.enums.CommentStatus.DELETED")
    Optional<Comment> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT c FROM Comment c WHERE c.taskId = :taskId AND c.status <> com.taskmanagement.comment.enums.CommentStatus.DELETED")
    Page<Comment> findByTaskIdAndNotDeleted(@Param("taskId") Long taskId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.taskId = :taskId")
    Page<Comment> findByTaskId(@Param("taskId") Long taskId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = :userId AND c.status <> com.taskmanagement.comment.enums.CommentStatus.DELETED")
    Page<Comment> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.createdBy = :userId AND c.status <> com.taskmanagement.comment.enums.CommentStatus.DELETED")
    Page<Comment> findByCreatedByAndNotDeleted(@Param("userId") Long userId, Pageable pageable);
}
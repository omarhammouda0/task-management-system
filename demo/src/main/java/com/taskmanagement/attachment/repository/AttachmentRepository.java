package com.taskmanagement.attachment.repository;

import com.taskmanagement.attachment.entity.Attachment;
import com.taskmanagement.attachment.enums.AttachmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.status <> com.taskmanagement.attachment.enums.AttachmentStatus.DELETED")
    Optional<Attachment> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT a FROM Attachment a WHERE a.taskId = :taskId AND a.status <> com.taskmanagement.attachment.enums.AttachmentStatus.DELETED")
    Page<Attachment> findByTaskIdAndNotDeleted(@Param("taskId") Long taskId, Pageable pageable);

    @Query("SELECT a FROM Attachment a WHERE a.taskId = :taskId")
    Page<Attachment> findByTaskId(@Param("taskId") Long taskId, Pageable pageable);

    @Query("SELECT a FROM Attachment a WHERE a.userId = :userId AND a.status <> com.taskmanagement.attachment.enums.AttachmentStatus.DELETED")
    Page<Attachment> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Attachment a WHERE a.createdBy = :userId AND a.status <> com.taskmanagement.attachment.enums.AttachmentStatus.DELETED")
    Page<Attachment> findByCreatedByAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.taskId = :taskId AND a.status <> com.taskmanagement.attachment.enums.AttachmentStatus.DELETED")
    long countByTaskIdAndNotDeleted(@Param("taskId") Long taskId);
}
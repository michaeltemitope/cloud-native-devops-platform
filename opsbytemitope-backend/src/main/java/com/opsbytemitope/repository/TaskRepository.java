package com.opsbytemitope.repository;

import com.opsbytemitope.entity.Task;
import com.opsbytemitope.entity.TaskPriority;
import com.opsbytemitope.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Task entity operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.status NOT IN ('DONE', 'CANCELLED')")
    long countOpenTasksByAssignee(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority = :priority ORDER BY t.createdAt DESC")
    List<Task> findByProjectIdAndPriority(@Param("projectId") Long projectId, @Param("priority") TaskPriority priority);

    @Query("SELECT t FROM Task t WHERE t.project.owner.id = :ownerId AND t.status = :status")
    List<Task> findByProjectOwnerAndStatus(@Param("ownerId") Long ownerId, @Param("status") TaskStatus status);
}

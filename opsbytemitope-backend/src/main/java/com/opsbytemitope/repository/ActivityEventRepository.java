package com.opsbytemitope.repository;

import com.opsbytemitope.entity.ActivityEvent;
import com.opsbytemitope.entity.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ActivityEvent entity operations.
 */
@Repository
public interface ActivityEventRepository extends JpaRepository<ActivityEvent, Long> {

    Page<ActivityEvent> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    Page<ActivityEvent> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);

    List<ActivityEvent> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @Query("SELECT ae FROM ActivityEvent ae WHERE ae.actor.id = :actorId AND ae.createdAt >= :since ORDER BY ae.createdAt DESC")
    List<ActivityEvent> findRecentByActor(@Param("actorId") Long actorId, @Param("since") LocalDateTime since);

    @Query("SELECT ae FROM ActivityEvent ae WHERE ae.project.id = :projectId AND ae.eventType = :eventType ORDER BY ae.createdAt DESC")
    List<ActivityEvent> findByProjectAndEventType(
            @Param("projectId") Long projectId,
            @Param("eventType") EventType eventType);
}

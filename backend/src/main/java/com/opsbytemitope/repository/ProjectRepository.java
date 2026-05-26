package com.opsbytemitope.repository;

import com.opsbytemitope.entity.Project;
import com.opsbytemitope.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    List<Project> findByOwnerIdAndStatus(Long ownerId, ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId AND p.status != 'ARCHIVED'")
    List<Project> findActiveProjectsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.id = :ownerId AND p.status = :status")
    long countByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") ProjectStatus status);

    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);
}

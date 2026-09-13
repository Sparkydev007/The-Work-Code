package com.theworkcode.audit.repository;

import java.util.UUID;

import com.theworkcode.audit.entity.AuditEventEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    @Query("""
            SELECT a FROM AuditEventEntity a
            WHERE (CAST(:action AS string) IS NULL OR a.action = :action)
              AND (CAST(:actor AS string) IS NULL OR a.actor = :actor)
              AND (CAST(:resourceId AS string) IS NULL OR a.resourceId = :resourceId)
            """)
    Page<AuditEventEntity> search(@Param("action") String action,
                                  @Param("actor") String actor,
                                  @Param("resourceId") String resourceId,
                                  Pageable pageable);
}

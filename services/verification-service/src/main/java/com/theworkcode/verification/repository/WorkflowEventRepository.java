package com.theworkcode.verification.repository;

import java.util.List;
import java.util.UUID;

import com.theworkcode.verification.entity.WorkflowEventEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowEventRepository extends JpaRepository<WorkflowEventEntity, UUID> {

    List<WorkflowEventEntity> findByRequestIdOrderByOccurredAtAsc(UUID requestId);
}

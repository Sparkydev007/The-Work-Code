package com.theworkcode.batch.repository;

import java.util.List;
import java.util.UUID;

import com.theworkcode.batch.entity.BatchItemEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchItemRepository extends JpaRepository<BatchItemEntity, UUID> {

    List<BatchItemEntity> findByBatchIdOrderByRowNumberAsc(UUID batchId);
}

package com.theworkcode.batch.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.theworkcode.batch.entity.BatchJobEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobRepository extends JpaRepository<BatchJobEntity, UUID> {

    Optional<BatchJobEntity> findByBatchCode(String batchCode);

    List<BatchJobEntity> findAllByOrderByCreatedAtDesc();
}

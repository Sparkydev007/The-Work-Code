package com.theworkcode.verification.repository;

import java.util.UUID;

import com.theworkcode.verification.entity.DisputeEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeRepository extends JpaRepository<DisputeEntity, UUID> {

    Page<DisputeEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}

package com.theworkcode.verification.repository;

import java.util.UUID;

import com.theworkcode.verification.entity.ManualVerificationEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualVerificationRepository extends JpaRepository<ManualVerificationEntity, UUID> {

    Page<ManualVerificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}

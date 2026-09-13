package com.theworkcode.identity.repository;

import java.util.UUID;

import com.theworkcode.identity.entity.IdentityVerificationEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityVerificationRepository extends JpaRepository<IdentityVerificationEntity, UUID> {

    Page<IdentityVerificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

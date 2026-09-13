package com.theworkcode.employment.repository;

import java.util.UUID;

import com.theworkcode.employment.entity.EmploymentVerificationEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentVerificationRepository extends JpaRepository<EmploymentVerificationEntity, UUID> {

    Page<EmploymentVerificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

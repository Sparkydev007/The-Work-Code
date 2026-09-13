package com.theworkcode.income.repository;

import java.util.UUID;

import com.theworkcode.income.entity.IncomeVerificationEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeVerificationRepository extends JpaRepository<IncomeVerificationEntity, UUID> {

    Page<IncomeVerificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

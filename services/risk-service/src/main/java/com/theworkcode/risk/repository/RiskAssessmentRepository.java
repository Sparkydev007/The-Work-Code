package com.theworkcode.risk.repository;

import java.util.Optional;
import java.util.UUID;

import com.theworkcode.risk.entity.RiskAssessmentEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {

    Optional<RiskAssessmentEntity> findTopByVerificationIdOrderByCreatedAtDesc(String verificationId);
}

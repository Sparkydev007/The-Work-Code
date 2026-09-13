package com.theworkcode.report.repository;

import java.util.Optional;
import java.util.UUID;

import com.theworkcode.report.entity.ReportEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

    Optional<ReportEntity> findByReportCode(String reportCode);

    Optional<ReportEntity> findFirstByVerificationCodeOrderByGeneratedAtDesc(String verificationCode);

    Page<ReportEntity> findAllByOrderByGeneratedAtDesc(Pageable pageable);
}

package com.theworkcode.risk.repository;

import java.util.List;
import java.util.UUID;

import com.theworkcode.risk.entity.RiskAnomalyEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAnomalyRepository extends JpaRepository<RiskAnomalyEntity, UUID> {

    Page<RiskAnomalyEntity> findAllByOrderByDetectedAtDesc(Pageable pageable);

    List<RiskAnomalyEntity> findTop50ByStatusOrderByDetectedAtDesc(String status);

    long countByStatus(String status);

    long countBySeverity(String severity);
}

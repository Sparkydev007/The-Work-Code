package com.theworkcode.verification.repository;

import java.util.Optional;
import java.util.UUID;

import com.theworkcode.verification.entity.VerificationRequestEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequestEntity, UUID> {

    Optional<VerificationRequestEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<VerificationRequestEntity> findByVerificationCode(String verificationCode);

    @Query("""
            SELECT v FROM VerificationRequestEntity v
            WHERE (:status IS NULL OR v.status = :status)
              AND (:type IS NULL OR v.verificationType = :type)
              AND (:search IS NULL OR LOWER(v.applicantName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(v.verificationCode) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(v.employeeNumber) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<VerificationRequestEntity> search(@Param("status") String status,
                                           @Param("type") String type,
                                           @Param("search") String search,
                                           Pageable pageable);

    long countByStatus(String status);
}

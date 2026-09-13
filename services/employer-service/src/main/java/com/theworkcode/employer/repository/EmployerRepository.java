package com.theworkcode.employer.repository;

import java.util.Optional;
import java.util.UUID;

import com.theworkcode.employer.entity.EmployerEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployerRepository extends JpaRepository<EmployerEntity, UUID> {

    Optional<EmployerEntity> findByEmployerCode(String employerCode);

    @Query("""
            SELECT e FROM EmployerEntity e
            WHERE (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.employerCode) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:industry IS NULL OR e.industry = :industry)
              AND (:trustStatus IS NULL OR e.trustStatus = :trustStatus)
            """)
    Page<EmployerEntity> search(@Param("search") String search,
                                @Param("industry") String industry,
                                @Param("trustStatus") String trustStatus,
                                Pageable pageable);
}

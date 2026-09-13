package com.theworkcode.employee.repository;

import java.util.Optional;
import java.util.UUID;

import com.theworkcode.employee.entity.EmployeeEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    Optional<EmployeeEntity> findByEmployeeNumber(String employeeNumber);

    @Query("""
            SELECT e FROM EmployeeEntity e
            WHERE (CAST(:search AS string) IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
              AND (CAST(:status AS string) IS NULL OR e.status = :status)
              AND (CAST(:employerCode AS string) IS NULL OR e.employerCode = :employerCode)
              AND (CAST(:department AS string) IS NULL OR e.department = :department)
            """)
    Page<EmployeeEntity> search(@Param("search") String search,
                                @Param("status") String status,
                                @Param("employerCode") String employerCode,
                                @Param("department") String department,
                                Pageable pageable);

    long countByEmployerCode(String employerCode);

    long countByStatus(String status);
}

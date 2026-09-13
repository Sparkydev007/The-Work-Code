package com.theworkcode.employment.repository;

import java.util.List;
import java.util.UUID;

import com.theworkcode.employment.entity.EmploymentRecordEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentRecordRepository extends JpaRepository<EmploymentRecordEntity, UUID> {

    List<EmploymentRecordEntity> findByEmployeeNumberOrderByHireDateDesc(String employeeNumber);

    long countByEmployerCode(String employerCode);
}

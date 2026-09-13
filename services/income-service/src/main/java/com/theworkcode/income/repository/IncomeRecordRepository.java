package com.theworkcode.income.repository;

import java.util.Optional;
import java.util.UUID;

import com.theworkcode.income.entity.IncomeRecordEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeRecordRepository extends JpaRepository<IncomeRecordEntity, UUID> {

    Optional<IncomeRecordEntity> findByEmployeeNumber(String employeeNumber);
}

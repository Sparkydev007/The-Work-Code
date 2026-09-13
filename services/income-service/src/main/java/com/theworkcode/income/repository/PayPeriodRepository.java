package com.theworkcode.income.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.theworkcode.income.entity.PayPeriodEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PayPeriodRepository extends JpaRepository<PayPeriodEntity, UUID> {

    List<PayPeriodEntity> findByEmployeeNumberAndPayDateBetweenOrderByPayDateDesc(
            String employeeNumber, LocalDate start, LocalDate end);

    List<PayPeriodEntity> findByEmployeeNumberOrderByPayDateDesc(String employeeNumber);

    long countByEmployeeNumber(String employeeNumber);
}

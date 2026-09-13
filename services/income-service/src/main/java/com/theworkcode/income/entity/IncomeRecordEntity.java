package com.theworkcode.income.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "income_records")
@Getter
@Setter
@NoArgsConstructor
public class IncomeRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_number", nullable = false, unique = true)
    private String employeeNumber;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "annual_base_inr", nullable = false)
    private Long annualBaseInr;

    @Column(name = "annual_overtime_inr", nullable = false)
    private Long annualOvertimeInr;

    @Column(name = "annual_bonus_inr", nullable = false)
    private Long annualBonusInr;

    @Column(name = "annual_commission_inr", nullable = false)
    private Long annualCommissionInr;

    @Column(name = "annual_other_inr", nullable = false)
    private Long annualOtherInr;

    @Column(name = "annual_total_inr", nullable = false)
    private Long annualTotalInr;

    @Column(name = "monthly_total_inr", nullable = false)
    private Long monthlyTotalInr;

    @Column(name = "pay_frequency", nullable = false)
    private String payFrequency;

    @Column(name = "confidence", nullable = false)
    private java.math.BigDecimal confidence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

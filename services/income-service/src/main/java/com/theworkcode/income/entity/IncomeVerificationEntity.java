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
@Table(name = "income_verifications")
@Getter
@Setter
@NoArgsConstructor
public class IncomeVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_number", nullable = false)
    private String employeeNumber;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "confidence", nullable = false)
    private java.math.BigDecimal confidence;

    @Column(name = "lookback_months")
    private Integer lookbackMonths;

    @Column(name = "annualized_income")
    private Long annualizedIncome;

    @Column(name = "monthly_income")
    private Long monthlyIncome;

    @Column(name = "pay_frequency")
    private String payFrequency;

    @Column(name = "details", columnDefinition = "jsonb")
    private String details;

    @Column(name = "request_id")
    private String requestId;

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

package com.theworkcode.income.entity;

import java.time.LocalDate;
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
@Table(name = "pay_periods")
@Getter
@Setter
@NoArgsConstructor
public class PayPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_number", nullable = false)
    private String employeeNumber;

    @Column(name = "pay_date", nullable = false)
    private LocalDate payDate;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "gross_pay_inr", nullable = false)
    private Long grossPayInr;

    @Column(name = "net_pay_inr", nullable = false)
    private Long netPayInr;

    @Column(name = "base_pay_inr", nullable = false)
    private Long basePayInr;

    @Column(name = "overtime_inr", nullable = false)
    private Long overtimeInr;

    @Column(name = "bonus_inr", nullable = false)
    private Long bonusInr;

    @Column(name = "commission_inr", nullable = false)
    private Long commissionInr;

    @Column(name = "hours_worked", nullable = false)
    private java.math.BigDecimal hoursWorked;

    @Column(name = "deductions_inr", nullable = false)
    private Long deductionsInr;

    @Column(name = "pay_frequency", nullable = false)
    private String payFrequency;

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

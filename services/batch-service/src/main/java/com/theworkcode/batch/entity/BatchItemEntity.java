package com.theworkcode.batch.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "batch_items")
@Getter
@Setter
@NoArgsConstructor
public class BatchItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "employer")
    private String employer;

    @Column(name = "verification_type")
    private String verificationType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "result")
    private String result;

    @Column(name = "confidence")
    private BigDecimal confidence;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}

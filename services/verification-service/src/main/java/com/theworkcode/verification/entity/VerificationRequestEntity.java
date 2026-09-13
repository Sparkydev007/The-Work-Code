package com.theworkcode.verification.entity;

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

/**
 * Verification request aggregate. Status follows the workflow state machine:
 *
 * <pre>
 * CREATED -> PROCESSING -> IDENTITY_CHECK -> EMPLOYMENT_CHECK -> INCOME_CHECK
 *         -> RISK_CHECK -> COMPLETED | REVIEW_REQUIRED | FAILED | EXPIRED
 * </pre>
 */
@Entity
@Table(name = "verification_requests")
@Getter
@Setter
@NoArgsConstructor
public class VerificationRequestEntity {

    // State machine states
    public static final String CREATED = "CREATED";
    public static final String PROCESSING = "PROCESSING";
    public static final String IDENTITY_CHECK = "IDENTITY_CHECK";
    public static final String EMPLOYMENT_CHECK = "EMPLOYMENT_CHECK";
    public static final String INCOME_CHECK = "INCOME_CHECK";
    public static final String RISK_CHECK = "RISK_CHECK";
    public static final String REVIEW_REQUIRED = "REVIEW_REQUIRED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String EXPIRED = "EXPIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verification_code", unique = true)
    private String verificationCode;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "verification_type", nullable = false)
    private String verificationType;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "applicant_email")
    private String applicantEmail;

    @Column(name = "applicant_dob")
    private LocalDate applicantDob;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "lookback_months")
    private Integer lookbackMonths;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "result")
    private String result;

    @Column(name = "confidence")
    private java.math.BigDecimal confidence;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_band")
    private String riskBand;

    @Column(name = "identity_ref")
    private String identityRef;

    @Column(name = "employment_ref")
    private String employmentRef;

    @Column(name = "income_ref")
    private String incomeRef;

    @Column(name = "report_ref")
    private String reportRef;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

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

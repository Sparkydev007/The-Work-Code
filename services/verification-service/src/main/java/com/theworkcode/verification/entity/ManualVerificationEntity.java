package com.theworkcode.verification.entity;

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
 * Manual verification fallback queue:
 * AUTOMATED SEARCH -> NO DATA -> MANUAL REVIEW -> CONTACT EMPLOYER (simulated)
 * -> VERIFY DATA -> FINAL REPORT.
 */
@Entity
@Table(name = "manual_verifications")
@Getter
@Setter
@NoArgsConstructor
public class ManualVerificationEntity {

    public static final String PENDING = "PENDING";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String AWAITING_RESPONSE = "AWAITING_RESPONSE";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verification_id")
    private UUID verificationId;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "assigned_analyst")
    private String assignedAnalyst;

    @Column(name = "employer_contact")
    private String employerContact;

    @Column(name = "contact_method")
    private String contactMethod;

    @Column(name = "requested_attributes", columnDefinition = "text")
    private String requestedAttributes;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status", nullable = false)
    private String status;

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

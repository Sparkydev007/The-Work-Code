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

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
public class DisputeEntity {

    public static final String SUBMITTED = "SUBMITTED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String RESOLVED = "RESOLVED";
    public static final String REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dispute_code", unique = true)
    private String disputeCode;

    @Column(name = "verification_id")
    private UUID verificationId;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "reported_value", nullable = false)
    private String reportedValue;

    @Column(name = "claimed_value", nullable = false)
    private String claimedValue;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "reviewed_by")
    private String reviewedBy;

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

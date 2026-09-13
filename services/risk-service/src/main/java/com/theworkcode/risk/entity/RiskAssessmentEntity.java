package com.theworkcode.risk.entity;

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
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
public class RiskAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verification_id")
    private String verificationId;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "band", nullable = false)
    private String band;

    @Column(name = "signals", columnDefinition = "jsonb")
    private String signals;

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

package com.theworkcode.identity.entity;

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
@Table(name = "identity_verifications")
@Getter
@Setter
@NoArgsConstructor
public class IdentityVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "input_name")
    private String inputName;

    @Column(name = "input_employee_number")
    private String inputEmployeeNumber;

    @Column(name = "input_date_of_birth")
    private LocalDate inputDateOfBirth;

    @Column(name = "input_email")
    private String inputEmail;

    @Column(name = "input_employer")
    private String inputEmployer;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "confidence", nullable = false)
    private java.math.BigDecimal confidence;

    @Column(name = "name_score")
    private java.math.BigDecimal nameScore;

    @Column(name = "employer_score")
    private java.math.BigDecimal employerScore;

    @Column(name = "employee_id_score")
    private java.math.BigDecimal employeeIdScore;

    @Column(name = "email_score")
    private java.math.BigDecimal emailScore;

    @Column(name = "matched_employee_number")
    private String matchedEmployeeNumber;

    @Column(name = "breakdown", columnDefinition = "jsonb")
    private String breakdown;

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

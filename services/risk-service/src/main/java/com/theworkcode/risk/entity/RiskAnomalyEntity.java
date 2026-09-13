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
@Table(name = "risk_anomalies")
@Getter
@Setter
@NoArgsConstructor
public class RiskAnomalyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "explanation", nullable = false)
    private String explanation;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "verification_id")
    private String verificationId;

    @Column(name = "recommended_action")
    private String recommendedAction;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt;
}

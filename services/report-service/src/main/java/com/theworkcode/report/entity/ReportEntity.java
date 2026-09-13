package com.theworkcode.report.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "report_code", unique = true)
    private String reportCode;

    @Column(name = "verification_id")
    private String verificationId;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "result")
    private String result;

    @Column(name = "confidence")
    private java.math.BigDecimal confidence;

    @Column(name = "generated_by")
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Lob
    @Column(name = "pdf_bytes", columnDefinition = "bytea")
    private byte[] pdfBytes;

    @Column(name = "access_count", nullable = false)
    private Integer accessCount;

    @Column(name = "content", columnDefinition = "jsonb")
    private String content;
}

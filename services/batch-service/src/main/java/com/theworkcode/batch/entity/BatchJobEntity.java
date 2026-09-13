package com.theworkcode.batch.entity;

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
@Table(name = "batch_jobs")
@Getter
@Setter
@NoArgsConstructor
public class BatchJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_code", unique = true)
    private String batchCode;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Column(name = "processed_rows", nullable = false)
    private Integer processedRows;

    @Column(name = "verified_rows", nullable = false)
    private Integer verifiedRows;

    @Column(name = "review_rows", nullable = false)
    private Integer reviewRows;

    @Column(name = "failed_rows", nullable = false)
    private Integer failedRows;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_by")
    private String createdBy;

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

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
 * Verifier access / credentialing. In demo mode the organization starts
 * CREDENTIALED so flows work out of the box; the UI exposes this state.
 */
@Entity
@Table(name = "organization_credentialing")
@Getter
@Setter
@NoArgsConstructor
public class OrganizationCredentialEntity {

    public static final String PENDING_CREDENTIALING = "PENDING_CREDENTIALING";
    public static final String CREDENTIALED = "CREDENTIALED";
    public static final String SUSPENDED = "SUSPENDED";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false, unique = true)
    private String organizationId;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "verification_purpose")
    private String verificationPurpose;

    @Column(name = "credential_status", nullable = false)
    private String credentialStatus;

    @Column(name = "credentialed", nullable = false)
    private boolean credentialed;

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

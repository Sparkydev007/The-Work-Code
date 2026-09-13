package com.theworkcode.verification.service;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.verification.dto.VerificationDtos.CredentialResponse;
import com.theworkcode.verification.entity.OrganizationCredentialEntity;
import com.theworkcode.verification.repository.OrganizationCredentialRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifier access / credentialing. Demo organizations are seeded CREDENTIALED
 * so the platform works out of the box; the UI can still demo the full state
 * machine (PENDING_CREDENTIALING -> CREDENTIALED / SUSPENDED).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialService {

    private final OrganizationCredentialRepository repository;

    @Transactional
    public CredentialResponse getOrCreate(String organizationId) {
        OrganizationCredentialEntity e = repository.findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    OrganizationCredentialEntity created = new OrganizationCredentialEntity();
                    created.setOrganizationId(organizationId);
                    created.setOrganizationName("Meridian Lending Group (Demo)");
                    created.setVerificationPurpose("Mortgage and loan underwriting verification");
                    created.setCredentialStatus(OrganizationCredentialEntity.CREDENTIALED);
                    created.setCredentialed(true);
                    return repository.save(created);
                });
        return toResponse(e);
    }

    @Transactional
    public CredentialResponse activateDemoCredentialing(String organizationId) {
        OrganizationCredentialEntity e = repository.findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    OrganizationCredentialEntity created = new OrganizationCredentialEntity();
                    created.setOrganizationId(organizationId);
                    created.setOrganizationName("Meridian Lending Group (Demo)");
                    return created;
                });
        e.setCredentialStatus(OrganizationCredentialEntity.CREDENTIALED);
        e.setCredentialed(true);
        log.info("demo_credentialing_activated org={}", organizationId);
        return toResponse(repository.save(e));
    }

    @Transactional
    public CredentialResponse suspend(String organizationId) {
        OrganizationCredentialEntity e = repository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Organization not found."));
        e.setCredentialStatus(OrganizationCredentialEntity.SUSPENDED);
        e.setCredentialed(false);
        return toResponse(repository.save(e));
    }

    private CredentialResponse toResponse(OrganizationCredentialEntity e) {
        return new CredentialResponse(e.getOrganizationId(), e.getOrganizationName(),
                e.getVerificationPurpose(), e.getCredentialStatus(), e.isCredentialed());
    }
}

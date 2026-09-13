package com.theworkcode.verification.repository;

import java.util.Optional;

import com.theworkcode.verification.entity.OrganizationCredentialEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationCredentialRepository extends JpaRepository<OrganizationCredentialEntity, Long> {

    Optional<OrganizationCredentialEntity> findByOrganizationId(String organizationId);
}

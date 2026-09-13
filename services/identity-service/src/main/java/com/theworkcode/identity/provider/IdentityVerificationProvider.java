package com.theworkcode.identity.provider;

import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyRequest;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyResponse;

/**
 * Provider abstraction for identity verification. The demo provider resolves
 * against the synthetic employee index; a production provider (e.g. a real
 * workforce data network) can implement the same interface without changing
 * the business layer.
 */
public interface IdentityVerificationProvider {

    IdentityVerifyResponse verify(IdentityVerifyRequest request);
}

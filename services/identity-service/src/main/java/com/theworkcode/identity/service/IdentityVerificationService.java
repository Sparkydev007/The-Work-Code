package com.theworkcode.identity.service;

import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.identity.dto.IdentityDtos.IdentityPageResponse;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerificationSummary;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyRequest;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyResponse;
import com.theworkcode.identity.entity.IdentityVerificationEntity;
import com.theworkcode.identity.provider.IdentityVerificationProvider;
import com.theworkcode.identity.repository.IdentityVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityVerificationService {

    private final IdentityVerificationProvider provider;
    private final IdentityVerificationRepository repository;

    @Transactional
    public IdentityVerifyResponse verify(IdentityVerifyRequest request, String requestId) {
        IdentityVerifyResponse response = provider.verify(request);

        IdentityVerificationEntity entity = new IdentityVerificationEntity();
        entity.setInputName(request.name());
        entity.setInputEmployeeNumber(request.employeeNumber());
        try {
            if (request.dateOfBirth() != null && !request.dateOfBirth().isBlank()) {
                entity.setInputDateOfBirth(java.time.LocalDate.parse(request.dateOfBirth()));
            }
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "dateOfBirth must be ISO format (yyyy-MM-dd).");
        }
        entity.setInputEmail(request.email());
        entity.setInputEmployer(request.employer());
        entity.setResult(response.result());
        entity.setConfidence(java.math.BigDecimal.valueOf(response.confidence()));
        entity.setNameScore(java.math.BigDecimal.valueOf(response.nameScore()));
        entity.setEmployerScore(java.math.BigDecimal.valueOf(response.employerScore()));
        entity.setEmployeeIdScore(java.math.BigDecimal.valueOf(response.employeeIdScore()));
        entity.setEmailScore(java.math.BigDecimal.valueOf(response.emailScore()));
        entity.setMatchedEmployeeNumber(response.matchedEmployeeNumber());
        try {
            entity.setBreakdown(new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(response.breakdown()));
        } catch (Exception ignored) {
            // breakdown persistence is best-effort
        }
        entity.setRequestId(requestId);

        entity = repository.save(entity);
        log.info("identity_verified id={} result={} confidence={}", entity.getId(), response.result(),
                response.confidence());
        return new IdentityVerifyResponse(entity.getId(), response.result(), response.confidence(),
                response.nameScore(), response.employerScore(), response.employeeIdScore(),
                response.emailScore(), response.matchedEmployeeNumber(), response.breakdown(),
                entity.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public IdentityVerifyResponse get(UUID id) {
        IdentityVerificationEntity e = repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Identity verification not found."));
        return new IdentityVerifyResponse(e.getId(), e.getResult(), e.getConfidence().doubleValue(),
                e.getNameScore() == null ? 0 : e.getNameScore().doubleValue(),
                e.getEmployerScore() == null ? 0 : e.getEmployerScore().doubleValue(),
                e.getEmployeeIdScore() == null ? 0 : e.getEmployeeIdScore().doubleValue(),
                e.getEmailScore() == null ? 0 : e.getEmailScore().doubleValue(),
                e.getMatchedEmployeeNumber(),
                e.getBreakdown() == null ? java.util.Map.of() : java.util.Map.of("raw", e.getBreakdown()),
                e.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public IdentityPageResponse history(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        Page<IdentityVerificationEntity> result = repository.findAllByOrderByCreatedAtDesc(pageable);
        return new IdentityPageResponse(
                result.getContent().stream()
                        .map(e -> new IdentityVerificationSummary(e.getId(), e.getInputName(),
                                e.getInputEmployeeNumber(), e.getResult(),
                                e.getConfidence() == null ? 0 : e.getConfidence().doubleValue(), e.getCreatedAt()))
                        .toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}

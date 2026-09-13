package com.theworkcode.verification.service;

import java.util.List;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.verification.dto.VerificationDtos.CreateDisputeRequest;
import com.theworkcode.verification.dto.VerificationDtos.DisputePageResponse;
import com.theworkcode.verification.dto.VerificationDtos.DisputeResponse;
import com.theworkcode.verification.dto.VerificationDtos.DisputeStatusUpdate;
import com.theworkcode.verification.entity.DisputeEntity;
import com.theworkcode.verification.repository.DisputeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dispute center: employees can dispute a reported field; analysts resolve.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;

    @Transactional
    public DisputeResponse create(CreateDisputeRequest request, String submittedBy) {
        DisputeEntity e = new DisputeEntity();
        e.setDisputeCode("DSP-" + Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()), 36)
                .toUpperCase().substring(0, 5));
        e.setVerificationId(request.verificationId());
        e.setEmployeeNumber(request.employeeNumber());
        e.setFieldName(request.fieldName());
        e.setReportedValue(request.reportedValue());
        e.setClaimedValue(request.claimedValue());
        e.setExplanation(request.explanation());
        e.setStatus(DisputeEntity.SUBMITTED);
        e.setSubmittedBy(submittedBy);
        e = disputeRepository.save(e);
        log.info("dispute_created code={} field={}", e.getDisputeCode(), e.getFieldName());
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public DisputePageResponse list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        Page<DisputeEntity> result = disputeRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<DisputeResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new DisputePageResponse(content, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DisputeResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public DisputeResponse updateStatus(UUID id, DisputeStatusUpdate update, String reviewedBy) {
        DisputeEntity e = find(id);
        String status = update.status() == null ? e.getStatus() : update.status().toUpperCase();
        if (!List.of(DisputeEntity.SUBMITTED, DisputeEntity.UNDER_REVIEW, DisputeEntity.RESOLVED,
                DisputeEntity.REJECTED).contains(status)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid dispute status: " + status);
        }
        e.setStatus(status);
        e.setResolution(update.resolution());
        e.setReviewedBy(reviewedBy);
        e = disputeRepository.save(e);
        log.info("dispute_updated code={} status={}", e.getDisputeCode(), e.getStatus());
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return disputeRepository.countByStatus(status);
    }

    private DisputeEntity find(UUID id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.DISPUTE_NOT_FOUND, "Dispute not found."));
    }

    private DisputeResponse toResponse(DisputeEntity e) {
        return new DisputeResponse(e.getId(), e.getDisputeCode(), e.getVerificationId(), e.getEmployeeNumber(),
                e.getFieldName(), e.getReportedValue(), e.getClaimedValue(), e.getExplanation(), e.getStatus(),
                e.getResolution(), e.getSubmittedBy(), e.getReviewedBy(), e.getCreatedAt(), e.getUpdatedAt());
    }
}

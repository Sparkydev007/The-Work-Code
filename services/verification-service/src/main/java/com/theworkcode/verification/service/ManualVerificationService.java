package com.theworkcode.verification.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.verification.dto.VerificationDtos.ManualAssignRequest;
import com.theworkcode.verification.dto.VerificationDtos.ManualQueueItem;
import com.theworkcode.verification.dto.VerificationDtos.ManualQueuePageResponse;
import com.theworkcode.verification.entity.ManualVerificationEntity;
import com.theworkcode.verification.repository.ManualVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manual verification fallback queue:
 * PENDING -> IN_PROGRESS -> AWAITING_RESPONSE -> COMPLETED / FAILED.
 * "Simulate Employer Response" advances the case deterministically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManualVerificationService {

    private final ManualVerificationRepository repository;

    @Transactional
    public ManualQueueItem enqueue(UUID verificationId, String employeeNumber, String employerName, String reason) {
        ManualVerificationEntity e = new ManualVerificationEntity();
        e.setVerificationId(verificationId);
        e.setEmployeeNumber(employeeNumber);
        e.setEmployerName(employerName);
        e.setStatus(ManualVerificationEntity.PENDING);
        e.setNotes(reason);
        e.setContactMethod("EMAIL");
        e.setEmployerContact("hr@" + (employerName == null ? "employer" : employerName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "")) + ".demo");
        e = repository.save(e);
        log.info("manual_verification_queued id={} employer={}", e.getId(), employerName);
        return toItem(e);
    }

    @Transactional(readOnly = true)
    public ManualQueuePageResponse list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        Page<ManualVerificationEntity> result = repository.findAllByOrderByCreatedAtDesc(pageable);
        List<ManualQueueItem> content = result.getContent().stream().map(this::toItem).toList();
        return new ManualQueuePageResponse(content, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Transactional
    public ManualQueueItem assign(UUID id, ManualAssignRequest request) {
        ManualVerificationEntity e = find(id);
        e.setAssignedAnalyst(request.assignedAnalyst());
        if (request.employerContact() != null) {
            e.setEmployerContact(request.employerContact());
        }
        if (request.contactMethod() != null) {
            e.setContactMethod(request.contactMethod());
        }
        if (request.notes() != null) {
            e.setNotes(request.notes());
        }
        e.setStatus(ManualVerificationEntity.IN_PROGRESS);
        return toItem(repository.save(e));
    }

    @Transactional
    public ManualQueueItem awaitResponse(UUID id) {
        ManualVerificationEntity e = find(id);
        e.setStatus(ManualVerificationEntity.AWAITING_RESPONSE);
        return toItem(repository.save(e));
    }

    /** Simulated employer response: deterministic success (9 in 10 cases). */
    @Transactional
    public ManualQueueItem simulateEmployerResponse(UUID id) {
        ManualVerificationEntity e = find(id);
        if (!ManualVerificationEntity.AWAITING_RESPONSE.equals(e.getStatus())
                && !ManualVerificationEntity.IN_PROGRESS.equals(e.getStatus())) {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Case must be IN_PROGRESS or AWAITING_RESPONSE to simulate a response.");
        }
        boolean success = e.getId().hashCode() % 10 != 3; // deterministic 90% success
        e.setStatus(success ? ManualVerificationEntity.COMPLETED : ManualVerificationEntity.FAILED);
        e.setCompletedAt(OffsetDateTime.now());
        if (success && e.getNotes() != null) {
            e.setNotes(e.getNotes() + " | Employer response received and data verified.");
        }
        log.info("manual_verification_resolved id={} status={}", e.getId(), e.getStatus());
        return toItem(repository.save(e));
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    private ManualVerificationEntity find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Manual verification case not found."));
    }

    private ManualQueueItem toItem(ManualVerificationEntity e) {
        return new ManualQueueItem(e.getId(), e.getVerificationId(), e.getEmployeeNumber(), e.getEmployerName(),
                e.getAssignedAnalyst(), e.getEmployerContact(), e.getContactMethod(), e.getNotes(), e.getStatus(),
                e.getCreatedAt(), e.getCompletedAt());
    }
}

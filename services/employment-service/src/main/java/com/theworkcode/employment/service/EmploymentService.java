package com.theworkcode.employment.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.common.demo.DemoDataFactory;
import com.theworkcode.common.demo.DemoEmployee;
import com.theworkcode.common.demo.DemoEmploymentRecord;
import com.theworkcode.employment.dto.EmploymentDtos.HistoryResponse;
import com.theworkcode.employment.dto.EmploymentDtos.TenureResponse;
import com.theworkcode.employment.dto.EmploymentDtos.VerificationPageResponse;
import com.theworkcode.employment.dto.EmploymentDtos.VerificationResponse;
import com.theworkcode.employment.dto.EmploymentDtos.VerifyRequest;
import com.theworkcode.employment.entity.EmploymentRecordEntity;
import com.theworkcode.employment.entity.EmploymentVerificationEntity;
import com.theworkcode.employment.repository.EmploymentRecordRepository;
import com.theworkcode.employment.repository.EmploymentVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Employment verification (VOE) engine:
 *
 * <pre>
 *   VERIFIED     - current (or lookback) tenure found with high confidence
 *   REVIEW       - record exists but data is stale / low confidence
 *   NOT_VERIFIED - employer supplied does not match the record on file
 *   NO_RECORD    - employee number unknown to the employment index
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmploymentService {

    private final EmploymentRecordRepository recordRepository;
    private final EmploymentVerificationRepository verificationRepository;

    @Transactional
    public VerificationResponse verify(VerifyRequest request, String requestId) {
        List<EmploymentRecordEntity> records =
                recordRepository.findByEmployeeNumberOrderByHireDateDesc(request.employeeNumber());

        if (records.isEmpty()) {
            return persist(request.employeeNumber(), null, "NO_RECORD", 0.0, request.lookbackMonths(),
                    Map.of("reason", "No employment records found for this employee number."), requestId);
        }

        EmploymentRecordEntity current = records.get(0);
        boolean employerMatches = request.employerName() == null || request.employerName().isBlank()
                || current.getEmployerName().equalsIgnoreCase(request.employerName());

        // Lookback window: does a tenure overlap the requested window?
        boolean inWindow = true;
        if (request.lookbackMonths() != null && request.lookbackMonths() > 0) {
            LocalDate windowStart = LocalDate.now().minusMonths(request.lookbackMonths());
            inWindow = !current.getHireDate().isAfter(LocalDate.now())
                    && (current.getTerminationDate() == null
                        || !current.getTerminationDate().isBefore(windowStart));
        }

        String result;
        double confidence;
        if (!employerMatches) {
            result = "NOT_VERIFIED";
            confidence = 0.35;
        } else if (!inWindow) {
            result = "NO_RECORD";
            confidence = 0.40;
        } else if ("TERMINATED".equals(current.getStatus())) {
            result = "VERIFIED";
            confidence = 0.93;
        } else {
            result = "VERIFIED";
            confidence = current.getConfidence() == null ? 0.95 : current.getConfidence().doubleValue();
        }

        Map<String, Object> details = Map.of(
                "recordsFound", records.size(),
                "employerMatches", employerMatches,
                "withinLookback", inWindow,
                "source", current.getSource(),
                "lastDataUpdate", String.valueOf(current.getUpdatedAt()));

        return persist(request.employeeNumber(), current, result, confidence, request.lookbackMonths(),
                details, requestId);
    }

    @Transactional(readOnly = true)
    public HistoryResponse history(String employeeNumber) {
        List<EmploymentRecordEntity> records =
                recordRepository.findByEmployeeNumberOrderByHireDateDesc(employeeNumber);
        if (records.isEmpty()) {
            throw new ApiException(ErrorCode.EMPLOYEE_NOT_FOUND,
                    "No employment records for '" + employeeNumber + "'.");
        }
        List<TenureResponse> tenures = records.stream()
                .map(r -> new TenureResponse(r.getEmployerName(), r.getJobTitle(), r.getDepartment(),
                        r.getStatus(), r.getEmploymentType(), r.getHireDate(), r.getTerminationDate(),
                        r.getWorkLocation(), r.getSource(), r.getConfidence().doubleValue()))
                .toList();
        boolean active = !tenures.isEmpty()
                && tenures.get(0).endDate() == null
                && "ACTIVE".equals(tenures.get(0).status());
        return new HistoryResponse(employeeNumber, tenures, active);
    }

    @Transactional(readOnly = true)
    public VerificationResponse get(UUID id) {
        EmploymentVerificationEntity e = verificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Employment verification not found."));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public VerificationPageResponse history(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        Page<EmploymentVerificationEntity> result = verificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new VerificationPageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public long countByEmployer(String employerCode) {
        return recordRepository.countByEmployerCode(employerCode);
    }

    // ------------------------------------------------------------------
    // seeding
    // ------------------------------------------------------------------

    @Transactional
    public void seedDemoDataIfEmpty() {
        if (recordRepository.count() > 0) {
            return;
        }
        var employees = DemoDataFactory.employees(500);
        int count = 0;
        for (DemoEmployee demo : employees) {
            List<DemoEmploymentRecord> history = DemoDataFactory.employmentHistory(demo);
            for (DemoEmploymentRecord h : history) {
                EmploymentRecordEntity e = new EmploymentRecordEntity();
                e.setEmployeeNumber(h.employeeNumber());
                e.setEmployerName(h.employerName());
                e.setEmployerCode(DemoDataFactory.employerCode(h.employerName()));
                e.setJobTitle(h.jobTitle());
                e.setDepartment(h.department());
                e.setStatus(h.status());
                e.setEmploymentType(h.employmentType());
                e.setHireDate(h.hireDate());
                e.setTerminationDate(h.terminationDate());
                e.setWorkLocation(h.workLocation());
                e.setSource(h.source());
                e.setConfidence(java.math.BigDecimal.valueOf(h.confidence()));
                recordRepository.save(e);
                count++;
            }
        }
        log.info("employment_seed_complete employees={} records={}", employees.size(), count);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private VerificationResponse persist(String employeeNumber, EmploymentRecordEntity current, String result,
            double confidence, Integer lookbackMonths, Map<String, Object> details, String requestId) {
        EmploymentVerificationEntity e = new EmploymentVerificationEntity();
        e.setEmployeeNumber(employeeNumber);
        if (current != null) {
            e.setEmployerName(current.getEmployerName());
            e.setEmploymentStatus(current.getStatus());
            e.setJobTitle(current.getJobTitle());
            e.setDepartment(current.getDepartment());
            e.setHireDate(current.getHireDate());
            e.setTerminationDate(current.getTerminationDate());
        }
        e.setResult(result);
        e.setConfidence(java.math.BigDecimal.valueOf(confidence));
        e.setLookbackMonths(lookbackMonths);
        e.setRequestId(requestId);
        try {
            e.setDetails(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(details));
        } catch (Exception ignored) {
            // best effort
        }
        e = verificationRepository.save(e);
        log.info("employment_verified id={} employee={} result={}", e.getId(), employeeNumber, result);
        return toResponse(e);
    }

    private VerificationResponse toResponse(EmploymentVerificationEntity e) {
        Map<String, Object> details = Map.of();
        if (e.getDetails() != null) {
            try {
                details = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(e.getDetails(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        });
            } catch (Exception ignored) {
                // best effort
            }
        }
        return new VerificationResponse(e.getId(), e.getEmployeeNumber(), e.getEmployerName(),
                e.getEmploymentStatus(), e.getJobTitle(), e.getDepartment(), e.getHireDate(),
                e.getTerminationDate(), e.getResult(), e.getConfidence().doubleValue(), e.getLookbackMonths(),
                details, e.getCreatedAt());
    }
}

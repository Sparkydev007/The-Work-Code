package com.theworkcode.employer.service;

import java.time.Duration;
import java.util.List;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.common.demo.DemoDataFactory;
import com.theworkcode.employer.dto.EmployerDtos.CreateRequest;
import com.theworkcode.employer.dto.EmployerDtos.EmployerProfile;
import com.theworkcode.employer.dto.EmployerDtos.EmployerResponse;
import com.theworkcode.employer.dto.EmployerDtos.PageResponse;
import com.theworkcode.employer.entity.EmployerEntity;
import com.theworkcode.employer.repository.EmployerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * Employer business logic. The profile endpoint demonstrates cross-service
 * communication with graceful degradation: if employee-service is
 * unavailable, the employer profile still renders with the employee count
 * marked UNAVAILABLE instead of failing the request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerRepository repository;
    private final RestClient restClient;
    private final String employeeServiceBaseUrl;

    public EmployerService(EmployerRepository repository,
            @Value("${workcode.employee-service-url:http://localhost:8082}") String employeeServiceBaseUrl) {
        this.repository = repository;
        this.employeeServiceBaseUrl = employeeServiceBaseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(employeeServiceBaseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                    {
                        setConnectTimeout(Duration.ofSeconds(2));
                        setReadTimeout(Duration.ofSeconds(3));
                    }
                })
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse search(String search, String industry, String trustStatus, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)),
                Sort.by(Sort.Direction.ASC, "name"));
        Page<EmployerEntity> result = repository.search(
                blankToNull(search), blankToNull(industry), blankToNull(trustStatus), pageable);
        List<EmployerResponse> content = result.getContent().stream().map(EmployerResponse::from).toList();
        return new PageResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EmployerResponse getByCode(String employerCode) {
        return repository.findByEmployerCode(employerCode)
                .map(EmployerResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.EMPLOYER_NOT_FOUND,
                        "Employer '" + employerCode + "' was not found."));
    }

    /** Employer profile + employee stats from employee-service (resilient call). */
    @Transactional(readOnly = true)
    public EmployerProfile getProfile(String employerCode) {
        EmployerResponse employer = getByCode(employerCode);
        long activeRecords;
        String status;
        try {
            Long count = restClient.get()
                    .uri("/api/v1/employees/internal/count?employerCode=" + employerCode)
                    .retrieve()
                    .body(Long.class);
            activeRecords = count == null ? 0 : count;
            status = "UP";
        } catch (Exception e) {
            log.warn("employee_service_unavailable employerCode={}", employerCode);
            activeRecords = employer.employeeCount() == null ? 0 : employer.employeeCount();
            status = "UNAVAILABLE";
        }
        return new EmployerProfile(employer, activeRecords, status);
    }

    @Transactional
    public EmployerResponse create(CreateRequest request) {
        String code = DemoDataFactory.employerCode(request.name());
        repository.findByEmployerCode(code).ifPresent(e -> {
            throw new ApiException(ErrorCode.CONFLICT, "Employer code '" + code + "' already exists.");
        });
        EmployerEntity entity = new EmployerEntity();
        entity.setEmployerCode(code);
        entity.setName(request.name());
        entity.setIndustry(request.industry());
        entity.setCity(request.city());
        entity.setEmployeeCount(request.employeeCount());
        entity.setTrustStatus(request.trustStatus() == null ? "PENDING_REVIEW" : request.trustStatus());
        entity.setContributorSince(java.time.LocalDate.now());
        entity.setLastDataUpdate(java.time.LocalDate.now());
        entity.setDataSource("Synthetic employer payroll feed");
        entity.setRefreshFrequency("Every pay period");
        log.info("employer_created code={} name={}", code, request.name());
        return EmployerResponse.from(repository.save(entity));
    }

    @Transactional
    public void seedDemoDataIfEmpty() {
        if (repository.count() > 0) {
            return;
        }
        List<DemoEmployerFacade> employers = DemoDataFactory.employers().stream()
                .map(d -> new DemoEmployerFacade(d.employerCode(), d.name(), d.industry(), d.city(),
                        d.employeeCount(), d.trustStatus(), d.contributorSince(), d.lastDataUpdate()))
                .toList();
        for (DemoEmployerFacade d : employers) {
            EmployerEntity e = new EmployerEntity();
            e.setEmployerCode(d.code());
            e.setName(d.name());
            e.setIndustry(d.industry());
            e.setCity(d.city());
            e.setEmployeeCount(d.employeeCount());
            e.setTrustStatus(d.trustStatus());
            e.setContributorSince(d.contributorSince());
            e.setLastDataUpdate(d.lastDataUpdate());
            e.setDataSource("Synthetic employer payroll feed");
            e.setRefreshFrequency("Every pay period");
            repository.save(e);
        }
        log.info("employer_seed_complete employers={}", employers.size());
    }

    /** Local facade record to avoid leaking common-lib types into the entity mapping. */
    private record DemoEmployerFacade(String code, String name, String industry, String city,
            int employeeCount, String trustStatus, java.time.LocalDate contributorSince,
            java.time.LocalDate lastDataUpdate) {
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}

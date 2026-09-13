package com.theworkcode.employee.service;

import java.util.List;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.common.demo.DemoDataFactory;
import com.theworkcode.common.demo.DemoEmployer;
import com.theworkcode.employee.dto.EmployeeDtos.CreateRequest;
import com.theworkcode.employee.dto.EmployeeDtos.EmployeeResponse;
import com.theworkcode.employee.dto.EmployeeDtos.PageResponse;
import com.theworkcode.employee.entity.EmployeeEntity;
import com.theworkcode.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Employee business logic. On first boot with an empty table the demo seeder
 * loads the deterministic synthetic dataset (500 employees incl. scenarios).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    public static final int DEMO_EMPLOYEE_COUNT = 500;

    private final EmployeeRepository repository;

    @Transactional(readOnly = true)
    public PageResponse search(String search, String status, String employerCode, String department,
            int page, int size, boolean masked) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)),
                Sort.by(Sort.Direction.ASC, "name"));
        Page<EmployeeEntity> result = repository.search(
                blankToNull(search), blankToNull(status), blankToNull(employerCode), blankToNull(department),
                pageable);
        List<EmployeeResponse> content = result.getContent().stream()
                .map(masked ? EmployeeResponse::masked : EmployeeResponse::from)
                .toList();
        return new PageResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getByEmployeeNumber(String employeeNumber, boolean masked) {
        EmployeeEntity entity = repository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.EMPLOYEE_NOT_FOUND,
                        "Employee '" + employeeNumber + "' was not found."));
        return masked ? EmployeeResponse.masked(entity) : EmployeeResponse.from(entity);
    }

    @Transactional
    public EmployeeResponse create(CreateRequest request) {
        repository.findByEmployeeNumber(request.employeeNumber()).ifPresent(e -> {
            throw new ApiException(ErrorCode.CONFLICT,
                    "Employee number '" + request.employeeNumber() + "' already exists.");
        });
        EmployeeEntity entity = new EmployeeEntity();
        applyRequest(entity, request);
        if (entity.getStatus() == null) {
            entity.setStatus("ACTIVE");
        }
        if (entity.getEmployerName() != null) {
            entity.setEmployerCode(DemoDataFactory.employerCode(entity.getEmployerName()));
        }
        log.info("employee_created employeeNumber={} name={}", entity.getEmployeeNumber(), entity.getName());
        return EmployeeResponse.from(repository.save(entity));
    }

    @Transactional
    public EmployeeResponse update(String employeeNumber, CreateRequest request) {
        EmployeeEntity entity = repository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.EMPLOYEE_NOT_FOUND,
                        "Employee '" + employeeNumber + "' was not found."));
        applyRequest(entity, request);
        if (entity.getEmployerName() != null) {
            entity.setEmployerCode(DemoDataFactory.employerCode(entity.getEmployerName()));
        }
        return EmployeeResponse.from(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    public long countByEmployerCode(String employerCode) {
        return repository.countByEmployerCode(employerCode);
    }

    /**
     * Seeds the deterministic demo dataset exactly once (when the table is
     * empty and seeding is enabled). Invoked from {@link EmployeeSeedRunner}.
     */
    @Transactional
    public void seedDemoDataIfEmpty() {
        if (repository.count() > 0) {
            log.info("employee_seed_skipped table not empty ({} rows)", repository.count());
            return;
        }
        List<DemoEmployer> employers = DemoDataFactory.employers();
        var employees = DemoDataFactory.employees(DEMO_EMPLOYEE_COUNT);
        for (var demo : employees) {
            EmployeeEntity e = new EmployeeEntity();
            e.setEmployeeNumber(demo.employeeNumber());
            e.setName(demo.name());
            e.setDateOfBirth(demo.dateOfBirth());
            e.setEmail(demo.email());
            e.setPhone(demo.phone());
            e.setEmployerName(demo.employerName());
            e.setEmployerCode(DemoDataFactory.employerCode(demo.employerName()));
            e.setStatus(demo.status());
            e.setDepartment(demo.department());
            e.setJobTitle(demo.jobTitle());
            e.setEmploymentType(demo.employmentType());
            e.setHireDate(demo.hireDate());
            e.setTerminationDate(demo.terminationDate());
            e.setWorkLocation(demo.workLocation());
            e.setMonthlyBaseIncomeInr(demo.monthlyBaseIncomeInr());
            e.setPayFrequency(demo.payFrequency());
            e.setScenarioTag(demo.scenarioTag());
            repository.save(e);
        }
        log.info("employee_seed_complete employees={} employers={}", employees.size(), employers.size());
    }

    private void applyRequest(EmployeeEntity entity, CreateRequest request) {
        entity.setEmployeeNumber(request.employeeNumber());
        entity.setName(request.name());
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setEmployerName(request.employerName());
        entity.setStatus(request.status());
        entity.setDepartment(request.department());
        entity.setJobTitle(request.jobTitle());
        entity.setEmploymentType(request.employmentType());
        entity.setHireDate(request.hireDate());
        entity.setTerminationDate(request.terminationDate());
        entity.setWorkLocation(request.workLocation());
        entity.setMonthlyBaseIncomeInr(request.monthlyBaseIncomeInr());
        entity.setPayFrequency(request.payFrequency());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}

package com.theworkcode.employee.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import com.theworkcode.employee.entity.EmployeeEntity;

/**
 * Employee DTOs. Responses support field masking for sensitive-looking data.
 */
public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    public record CreateRequest(
            @NotBlank String employeeNumber,
            @NotBlank String name,
            LocalDate dateOfBirth,
            String email,
            String phone,
            String employerName,
            String status,
            String department,
            String jobTitle,
            String employmentType,
            LocalDate hireDate,
            LocalDate terminationDate,
            String workLocation,
            Long monthlyBaseIncomeInr,
            String payFrequency) {
    }

    public record EmployeeResponse(
            UUID id,
            String employeeNumber,
            String name,
            LocalDate dateOfBirth,
            String email,
            String phone,
            String employerName,
            String employerCode,
            String status,
            String department,
            String jobTitle,
            String employmentType,
            LocalDate hireDate,
            LocalDate terminationDate,
            String workLocation,
            Long monthlyBaseIncomeInr,
            String payFrequency,
            String scenarioTag,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        public static EmployeeResponse from(EmployeeEntity e) {
            return new EmployeeResponse(e.getId(), e.getEmployeeNumber(), e.getName(), e.getDateOfBirth(),
                    e.getEmail(), e.getPhone(), e.getEmployerName(), e.getEmployerCode(), e.getStatus(),
                    e.getDepartment(), e.getJobTitle(), e.getEmploymentType(), e.getHireDate(),
                    e.getTerminationDate(), e.getWorkLocation(), e.getMonthlyBaseIncomeInr(),
                    e.getPayFrequency(), e.getScenarioTag(), e.getCreatedAt(), e.getUpdatedAt());
        }

        /** Masked view: hides DOB/income and partially masks email/phone. */
        public static EmployeeResponse masked(EmployeeEntity e) {
            return new EmployeeResponse(
                    e.getId(), e.getEmployeeNumber(), e.getName(), null,
                    maskEmail(e.getEmail()), maskPhone(e.getPhone()),
                    e.getEmployerName(), e.getEmployerCode(), e.getStatus(), e.getDepartment(),
                    e.getJobTitle(), e.getEmploymentType(), e.getHireDate(), e.getTerminationDate(),
                    e.getWorkLocation(), null, e.getPayFrequency(), e.getScenarioTag(),
                    e.getCreatedAt(), e.getUpdatedAt());
        }

        private static String maskEmail(String email) {
            if (email == null || !email.contains("@")) {
                return email;
            }
            int at = email.indexOf('@');
            String local = email.substring(0, at);
            String maskedLocal = local.length() <= 2
                    ? local.charAt(0) + "••"
                    : local.charAt(0) + "••••" + local.charAt(local.length() - 1);
            return maskedLocal + email.substring(at);
        }

        private static String maskPhone(String phone) {
            if (phone == null || phone.length() < 4) {
                return phone;
            }
            return "•••••" + phone.substring(phone.length() - 4);
        }
    }

    public record PageResponse(List<EmployeeResponse> content, int page, int size,
            long totalElements, int totalPages) {
    }
}

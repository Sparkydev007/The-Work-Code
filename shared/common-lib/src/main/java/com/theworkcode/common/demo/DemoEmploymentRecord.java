package com.theworkcode.common.demo;

import java.time.LocalDate;

/**
 * One employment tenure (current or historical) for a demo employee.
 */
public record DemoEmploymentRecord(
        String employeeNumber,
        String employerName,
        String jobTitle,
        String department,
        String status,
        String employmentType,
        LocalDate hireDate,
        LocalDate terminationDate,
        String workLocation,
        String source,
        double confidence) {
}

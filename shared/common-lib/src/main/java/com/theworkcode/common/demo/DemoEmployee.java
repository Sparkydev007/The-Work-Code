package com.theworkcode.common.demo;

import java.time.LocalDate;

/**
 * Synthetic employee produced by {@link DemoDataFactory}. Carries a
 * {@code scenarioTag} used by the interview demo launcher.
 */
public record DemoEmployee(
        String employeeNumber,
        String name,
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
        long monthlyBaseIncomeInr,
        String payFrequency,
        String scenarioTag) {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_TERMINATED = "TERMINATED";
    public static final String STATUS_ON_LEAVE = "ON_LEAVE";

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
}

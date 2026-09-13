package com.theworkcode.common.demo;

import java.time.LocalDate;

/**
 * Synthetic employer record produced by {@link DemoDataFactory}.
 */
public record DemoEmployer(
        String employerCode,
        String name,
        String industry,
        String city,
        int employeeCount,
        String trustStatus,
        LocalDate contributorSince,
        LocalDate lastDataUpdate) {
}

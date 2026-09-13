package com.theworkcode.common.demo;

import java.time.LocalDate;

/**
 * One synthetic pay period. Gross/net must stay consistent with the
 * employee's monthly base income and pay frequency.
 */
public record DemoPayPeriod(
        String employeeNumber,
        LocalDate payDate,
        LocalDate periodStart,
        LocalDate periodEnd,
        long grossPayInr,
        long netPayInr,
        long basePayInr,
        long overtimeInr,
        long bonusInr,
        long commissionInr,
        double hoursWorked,
        long deductionsInr,
        String payFrequency) {
}

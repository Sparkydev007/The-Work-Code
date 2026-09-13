package com.theworkcode.income.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public final class IncomeDtos {

    private IncomeDtos() {
    }

    public record VerifyRequest(
            @NotBlank String employeeNumber,
            String employerName,
            Integer lookbackMonths) {
    }

    public record IncomeSummaryResponse(
            String employeeNumber,
            String employerName,
            long annualIncomeInr,
            long monthlyIncomeInr,
            long annualBaseInr,
            long annualOvertimeInr,
            long annualBonusInr,
            long annualCommissionInr,
            long annualOtherInr,
            String payFrequency,
            double confidence) {

        public static IncomeSummaryResponse from(com.theworkcode.income.entity.IncomeRecordEntity e) {
            return new IncomeSummaryResponse(e.getEmployeeNumber(), e.getEmployerName(),
                    e.getAnnualTotalInr(), e.getMonthlyTotalInr(), e.getAnnualBaseInr(),
                    e.getAnnualOvertimeInr(), e.getAnnualBonusInr(), e.getAnnualCommissionInr(),
                    e.getAnnualOtherInr(), e.getPayFrequency(), e.getConfidence().doubleValue());
        }
    }

    public record PayPeriodResponse(
            UUID id,
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

        public static PayPeriodResponse from(com.theworkcode.income.entity.PayPeriodEntity e) {
            return new PayPeriodResponse(e.getId(), e.getEmployeeNumber(), e.getPayDate(),
                    e.getPeriodStart(), e.getPeriodEnd(), e.getGrossPayInr(), e.getNetPayInr(),
                    e.getBasePayInr(), e.getOvertimeInr(), e.getBonusInr(), e.getCommissionInr(),
                    e.getHoursWorked().doubleValue(), e.getDeductionsInr(), e.getPayFrequency());
        }
    }

    public record VerificationResponse(
            UUID id,
            String employeeNumber,
            String employerName,
            String result,
            double confidence,
            Integer lookbackMonths,
            Long annualizedIncome,
            Long monthlyIncome,
            String payFrequency,
            Map<String, Object> details,
            OffsetDateTime createdAt) {
    }

    public record IncomeTrendPoint(LocalDate month, long grossInr, long baseInr, long variableInr) {
    }

    public record IncomeTrendResponse(String employeeNumber, List<IncomeTrendPoint> points) {
    }

    public record PayPeriodPageResponse(List<PayPeriodResponse> content, int page, int size,
            long totalElements) {
    }

    public record VerificationPageResponse(List<VerificationResponse> content, int page, int size,
            long totalElements, int totalPages) {
    }
}

package com.theworkcode.income.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.common.demo.DemoDataFactory;
import com.theworkcode.common.demo.DemoEmployee;
import com.theworkcode.common.demo.DemoPayPeriod;
import com.theworkcode.income.dto.IncomeDtos.IncomeSummaryResponse;
import com.theworkcode.income.dto.IncomeDtos.IncomeTrendPoint;
import com.theworkcode.income.dto.IncomeDtos.IncomeTrendResponse;
import com.theworkcode.income.dto.IncomeDtos.PayPeriodPageResponse;
import com.theworkcode.income.dto.IncomeDtos.PayPeriodResponse;
import com.theworkcode.income.dto.IncomeDtos.VerificationPageResponse;
import com.theworkcode.income.dto.IncomeDtos.VerificationResponse;
import com.theworkcode.income.dto.IncomeDtos.VerifyRequest;
import com.theworkcode.income.entity.IncomeRecordEntity;
import com.theworkcode.income.entity.IncomeVerificationEntity;
import com.theworkcode.income.entity.PayPeriodEntity;
import com.theworkcode.income.repository.IncomeRecordRepository;
import com.theworkcode.income.repository.IncomeVerificationRepository;
import com.theworkcode.income.repository.PayPeriodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Income verification (VOI) engine with configurable lookback
 * (12/24/36 months / all available). Result semantics:
 *
 * <pre>
 *   VERIFIED     - payroll records found and internally consistent
 *   REVIEW       - sparse payroll coverage or high variance
 *   NOT_VERIFIED - employer mismatch
 *   NO_RECORD    - no payroll data for the employee
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRecordRepository incomeRecordRepository;
    private final PayPeriodRepository payPeriodRepository;
    private final IncomeVerificationRepository verificationRepository;

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Transactional
    public VerificationResponse verify(VerifyRequest request, String requestId) {
        var incomeOpt = incomeRecordRepository.findByEmployeeNumber(request.employeeNumber());
        if (incomeOpt.isEmpty()) {
            return persist(request.employeeNumber(), null, "NO_RECORD", 0.0, request.lookbackMonths(),
                    Map.of("reason", "No payroll data on file for this employee."), requestId);
        }

        IncomeRecordEntity income = incomeOpt.get();
        boolean employerMatches = request.employerName() == null || request.employerName().isBlank()
                || income.getEmployerName().equalsIgnoreCase(request.employerName());

        LocalDate windowStart = lookbackStart(request.lookbackMonths());
        List<PayPeriodEntity> periods =
                payPeriodRepository.findByEmployeeNumberAndPayDateBetweenOrderByPayDateDesc(
                        request.employeeNumber(), windowStart, LocalDate.now());

        String result;
        double confidence;
        if (!employerMatches) {
            result = "NOT_VERIFIED";
            confidence = 0.35;
        } else if (periods.size() < 3) {
            result = "REVIEW";
            confidence = 0.55;
        } else {
            double variance = coefficientOfVariation(periods);
            if (variance > 0.45) {
                result = "REVIEW";
                confidence = 0.72;
            } else {
                result = "VERIFIED";
                confidence = 0.96;
            }
        }

        // annualized income recomputed from the lookback window (12-month trailing)
        long trailing12 = periods.stream()
                .filter(p -> p.getPayDate().isAfter(LocalDate.now().minusMonths(12)))
                .mapToLong(PayPeriodEntity::getGrossPayInr)
                .sum();

        Map<String, Object> details = new HashMap<>();
        details.put("payPeriodsInWindow", periods.size());
        details.put("employerMatches", employerMatches);
        details.put("trailing12MonthGrossInr", trailing12);
        details.put("grossVsNetRatio", periods.isEmpty() ? 0.0
                : periods.get(0).getNetPayInr() * 1.0 / periods.get(0).getGrossPayInr());
        details.put("lookbackMonths", request.lookbackMonths() == null ? 36 : request.lookbackMonths());

        return persist(request.employeeNumber(), income, result, confidence, request.lookbackMonths(),
                details, requestId);
    }

    @Transactional(readOnly = true)
    public IncomeSummaryResponse summary(String employeeNumber) {
        IncomeRecordEntity e = incomeRecordRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.INSUFFICIENT_DATA,
                        "No income record for '" + employeeNumber + "'."));
        return IncomeSummaryResponse.from(e);
    }

    @Transactional(readOnly = true)
    public PayPeriodPageResponse payPeriods(String employeeNumber, Integer lookbackMonths, int page, int size) {
        LocalDate windowStart = lookbackStart(lookbackMonths);
        List<PayPeriodEntity> periods =
                payPeriodRepository.findByEmployeeNumberAndPayDateBetweenOrderByPayDateDesc(
                        employeeNumber, windowStart, LocalDate.now());
        int from = Math.max(0, page) * Math.max(1, size);
        int to = Math.min(periods.size(), from + Math.max(1, size));
        List<PayPeriodResponse> content = from >= periods.size() ? List.of()
                : periods.subList(from, to).stream().map(PayPeriodResponse::from).toList();
        return new PayPeriodPageResponse(content, page, size, periods.size());
    }

    @Transactional(readOnly = true)
    public IncomeTrendResponse trend(String employeeNumber, Integer lookbackMonths) {
        LocalDate windowStart = lookbackStart(lookbackMonths);
        List<PayPeriodEntity> periods =
                payPeriodRepository.findByEmployeeNumberAndPayDateBetweenOrderByPayDateDesc(
                        employeeNumber, windowStart, LocalDate.now());

        Map<YearMonth, long[]> byMonth = new HashMap<>();
        for (PayPeriodEntity p : periods) {
            YearMonth ym = YearMonth.from(p.getPayDate());
            long[] agg = byMonth.computeIfAbsent(ym, k -> new long[3]);
            agg[0] += p.getGrossPayInr();
            agg[1] += p.getBasePayInr();
            agg[2] += p.getOvertimeInr() + p.getBonusInr() + p.getCommissionInr();
        }
        List<IncomeTrendPoint> points = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new IncomeTrendPoint(e.getKey().atDay(1), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .toList();
        return new IncomeTrendResponse(employeeNumber, points);
    }

    @Transactional(readOnly = true)
    public VerificationResponse get(java.util.UUID id) {
        IncomeVerificationEntity e = verificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Income verification not found."));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public VerificationPageResponse list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        Page<IncomeVerificationEntity> result = verificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new VerificationPageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    // ------------------------------------------------------------------
    // seeding
    // ------------------------------------------------------------------

    @Transactional
    public void seedDemoDataIfEmpty() {
        if (incomeRecordRepository.count() > 0) {
            return;
        }
        var employees = DemoDataFactory.employees(500);
        LocalDate lookback = DemoDataFactory.defaultLookbackStart();
        int periodCount = 0;
        List<PayPeriodEntity> batch = new ArrayList<>(2048);
        for (DemoEmployee demo : employees) {
            // income record
            IncomeRecordEntity e = new IncomeRecordEntity();
            e.setEmployeeNumber(demo.employeeNumber());
            e.setEmployerName(demo.employerName());
            long annualBase = demo.monthlyBaseIncomeInr() * 12;
            long ot = Math.round(annualBase * 0.04);
            long bonus = Math.round(annualBase * (demo.scenarioTag() != null ? 0.02 : 0.06));
            long commission = "Sales".equalsIgnoreCase(demo.department()) ? Math.round(annualBase * 0.10) : 0;
            long total = annualBase + ot + bonus + commission;
            e.setAnnualBaseInr(annualBase);
            e.setAnnualOvertimeInr(ot);
            e.setAnnualBonusInr(bonus);
            e.setAnnualCommissionInr(commission);
            e.setAnnualOtherInr(0L);
            e.setAnnualTotalInr(total);
            e.setMonthlyTotalInr(Math.round(total / 12.0));
            e.setPayFrequency(demo.payFrequency());
            e.setConfidence(BigDecimal.valueOf(0.95));
            incomeRecordRepository.save(e);

            // pay periods (36-month lookback)
            List<DemoPayPeriod> periods = DemoDataFactory.payPeriods(demo, lookback);
            for (DemoPayPeriod p : periods) {
                PayPeriodEntity pe = new PayPeriodEntity();
                pe.setEmployeeNumber(p.employeeNumber());
                pe.setPayDate(p.payDate());
                pe.setPeriodStart(p.periodStart());
                pe.setPeriodEnd(p.periodEnd());
                pe.setGrossPayInr(p.grossPayInr());
                pe.setNetPayInr(p.netPayInr());
                pe.setBasePayInr(p.basePayInr());
                pe.setOvertimeInr(p.overtimeInr());
                pe.setBonusInr(p.bonusInr());
                pe.setCommissionInr(p.commissionInr());
                pe.setHoursWorked(BigDecimal.valueOf(p.hoursWorked()));
                pe.setDeductionsInr(p.deductionsInr());
                pe.setPayFrequency(p.payFrequency());
                batch.add(pe);
                periodCount++;
                if (batch.size() >= 1000) {
                    payPeriodRepository.saveAll(batch);
                    batch.clear();
                }
            }
        }
        payPeriodRepository.saveAll(batch);
        log.info("income_seed_complete employees={} payPeriods={}", employees.size(), periodCount);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** Lookback window start; null/0 means all available (defaults to 36 months). */
    public static LocalDate lookbackStart(Integer months) {
        int m = months == null || months <= 0 ? 36 : months;
        return LocalDate.now().minusMonths(m);
    }

    private double coefficientOfVariation(List<PayPeriodEntity> periods) {
        if (periods.size() < 2) {
            return 0.0;
        }
        double mean = periods.stream().mapToLong(PayPeriodEntity::getGrossPayInr).average().orElse(0);
        if (mean == 0) {
            return 0.0;
        }
        double variance = periods.stream()
                .mapToDouble(p -> Math.pow(p.getGrossPayInr() - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance) / mean;
    }

    private VerificationResponse persist(String employeeNumber, IncomeRecordEntity income, String result,
            double confidence, Integer lookbackMonths, Map<String, Object> details, String requestId) {
        IncomeVerificationEntity e = new IncomeVerificationEntity();
        e.setEmployeeNumber(employeeNumber);
        if (income != null) {
            e.setEmployerName(income.getEmployerName());
            e.setAnnualizedIncome(income.getAnnualTotalInr());
            e.setMonthlyIncome(income.getMonthlyTotalInr());
            e.setPayFrequency(income.getPayFrequency());
        }
        e.setResult(result);
        e.setConfidence(BigDecimal.valueOf(confidence));
        e.setLookbackMonths(lookbackMonths);
        e.setRequestId(requestId);
        try {
            e.setDetails(MAPPER.writeValueAsString(details));
        } catch (Exception ignored) {
            // best effort
        }
        e = verificationRepository.save(e);
        log.info("income_verified id={} employee={} result={}", e.getId(), employeeNumber, result);
        return toResponse(e);
    }

    private VerificationResponse toResponse(IncomeVerificationEntity e) {
        Map<String, Object> details = Map.of();
        if (e.getDetails() != null) {
            try {
                details = MAPPER.readValue(e.getDetails(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        });
            } catch (Exception ignored) {
                // best effort
            }
        }
        return new VerificationResponse(e.getId(), e.getEmployeeNumber(), e.getEmployerName(), e.getResult(),
                e.getConfidence().doubleValue(), e.getLookbackMonths(), e.getAnnualizedIncome(),
                e.getMonthlyIncome(), e.getPayFrequency(), details, e.getCreatedAt());
    }
}

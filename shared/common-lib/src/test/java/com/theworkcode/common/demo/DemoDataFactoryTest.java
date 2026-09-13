package com.theworkcode.common.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DemoDataFactoryTest {

    @Test
    void generatesTwentyEmployersDeterministically() {
        List<DemoEmployer> a = DemoDataFactory.employers();
        List<DemoEmployer> b = DemoDataFactory.employers();
        assertEquals(20, a.size());
        assertEquals(a, b, "same seed must reproduce identical employers");
        assertEquals("Acme Technologies Pvt Ltd", a.get(0).name());
    }

    @Test
    void generatesDeterministicEmployees() {
        List<DemoEmployee> a = DemoDataFactory.employees(500);
        List<DemoEmployee> b = DemoDataFactory.employees(500);
        assertEquals(500, a.size());
        assertEquals(a, b);
    }

    @Test
    void employeeNumbersAreUnique() {
        List<DemoEmployee> all = DemoDataFactory.employees(500);
        Set<String> ids = new HashSet<>();
        for (DemoEmployee e : all) {
            ids.add(e.employeeNumber());
        }
        assertEquals(all.size(), ids.size(), "employee numbers must be unique");
    }

    @Test
    void scenarioEmployeesPresent() {
        List<DemoEmployee> all = DemoDataFactory.employees(200);
        Set<String> ids = new HashSet<>();
        for (DemoEmployee e : all) {
            ids.add(e.employeeNumber());
        }
        assertTrue(ids.contains(DemoDataFactory.SCENARIO_PERFECT));
        assertTrue(ids.contains(DemoDataFactory.SCENARIO_MISMATCH));
        assertTrue(ids.contains(DemoDataFactory.SCENARIO_INCOME));
        assertTrue(ids.contains(DemoDataFactory.SCENARIO_FRAUD));
        assertTrue(ids.contains(DemoDataFactory.SCENARIO_MANUAL));
    }

    @Test
    void terminatedEmployeesHaveTerminationDateAfterHire() {
        List<DemoEmployee> all = DemoDataFactory.employees(500);
        for (DemoEmployee e : all) {
            if (DemoEmployee.STATUS_TERMINATED.equals(e.status())) {
                assertNotNull(e.terminationDate(), "terminated employee must have a termination date: " + e.employeeNumber());
                assertFalse(e.terminationDate().isBefore(e.hireDate()));
            } else {
                assertTrue(e.terminationDate() == null || !e.terminationDate().isBefore(e.hireDate()));
            }
        }
    }

    @Test
    void employmentHistoryDoesNotOverlap() {
        List<DemoEmployee> all = DemoDataFactory.employees(120);
        for (DemoEmployee e : all) {
            List<DemoEmploymentRecord> history = DemoDataFactory.employmentHistory(e);
            assertTrue(history.size() >= 1);
            // records are appended current-first; ensure each prior tenure ends before the next starts
            for (int i = 0; i < history.size() - 1; i++) {
                LocalDate laterStart = history.get(i).hireDate();
                LocalDate earlierEnd = history.get(i + 1).terminationDate();
                assertNotNull(earlierEnd, "historical tenure must be terminated");
                assertTrue(earlierEnd.isBefore(laterStart),
                        "tenures must not overlap for " + e.employeeNumber());
            }
            assertEquals(e.employerName(), history.get(0).employerName());
        }
    }

    @Test
    void payPeriodsAreConsistentWithFrequencyAndIncome() {
        DemoEmployee perfect = DemoDataFactory.scenarioPerfect();
        List<DemoPayPeriod> periods = DemoDataFactory.payPeriods(perfect, DemoDataFactory.defaultLookbackStart());
        assertFalse(periods.isEmpty());
        for (DemoPayPeriod p : periods) {
            assertEquals("MONTHLY", p.payFrequency());
            long expectedGross = p.basePayInr() + p.overtimeInr() + p.bonusInr() + p.commissionInr();
            assertEquals(expectedGross, p.grossPayInr());
            assertTrue(p.deductionsInr() > 0);
            assertEquals(p.grossPayInr() - p.deductionsInr(), p.netPayInr());
        }
        // monthly base of 70,000 -> monthly base pay matches (some months may add bonus/OT)
        long basePayments = periods.stream().filter(p -> p.bonusInr() == 0 && p.overtimeInr() == 0 && p.commissionInr() == 0)
                .mapToLong(DemoPayPeriod::basePayInr).distinct().count();
        assertTrue(basePayments >= 1);
    }

    @Test
    void annualizedIncomeConsistentWithMonthly() {
        DemoEmployee e = DemoDataFactory.scenarioPerfect();
        assertEquals(840_000L, e.monthlyBaseIncomeInr() * 12);
    }

    @Test
    void employerCodeIsStable() {
        assertEquals("EMP-ATP", DemoDataFactory.employerCode("Acme Technologies Pvt Ltd").substring(0, 7));
        String code = DemoDataFactory.employerCode("Nova Systems India");
        assertEquals(code, DemoDataFactory.employerCode("Nova Systems India"));
    }
}

package com.theworkcode.employee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seeds the deterministic demo employee dataset at startup when enabled
 * (default true) and the table is empty. Set SEED_DEMO_DATA=false to skip.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "workcode.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class EmployeeSeedRunner implements ApplicationRunner {

    private final EmployeeService employeeService;

    @Override
    public void run(ApplicationArguments args) {
        long start = System.nanoTime();
        employeeService.seedDemoDataIfEmpty();
        log.info("employee_seed_runner_done durationMs={}", (System.nanoTime() - start) / 1_000_000);
    }
}

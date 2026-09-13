package com.theworkcode.audit.config;

import com.theworkcode.audit.service.AuditService;
import com.theworkcode.common.api.GlobalExceptionHandler;
import com.theworkcode.common.correlation.CorrelationIdFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Configuration
@Import({ GlobalExceptionHandler.class })
public class AuditServiceConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/api/*", "/actuator/*");
        registration.setOrder(1);
        return registration;
    }

    @Component
    @RequiredArgsConstructor
    @ConditionalOnProperty(name = "workcode.seed-demo-data", havingValue = "true", matchIfMissing = true)
    public static class AuditSeedRunner implements ApplicationRunner {
        private final AuditService auditService;

        @Override
        public void run(ApplicationArguments args) {
            String[] actions = { "LOGIN", "CREATE_VERIFICATION", "VIEW_EMPLOYEE", "DOWNLOAD_REPORT",
                    "CREATE_API_KEY", "SUBMIT_DISPUTE", "RERUN_VERIFICATION", "VIEW_VERIFICATION",
                    "EXPORT_BATCH", "UPDATE_DISPUTE", "CREATE_WEBHOOK", "REVOKE_API_KEY",
                    "VIEW_INCOME", "LOGOUT", "LOGIN", "DOWNLOAD_REPORT" };
            String[] actors = { "admin", "analyst", "developer", "viewer" };
            for (int i = 0; i < 50; i++) {
                auditService.record(
                        actors[i % actors.length],
                        i % 4 == 0 ? "ADMIN" : i % 4 == 1 ? "ANALYST" : i % 4 == 2 ? "DEVELOPER" : "VIEWER",
                        actions[i % actions.length],
                        switch (actions[i % actions.length]) {
                            case "LOGIN", "LOGOUT" -> "SESSION";
                            case "CREATE_VERIFICATION", "VIEW_VERIFICATION", "RERUN_VERIFICATION" -> "VERIFICATION";
                            case "DOWNLOAD_REPORT" -> "REPORT";
                            case "CREATE_API_KEY", "REVOKE_API_KEY" -> "API_KEY";
                            case "SUBMIT_DISPUTE", "UPDATE_DISPUTE" -> "DISPUTE";
                            case "EXPORT_BATCH" -> "BATCH";
                            default -> "EMPLOYEE";
                        },
                        "SEED-" + (1000 + i),
                        "REQ-SEED-" + (1000 + i),
                        "SUCCESS",
                        "Seeded demo audit event",
                        "ORG-8821");
            }
        }
    }
}

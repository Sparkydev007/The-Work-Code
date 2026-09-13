package com.theworkcode.employee.config;

import com.theworkcode.common.api.GlobalExceptionHandler;
import com.theworkcode.common.correlation.CorrelationIdFilter;
import com.theworkcode.common.security.JwtSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Registers the shared correlation filter and global exception handler.
 */
@Configuration
@Import({ GlobalExceptionHandler.class })
public class EmployeeServiceConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter(
            @Value("${workcode.jwt-secret:demo-secret-key-change-me-0123456789abcdef}") String jwtSecret) {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/api/*", "/actuator/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public JwtSupport serviceJwtSupport(
            @Value("${workcode.jwt-secret:demo-secret-key-change-me-0123456789abcdef}") String jwtSecret) {
        // Services can validate forwarded identity claims if needed.
        return new JwtSupport(jwtSecret, 3600);
    }
}

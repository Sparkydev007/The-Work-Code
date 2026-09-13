package com.theworkcode.report.config;

import com.theworkcode.common.api.GlobalExceptionHandler;
import com.theworkcode.common.correlation.CorrelationIdFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ GlobalExceptionHandler.class })
public class ReportServiceConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/api/*", "/actuator/*");
        registration.setOrder(1);
        return registration;
    }
}

package com.theworkcode.notification.config;

import com.theworkcode.common.api.GlobalExceptionHandler;
import com.theworkcode.common.correlation.CorrelationIdFilter;
import com.theworkcode.notification.service.WebhookService;

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
public class NotificationServiceConfig {

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
    public static class WebhookSeedRunner implements ApplicationRunner {
        private final WebhookService webhookService;

        @Override
        public void run(ApplicationArguments args) {
            webhookService.seedDemoIfEmpty();
        }
    }
}

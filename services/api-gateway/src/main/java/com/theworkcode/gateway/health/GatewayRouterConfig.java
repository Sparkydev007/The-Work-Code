package com.theworkcode.gateway.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Gateway routes the aggregate health endpoint and configures CORS.
 */
@Configuration
public class GatewayRouterConfig {

    private final HealthAggregator healthAggregator;

    public GatewayRouterConfig(HealthAggregator healthAggregator) {
        this.healthAggregator = healthAggregator;
    }

    @Bean
    public RouterFunction<ServerResponse> healthRoute() {
        return RouterFunctions.route(GET("/api/health"), healthAggregator::health);
    }

    @Bean
    public WebFluxConfigurer corsConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://localhost:5500",
                                "http://127.0.0.1:5500",
                                "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("X-Request-ID")
                        .allowCredentials(true);
            }
        };
    }
}

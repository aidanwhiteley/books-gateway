package com.aidanwhiteley.books.gateway;

import com.aidanwhiteley.books.gateway.config.RoutesConfigProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GatewayRouting {

    @Bean
    public RouteLocator booksRoutes(RouteLocatorBuilder builder, RoutesConfigProperties routesConfigProperties) {

        return builder.routes()
                .route(p -> p
                        .path(routesConfigProperties.getReadOnlyApisPath())
                        .filters(f -> f.addRequestHeader("x-via-gateway-route", "ReadOnly").
                                hystrix(config -> config.setName("readCommand").
                                        setFallbackUri("forward:/fallback")).
                                filter(new ThrottleGatewayFilter(
                                    routesConfigProperties.getReadOnlyThrottleCapacity(),
                                    routesConfigProperties.getReadOnlyThrottleRefillTokens(),
                                    routesConfigProperties.getReadOnlyThrottleRefillPeriod(),
                                    routesConfigProperties.getReadOnlyThrottleRefillTimeUnit(),
                                    "ReadOnlyThrottle")))
                        .uri(routesConfigProperties.getUri()))
                .route(p -> p
                        .path(routesConfigProperties.getUpdateApisPath())
                        .filters(f -> f.addRequestHeader("x-via-gateway-route", "Update").
                                hystrix(config -> config.setName("updateCommand").
                                        setFallbackUri("forward:/fallback")).
                                filter(new ThrottleGatewayFilter(
                                    routesConfigProperties.getUpdateThrottleCapacity(),
                                    routesConfigProperties.getUpdateThrottleRefillTokens(),
                                    routesConfigProperties.getUpdateThrottleRefillPeriod(),
                                    routesConfigProperties.getUpdateThrottleRefillTimeUnit(),
                                        "UpdateThrottle")))
                        .uri(routesConfigProperties.getUri()))
                .build();
    }

}

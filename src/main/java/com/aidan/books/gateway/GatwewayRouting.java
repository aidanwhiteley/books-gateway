package com.aidan.books.gateway;

import com.aidan.books.gateway.config.RoutesConfigProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.sample.ThrottleGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class GatwewayRouting {

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
                                                routesConfigProperties.getReadOnlyThrottleRefillTimeUnit())))
                        .uri(routesConfigProperties.getUri()))
                .route(p -> p
                        .path(routesConfigProperties.getUpdateApisPath())
                        .filters(f -> f.addRequestHeader("x-via-gateway-route", "Update").
                                hystrix(config -> config.setName("updateCommand").
                                        setFallbackUri("forward:/fallback")))
                        .uri(routesConfigProperties.getUri()))
                // not setting setFallbackUri - and HTTP 504 wil do nicely
                .build();
    }

}

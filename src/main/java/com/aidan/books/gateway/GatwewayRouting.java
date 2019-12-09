package com.aidan.books.gateway;

import com.aidan.books.gateway.config.RoutesConfigProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GatwewayRouting {

    @Bean
    public RouteLocator booksRoutes(RouteLocatorBuilder builder, RoutesConfigProperties routesConfigProperties, RateLimiter rateLimiter) {

        return builder.routes()
                .route(p -> p
                        .path(routesConfigProperties.getReadOnlyApisPath())
                        .filters(f -> f.addRequestHeader("x-via-gateway-route", "ReadOnly").
                                hystrix(config -> config.setName("readCommand").
                                        setFallbackUri("forward:/fallback")).
                                requestRateLimiter().configure(c -> c.setRateLimiter(rateLimiter).setDenyEmptyKey(false)))
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

    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> Mono.just("DUMMY");
    }

    @Bean
    RateLimiter getRateLimiter()  {
        return new RateLimiter() {

            public void RateLimiter() {
                System.out.println("==== Constructor called");
            }
            @Override
            public Mono<Response> isAllowed(String routeId, String id) {
                System.out.println("==== isAllowed: routeId " + routeId);
                return Mono.just("DUMMY");
                //return null;
            }

            @Override
            public Map getConfig() {
                System.out.println("==== getConfig ");
                return null;
            }

            @Override
            public Class getConfigClass() {
                System.out.println("==== getConfigClass ");
                return null;
            }

            @Override
            public Object newConfig() {
                System.out.println("==== newConfig ");
                return null;
            }
    };

    }
}

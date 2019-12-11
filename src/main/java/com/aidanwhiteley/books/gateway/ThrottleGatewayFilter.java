/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aidanwhiteley.books.gateway;

import java.util.concurrent.TimeUnit;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

/**
 * Sample throttling filter. See https://github.com/bbeck/token-bucket
 *
 * Copied into this project from https://github.com/spring-cloud/spring-cloud-gateway/tree/master/spring-cloud-gateway-sample
 * and then amended for the very basic in memory throttling that is sufficient for this project.
 */
public class ThrottleGatewayFilter implements GatewayFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottleGatewayFilter.class);

    final TokenBucket tokenBucket;
    final String throttleName;

    public ThrottleGatewayFilter(int capacity, int refillTokens, int refillPeriod, TimeUnit refillUnit, String thottleName) {
        this.tokenBucket = TokenBuckets.builder().withCapacity(capacity)
                .withFixedIntervalRefillStrategy(refillTokens, refillPeriod, refillUnit)
                .build();
        this.throttleName = thottleName;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        logDebugInfo();

        boolean consumed = tokenBucket.tryConsume();
        if (consumed) {
            return chain.filter(exchange);
        }

        LOGGER.warn("{} throttle triggered. Seconds to next throttle refresh {}.",
                throttleName, tokenBucket.getDurationUntilNextRefill(TimeUnit.SECONDS));
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private void logDebugInfo() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} thottle capacity: {}", throttleName, tokenBucket.getCapacity());
            LOGGER.debug("{} thottle tokens: {}", throttleName, tokenBucket.getNumTokens());
            LOGGER.debug("{} thottle seconds to next bucket refill: {}", throttleName,
                    tokenBucket.getDurationUntilNextRefill(TimeUnit.SECONDS));
        }
    }

}
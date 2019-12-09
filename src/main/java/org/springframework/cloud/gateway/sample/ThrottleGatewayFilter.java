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

package org.springframework.cloud.gateway.sample;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

/**
 * Sample throttling filter. See https://github.com/bbeck/token-bucket
 *
 * Copied into this project from https://github.com/spring-cloud/spring-cloud-gateway/tree/master/spring-cloud-gateway-sample
 * and then amended for very basic in memory throttling that is sufficient for this project.
 */
public class ThrottleGatewayFilter implements GatewayFilter {

    private static final Log log = LogFactory.getLog(ThrottleGatewayFilter.class);

    final TokenBucket tokenBucket;

    public ThrottleGatewayFilter(int capacity, int refillTokens, int refillPeriod, TimeUnit refillUnit) {
        this.tokenBucket = TokenBuckets.builder().withCapacity(capacity)
                .withFixedIntervalRefillStrategy(refillTokens, refillPeriod, refillUnit)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

         // TODO: get a token bucket for a key
        logDebugInfo();
        boolean consumed = tokenBucket.tryConsume();
        if (consumed) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private void logDebugInfo() {
        if (log.isDebugEnabled()) {
            log.debug("Thottle capacity: " + tokenBucket.getCapacity());
            log.debug("Thottle tokens: " + tokenBucket.getNumTokens());
            log.debug("Thottle seconds to next bucket refill: " + tokenBucket.getDurationUntilNextRefill(TimeUnit.SECONDS));
        }
    }

}
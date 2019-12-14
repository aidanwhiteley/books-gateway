package com.aidanwhiteley.books.gateway;

import com.netflix.hystrix.exception.HystrixTimeoutException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.HYSTRIX_EXECUTION_EXCEPTION_ATTR;

@RestController
public class FallbackRoute {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackRoute.class);

    @GetMapping(value = "/fallback")
    public Mono<ErrorMessage> fallback(ServerWebExchange serverWebExchange) {
        String errMsg = "Unknown error - Tong, Pete gone it has";
        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        if (serverWebExchange.getAttribute(HYSTRIX_EXECUTION_EXCEPTION_ATTR) instanceof HystrixTimeoutException) {
            errMsg = "Timeout - the downstream system took too long to respond";
            LOGGER.error(errMsg);
            statusCode = HttpStatus.GATEWAY_TIMEOUT;
        } else if (serverWebExchange.getAttribute(HYSTRIX_EXECUTION_EXCEPTION_ATTR) instanceof  org.springframework.cloud.gateway.support.NotFoundException) {
                errMsg = "Cant find downsteam system: " + ((NotFoundException) serverWebExchange.getAttribute(HYSTRIX_EXECUTION_EXCEPTION_ATTR)).getMessage();
                LOGGER.error(errMsg);
                statusCode = HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            LOGGER.error("Unexpected error: " + serverWebExchange.getAttribute(HYSTRIX_EXECUTION_EXCEPTION_ATTR).toString() );
            serverWebExchange.getAttributes().forEach((k, v) -> LOGGER.error("Key: " + k + "  value: " + v));
        }

        serverWebExchange.getResponse().setStatusCode(statusCode);
        return Mono.just(new ErrorMessage(errMsg, statusCode.value()));
    }

    @Data
    @AllArgsConstructor
    static class ErrorMessage {
        private String msg;
        private int code;
    }
}

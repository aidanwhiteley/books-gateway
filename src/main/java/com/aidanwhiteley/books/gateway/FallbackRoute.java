package com.aidanwhiteley.books.gateway;

import com.netflix.hystrix.exception.HystrixTimeoutException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.HYSTRIX_EXECUTION_EXCEPTION_ATTR;

@RestController
public class FallbackRoute {

    @RequestMapping(value = "/fallback", method = RequestMethod.GET)
    public Mono<ErrorMessage> fallback(ServerWebExchange serverWebExchange) {
        String errMsg = "Unknown error - Tong, Pete gone it has";
        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        if (serverWebExchange.getAttribute(HYSTRIX_EXECUTION_EXCEPTION_ATTR) instanceof HystrixTimeoutException) {
            errMsg = "Timeout - the downstream system took too long to respond";
            statusCode = HttpStatus.GATEWAY_TIMEOUT;
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

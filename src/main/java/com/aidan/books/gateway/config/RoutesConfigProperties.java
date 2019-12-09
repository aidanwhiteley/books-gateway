package com.aidan.books.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// @Component shouldnt be needed as of Spring Boot 2.2 but IntelliJ complains without it.
@Component
@ConfigurationProperties(prefix = "books.gateway")
@Data
public class RoutesConfigProperties {

    private String uri;
    private String readOnlyApisPath;
    private String updateApisPath;

    private int readOnlyThrottleCapacity;
    private int readOnlyThrottleRefillTokens;
    private int readOnlyThrottleRefillPeriod;
    private TimeUnit readOnlyThrottleRefillTimeUnit;

    private int updateThrottleCapacity;
    private int updateThrottleRefillTokens;
    private int updateThrottleRefillPeriod;
    private TimeUnit updateThrottleRefillTimeUnit;

}

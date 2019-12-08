package com.aidan.books.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

// @Component shouldnt be needed as of Spring Boot 2.2 but IntelliJ complains without it.
@Component
@ConfigurationProperties(prefix = "books.gateway")
@Data
public class RoutesConfigProperties {

    private String uri;
    private String readOnlyApisPath;
    private String updateApisPath;

}

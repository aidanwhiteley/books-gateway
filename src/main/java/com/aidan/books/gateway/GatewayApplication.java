package com.aidan.books.gateway;

import com.aidan.books.gateway.config.RoutesConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	// Based on https://spring.io/guides/gs/gateway/

	@Bean
	public RouteLocator booksRoutes(RouteLocatorBuilder builder, RoutesConfigProperties routesConfigProperties) {
		return builder.routes()
				.route(p -> p
						.path(routesConfigProperties.getReadOnlyApisPath())
						.filters(f -> f.addRequestHeader("Hello", "ReadOnly"))
						.uri(routesConfigProperties.getUri()))
				.route(p -> p
						.path(routesConfigProperties.getUpdateApisPath())
						.filters(f -> f.hystrix(config -> config.setName("myupdatecmd")))
						.uri(routesConfigProperties.getUri()))
						// not setting setFallbackUri - and HTTP 504 wil do nicely
				.build();
	}

}

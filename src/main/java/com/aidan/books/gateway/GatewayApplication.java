package com.aidan.books.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
	public RouteLocator booksRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> f.addRequestHeader("Hello", "World"))
						.uri("http://httpbin.org:80"))
				.route(p -> p
						.host("*.hystrix.com")
						.filters(f -> f.hystrix(config -> config.setName("mycmd")))
						.uri("http://httpbin.org:80"))
						// not setting setFallbackUri - and HTTP 504 wil do nicely
				.build();
	}

}

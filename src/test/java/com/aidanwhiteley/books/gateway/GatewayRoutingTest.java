package com.aidanwhiteley.books.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"books.gateway.uri=http://localhost:${wiremock.server.port}",
                "hystrix.command.readCommand.execution.isolation.thread.timeoutInMilliseconds=500"})
@AutoConfigureWireMock(port = 0)
public class GatewayRoutingTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void shouldFindValidRouteOk() {

        stubFor(get(urlEqualTo("/api/books"))
                .willReturn(aResponse()
                        .withBody("{\"message\":{\"greeting\":\"Hello World\"}}")
                        .withHeader("Content-Type", "application/json")));

        webClient
                .get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message.greeting").isEqualTo("Hello World");
    }

    @Test
    public void shouldNotFindUnknownRoute() {

        stubFor(get(urlEqualTo("/unknownPath"))
                .willReturn(aResponse()
                        .withBody("{\"message\":{\"greeting\":\"Unknown World\"}}")
                        .withHeader("Content-Type", "application/json")));

        webClient
                .get().uri("/unknownPath")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void shouldFailOnSlowResponse() {
        stubFor(get(urlEqualTo("/api/books"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)));

        webClient
                .get().uri("/api/books")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    public void shouldFindUpdateApiRouteOk() {
        stubFor(get(urlEqualTo("/secure/api/user"))
                .willReturn(aResponse()
                        .withBody("{\"users\":{\"user\":\"Jim Bowen\"}}")
                        .withHeader("Content-Type", "application/json")));

        webClient
                .get().uri("/secure/api/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.users.user").isEqualTo("Jim Bowen");
    }

}

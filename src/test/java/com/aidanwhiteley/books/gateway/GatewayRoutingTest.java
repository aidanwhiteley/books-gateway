package com.aidanwhiteley.books.gateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"books.gateway.uri=http://localhost:${wiremock.server.port}",
                "hystrix.command.readCommand.execution.isolation.thread.timeoutInMilliseconds=500",
                "books.gateway.readOnlyThrottleCapacity=5"})
@AutoConfigureWireMock(port = 0)
public class GatewayRoutingTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void shouldFindValidRouteOk() {
        // Basing test on https://spring.io/guides/gs/gateway/

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
                        .withFixedDelay(2000)));

        webClient
                .get().uri("/api/books")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    public void shouldFailOnTooManyRequests() {

    }

    @Test
    public void shouldFindUpdateApiRouteOk() {

    }

}

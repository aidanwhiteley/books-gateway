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
                "books.gateway.readOnlyThrottleCapacity=5"})
@AutoConfigureWireMock(port = 0)
/*
  The throttling tests are in this seperate class to ensure that the test below that uses
  up all the throttle "tokens" doesnt then affect the other gateway routing tests
  (as a new Spring context is created for each test class).
 */
public class GatewayRoutingThottlingTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void shouldThrottleRequestWhenAllTokensUsed() {

        stubFor(get(urlEqualTo("/api/books"))
                .willReturn(aResponse()
                        .withBody("{\"message\":{\"greeting\":\"Hello World\"}}")
                        .withHeader("Content-Type", "application/json")));

        for (int i = 0; i <= 4; i++){
            webClient
                    .get().uri("/api/books")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.message.greeting").isEqualTo("Hello World");
        }

        webClient
                .get().uri("/api/books")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

}

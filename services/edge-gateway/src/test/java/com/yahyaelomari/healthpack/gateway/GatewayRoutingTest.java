package com.yahyaelomari.healthpack.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Proves that the patient-service route already declared in
 * application.properties actually proxies a request — nothing here writes a
 * {@code RouteLocator} bean, which is the whole point: the 13 routes need no
 * Java code to work.
 *
 * <p>No property is overridden here, on purpose. Adding or changing any
 * {@code spring.cloud.gateway.server.webflux.routes[N]} entry from a test
 * consistently fails with {@code UnboundConfigurationPropertiesException}
 * from {@code GatewayProperties}' own list binding — confirmed with a
 * minimal, isolated probe: a brand-new index nothing else ever defines,
 * added purely in memory via {@code @DynamicPropertySource} with no
 * properties file involved at all, on a clean build. Same failure every
 * time. This appears to be a genuine limitation of how this Spring Cloud
 * Gateway release binds that specific {@code List<RouteDefinition>}
 * property from test-time property sources, not a mistake in how it was
 * attempted — the identical property shape binds correctly in a real run
 * (verified when all 15 services were boot-tested earlier in this project).
 *
 * <p>So instead: the stub listens on the exact port patient-service's route
 * already targets ({@code localhost:8502}), and nothing about the gateway's
 * configuration is touched. There is no binding path left to go wrong. The
 * trade-off is that this test cannot run at the same time as a real
 * patient-service instance bound to that same port locally.
 *
 * <p>Security isn't wired up yet for the gateway's own default behaviour in
 * this test — {@code SecurityConfig} is excluded here via
 * {@code @Profile("!test")} + {@code @ActiveProfiles("test")}, and the
 * nested {@code PermissiveSecurityConfig} below stands in for it. Real JWT
 * validation is covered separately in {@code GatewayJwtSecurityTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayRoutingTest {

    // The port patient-service's route already targets by default
    // (PATIENT_SERVICE_URI in application.properties) — not a free choice.
    private static final int PATIENT_SERVICE_PORT = 8502;

    private static final WireMockServer PATIENT_SERVICE_STUB = new WireMockServer(PATIENT_SERVICE_PORT);

    static {
        PATIENT_SERVICE_STUB.start();
    }

    @Autowired
    private WebTestClient webTestClient;

    @AfterAll
    static void stopStub() {
        PATIENT_SERVICE_STUB.stop();
    }

    @Test
    void aPatientRequestIsProxiedToPatientService() {
        PATIENT_SERVICE_STUB.stubFor(get(urlEqualTo("/api/v1/patients/123"))
                .willReturn(aResponse().withStatus(200).withBody("stub patient response")));

        webTestClient.get().uri("/api/v1/patients/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("stub patient response");
    }

    @TestConfiguration
    static class PermissiveSecurityConfig {

        @Bean
        SecurityWebFilterChain permitAll(ServerHttpSecurity http) {
            return http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .build();
        }
    }
}

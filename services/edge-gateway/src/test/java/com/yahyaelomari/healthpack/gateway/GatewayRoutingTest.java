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
 * <p>No property is overridden here, on purpose. Every attempt to add or
 * change a {@code spring.cloud.gateway.server.webflux.routes[N]} entry from a
 * test-time property source — {@code @DynamicPropertySource}, inline
 * {@code @SpringBootTest(properties=)}, even a real {@code .properties} file
 * via {@code @TestPropertySource}, at the existing index and at fresh ones —
 * consistently failed with {@code UnboundConfigurationPropertiesException}
 * from {@code GatewayProperties}' own list binding, for reasons that didn't
 * resolve after several genuinely different attempts. The same property
 * shape binds correctly in a real run (verified earlier when all 15 services
 * were boot-tested), so the failure is specific to how the test context
 * assembles property sources around this one binding target.
 *
 * <p>Rather than keep excavating that, this test uses the route exactly as
 * configured: patient-service's route already targets
 * {@code localhost:8502}, so the stub listens there instead. Nothing about
 * the gateway's configuration is touched, so there is no binding path left
 * to go wrong.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
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

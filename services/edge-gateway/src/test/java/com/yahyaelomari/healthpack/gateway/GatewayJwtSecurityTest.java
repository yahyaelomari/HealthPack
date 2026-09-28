package com.yahyaelomari.healthpack.gateway;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * A real Keycloak container, imported from the same realm export already
 * verified in Feat #10 — not a mock JWT, an actual token issued by an actual
 * Keycloak, validated by the actual {@link com.yahyaelomari.healthpack.gateway.config.SecurityConfig}.
 *
 * <p>Unlike the gateway's own route list (see {@link GatewayRoutingTest}'s
 * javadoc), the resource-server properties here are a plain scalar string
 * each, and {@code @DynamicPropertySource} overrides those without any of
 * the trouble the indexed route list gave — this is the ordinary, well-worn
 * path for pointing a resource server at a Testcontainers Keycloak.
 *
 * <p>This test only checks that the security layer itself is doing its job —
 * rejecting no token, accepting a real one. What happens after that (does
 * the request actually reach a downstream service) is {@link GatewayRoutingTest}'s
 * concern, not this one's.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayJwtSecurityTest {

    @Container
    static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.4")
            .withRealmImportFile("healthpack-realm.json");

    @DynamicPropertySource
    static void overrideIssuer(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> KEYCLOAK.getIssuerUrl("healthpack"));
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> KEYCLOAK.getJwksUri("healthpack"));
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void aRequestWithNoTokenIsRejected() {
        webTestClient.get().uri("/api/v1/patients/123")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void aRequestWithARealTokenPassesAuthentication() {
        String token = KEYCLOAK.getAccessToken("healthpack", "healthpack-web", "dr.doe", "changeit");

        webTestClient.get().uri("/api/v1/patients/123")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                // Not asserting 200: nothing is listening on patient-service's
                // real port in this test, so the gateway will fail to connect
                // downstream. What matters here is that it got past security
                // to attempt that at all, rather than being turned back at 401.
                .expectStatus().value(status -> org.assertj.core.api.Assertions.assertThat(status)
                        .isNotEqualTo(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void healthEndpointNeedsNoToken() {
        // Not asserting 200: Redis isn't running in this test, so the
        // aggregate health indicator can legitimately report 503 — a real
        // dependency gap, not a security one. What this test checks is that
        // the request wasn't turned back at 401 before it even got there.
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().value(status -> org.assertj.core.api.Assertions.assertThat(status)
                        .isNotEqualTo(HttpStatus.UNAUTHORIZED.value()));
    }
}

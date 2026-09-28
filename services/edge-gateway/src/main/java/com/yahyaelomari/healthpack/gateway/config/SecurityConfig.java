package com.yahyaelomari.healthpack.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Requires a valid Keycloak-issued JWT for everything except health checks.
 *
 * <p>Without this class, Spring Boot's own default (once the resource-server
 * dependency and issuer-uri are on the classpath) already requires a valid
 * JWT for every single path, health checks included — which breaks Docker's
 * own health probe, since nothing supplies a token for that. This bean is
 * what carves out the one exception.
 *
 * <p>CSRF is disabled because there is no session or cookie here to protect:
 * every request carries its own bearer token, which a forged cross-site
 * request can't attach without the client's cooperation in the first place.
 *
 * <p>{@code @Profile("!test")}: {@code GatewayRoutingTest} (in the test
 * sources) needs to test routing without a real token, and provides its own
 * permissive chain for that. A nested {@code @TestConfiguration} bean alone
 * doesn't replace this one — Spring Security ends up with two
 * {@code anyExchange()} chains and no way to know which should win.
 * Excluding this bean by profile is what actually avoids the ambiguity,
 * rather than leaving it to chance.
 */
@Configuration
@EnableWebFluxSecurity
@Profile("!test")
public class SecurityConfig {

    // Auto-configured in healthpack-common: reads Keycloak's realm_access.roles
    // instead of Spring's default scope claim, so hasRole(...) downstream
    // actually matches something. See ReactiveJwtRoleConverterAutoConfiguration.
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http, ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}

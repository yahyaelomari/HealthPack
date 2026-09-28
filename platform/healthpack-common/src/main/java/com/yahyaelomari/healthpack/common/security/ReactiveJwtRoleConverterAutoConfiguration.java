package com.yahyaelomari.healthpack.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

/**
 * Wires {@link KeycloakRealmRoleConverter} into a reactive-stack resource
 * server — edge-gateway, and any future WebFlux service.
 *
 * <p>{@link ReactiveJwtAuthenticationConverterAdapter} is just an adapter:
 * the actual role-extraction logic is the same
 * {@link KeycloakRealmRoleConverter} the servlet side uses, wrapped so it
 * returns a {@code Mono} instead of a plain object. One converter, two thin
 * stack-specific wrappers.
 */
@AutoConfiguration
@ConditionalOnClass(ReactiveJwtAuthenticationConverterAdapter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveJwtRoleConverterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactiveJwtAuthenticationConverterAdapter reactiveJwtAuthenticationConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }
}

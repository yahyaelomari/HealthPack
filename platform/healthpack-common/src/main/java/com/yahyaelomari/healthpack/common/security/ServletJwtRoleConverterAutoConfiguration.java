package com.yahyaelomari.healthpack.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Wires {@link KeycloakRealmRoleConverter} into a servlet-stack resource
 * server (patient-service and friends, once security is unblocked there).
 *
 * <p>Guarded by {@code @ConditionalOnClass}: services that never added the
 * resource-server dependency at all — document-service, billing-service —
 * never even attempt to load this. And a servlet-only service never touches
 * {@link ReactiveJwtRoleConverterAutoConfiguration}'s reactive types, because
 * that one is gated on {@code @ConditionalOnWebApplication(REACTIVE)}, which
 * a servlet app never satisfies.
 */
@AutoConfiguration
@ConditionalOnClass(JwtAuthenticationConverter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletJwtRoleConverterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}

package com.yahyaelomari.healthpack.common.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Reads the roles Keycloak actually puts on a token and turns them into
 * Spring Security authorities.
 *
 * <p>Keycloak nests realm roles under {@code realm_access.roles} — not the
 * flat {@code scope}/{@code scp} claim Spring's own default
 * {@code JwtGrantedAuthoritiesConverter} reads. Without this class,
 * {@code @PreAuthorize("hasRole('PRACTITIONER')")} would never match
 * anything: the default converter is looking at a claim Keycloak never
 * populates, so every token ends up with authorities that have nothing to
 * do with the roles actually assigned to the user.
 *
 * <p>{@code "ROLE_"} is prefixed on purpose — that's the prefix
 * {@code hasRole(...)} strips off internally before comparing, so a role
 * named {@code PRACTITIONER} in Keycloak has to arrive as the authority
 * {@code ROLE_PRACTITIONER} for {@code hasRole("PRACTITIONER")} to match it.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    @NonNull
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String.class::cast)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .toList();
    }
}

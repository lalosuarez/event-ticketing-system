package com.eventtickets.order.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts data from a JWT token roles, scopes, etc. To spring security roles and scopes, in order to do RBAC
 * when checking permissions with @PreAuthorize
 */
public class AuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserDetailsService userDetailsService;
    private final String resourceAccessKey;

    public AuthenticationConverter(UserDetailsService userDetailsService, String resourceAccessKey) {
        this.userDetailsService = userDetailsService;
        this.resourceAccessKey = resourceAccessKey;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> realmAuthorities = extractRolesFromRealmAccess(jwt);
        Collection<GrantedAuthority> resourceAuthorities = extractRolesFromResourceAccess(jwt);
        Collection<GrantedAuthority> scopes = extractScopes(jwt);
        String username = jwt.getClaimAsString("preferred_username");
        var userDetails = this.userDetailsService.loadUserByUsername(username);
        // Sets all authorities configured in both keycloak and application database.
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(realmAuthorities);
        authorities.addAll(resourceAuthorities);
        authorities.addAll(scopes);
        authorities.addAll(userDetails.getAuthorities());
        return new JwtAuthenticationToken(jwt, authorities, username);
    }

    private Collection<GrantedAuthority> extractRolesFromRealmAccess(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream()
                .map(roleName -> new SimpleGrantedAuthority(
                        "ROLE_" + roleName.replace("-", "_").toUpperCase())) // Spring expects 'ROLE_' prefix
                .collect(Collectors.toList());
    }

    private Collection<GrantedAuthority> extractRolesFromResourceAccess(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null || resourceAccess.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(this.resourceAccessKey);

        Collection<String> roles = (Collection<String>) resource.get("roles");
        return roles.stream()
                .map(roleName -> new SimpleGrantedAuthority(
                        "ROLE_" + roleName.replace("-", "_").toUpperCase())) // Spring expects 'ROLE_' prefix
                .collect(Collectors.toList());
    }

    private Collection<GrantedAuthority> extractScopes(Jwt jwt) {
        var scope = jwt.getClaimAsString("scope");
        return Arrays.stream(scope.split(" "))
                // Don't add "SCOPE_" prefix, spring automatically adds that
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

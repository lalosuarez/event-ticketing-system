package com.eventtickets.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GraphqlSecurityConfig {

    private final String audience;
    private final String issuerUri;
    private final String resourceAccessKey;
    private final List<String> corsAllowedOrigins;
    private final List<String> corsAllowedMethods;

    public GraphqlSecurityConfig(@Value("${spring.security.oauth2.resourceserver.jwt.audience}") String audience,
                                 @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
                                 @Value("${spring.security.oauth2.resourceserver.jwt.resource-access-key}") String resourceAccessKey,
                                 @Value("${gateway.graphql.cors.allowed-origins}") List<String> corsAllowedOrigins,
                                 @Value("${gateway.graphql.cors.allowed-methods}") List<String> corsAllowedMethods) {
        this.audience = audience;
        this.issuerUri = issuerUri;
        this.resourceAccessKey = resourceAccessKey;
        this.corsAllowedOrigins = corsAllowedOrigins;
        this.corsAllowedMethods = corsAllowedMethods;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                // Enable and configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // Note: If you keep CSRF enabled, GraphQL POST requests will fail without a CSRF token
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                //.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/*/graphiql/**", "/*/vendor/**").permitAll()
                        .pathMatchers("/ticket", "/order", "/payment")
                        .access((Mono<Authentication> mono, AuthorizationContext ctx) -> mono
                                .filter(Authentication::isAuthenticated)
                                .map(auth -> {
                                    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                                    boolean hasAuth = authorities.stream()
                                            .anyMatch(a -> a.getAuthority().equals("gateway:read"));
                                    return (AuthorizationResult) new AuthorizationDecision(hasAuth);
                                })
                                .defaultIfEmpty(new AuthorizationDecision(false))
                        )
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        // To convert Keycloak roles to spring security
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
                )
                .build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> authenticationConverter() {
        return new AuthenticationConverter(this.resourceAccessKey);
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        var jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromIssuerLocation(this.issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(this.audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(this.issuerUri);

        // Chain the custom Keycloak audience validator with standard issuer verification
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your exact frontend origin (Do NOT use "*" if you need to send cookies/credentials)
        configuration.setAllowedOrigins(this.corsAllowedOrigins);

        // GraphQL heavily relies on POST, but OPTIONS is used for the pre-flight check
        configuration.setAllowedMethods(this.corsAllowedMethods);

        // Explicitly allow Content-Type and Authorization headers
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Upgrade", "Connection", "X-Requested-With"));

        // Allow credentials (cookies, authorization headers) to be sent
        configuration.setAllowCredentials(true);

        // Apply this configuration to all paths, including your GraphQL endpoint (e.g., /graphql)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

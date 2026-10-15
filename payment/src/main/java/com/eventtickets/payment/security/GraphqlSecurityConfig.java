package com.eventtickets.payment.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class GraphqlSecurityConfig {

    private final String audience;
    private final String issuerUri;
    private final String resourceAccessKey;
    private final UserDetailsService userDetailsService;

    public GraphqlSecurityConfig(@Value("${spring.security.oauth2.resourceserver.jwt.audience}") String audience,
                                 @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
                                 @Value("${spring.security.oauth2.resourceserver.jwt.resource-access-key}") String resourceAccessKey,
                                 @Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService) {
        this.audience = audience;
        this.issuerUri = issuerUri;
        this.userDetailsService = userDetailsService;
        this.resourceAccessKey = resourceAccessKey;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(AbstractHttpConfigurer::disable) // Tell downstream security to stop handling CORS
                // Note: If you keep CSRF enabled, GraphQL POST requests will fail without a CSRF token
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/graphql", "/graphiql")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow unrestricted access to the gateway/router communication paths
                        // Allow everyone to hit the base endpoint; fine-grained rules happen later
                        .requestMatchers("/graphql", "/graphiql").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                                // To convert Keycloak roles to spring security
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
                        // Crucial: If token processing fails at the filter level, do not return a 401/403 HTTP response.
                        // Instead, allow the request to continue to the GraphQL servlet.
                        /*.authenticationEntryPoint((request, response, authException) -> {
                            request.getRequestDispatcher("/graphql").forward(request, response);
                        })*/
                )
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> authenticationConverter() {
        return new AuthenticationConverter(this.userDetailsService, this.resourceAccessKey);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(this.issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(this.audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(this.issuerUri);

        // Chain the custom Keycloak audience validator with standard issuer verification
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }
}

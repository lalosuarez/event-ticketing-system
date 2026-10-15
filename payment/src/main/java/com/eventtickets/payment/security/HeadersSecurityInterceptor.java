package com.eventtickets.payment.security;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: ADD JUST AN INTERCEPTOR THAT VALIDATES GATEWAY SECRET
 */
@Profile("disabled")
@Component
public class HeadersSecurityInterceptor implements WebGraphQlInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HeadersSecurityInterceptor.class);

    private final String gatewaySecret;

    public HeadersSecurityInterceptor(@Value("${gateway.graphql.shared.secret:mydefaultgateewaysecret}") String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String path = request.getUri().getPath();
        // 1. AUTOMATIC GRAPHQL BYPASS: Let GraphiQL browser assets pass through instantly
        if (path.startsWith("/graphiql") || path.equals("/favicon.ico")) {
            return chain.next(request);
        }

        // 2. Validate Gateway Perimeter Secret
        String incomingSecret = request.getHeaders().getFirst("X-Gateway-Secret");
        if (incomingSecret == null || !incomingSecret.equals(this.gatewaySecret)) {
            logger.error("Invalid X-Gateway-Secret header");
            return handleEarlyFailure(request, chain, "Forbidden", ErrorType.FORBIDDEN);
        }

        // 3. Extract Plain-Text User Identity Context
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId == null) {
            logger.error("Invalid X-User-Id header");
            return handleEarlyFailure(request, chain, "Forbidden", ErrorType.FORBIDDEN);
        }

        // 4. Map Context Roles to Granted Authorities
        String rolesStr = request.getHeaders().getFirst("X-User-Roles");
        List<GrantedAuthority> authorities = Collections.emptyList();
        if (rolesStr != null && !rolesStr.isBlank()) {
            authorities = Arrays.stream(rolesStr.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // 5. Establish the trusted internal authentication footprint
        PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(userId, null, authorities);

        // 6. Commit to ThreadLocal Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Advance to the query resolvers, writing the identity token context safely into WebFlux/Reactive paths
        return chain.next(request)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    /**
     * Overwrites the response context cleanly using the native transform builders
     */
    private Mono<WebGraphQlResponse> handleEarlyFailure(WebGraphQlRequest request, Chain chain, String message, ErrorType type) {
        // Build the standalone engine error mapping
        GraphQLError graphqlError = GraphqlErrorBuilder.newError()
                .message(message)
                .errorType(type)
                .build();

        // Execute chain minimally, then wipe out its contents using clean property setters
        return chain.next(request).map(response ->
                response.transform(builder -> builder
                        // 1. Overwrite the data payload with a null map to erase all resolver outputs completely
                        .data(null)
                        // 2. Overwrite the error array with your custom security error message list
                        .errors(Collections.singletonList(graphqlError))
                )
        );
    }
}

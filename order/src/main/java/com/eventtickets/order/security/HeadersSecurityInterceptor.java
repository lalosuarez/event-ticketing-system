package com.eventtickets.order.security;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Validates gateway secret header
 */
@Component
public class HeadersSecurityInterceptor implements WebGraphQlInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HeadersSecurityInterceptor.class);

    private final String gatewaySecret;

    public HeadersSecurityInterceptor(@Value("${gateway.graphql.secret}") String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String path = request.getUri().getPath();
        // 1. Bypass static resources
        if (path.startsWith("/graphiql") || path.equals("/favicon.ico")) {
            return chain.next(request);
        }

        // 2. Bypass GraphQL Schema Introspection queries (used by GraphiQL Docs)
        String query = request.getDocument();
        if (query.contains("__schema")) {
            return chain.next(request);
        }

        // 3. Validate Gateway Perimeter Secret for actual business queries
        String incomingSecret = request.getHeaders().getFirst("X-Gateway-Secret");
        if (incomingSecret == null || !incomingSecret.equals(this.gatewaySecret)) {
            logger.error("Invalid X-Gateway-Secret header");
            return handleEarlyFailure(request, chain, "Forbidden", ErrorType.FORBIDDEN);
        }

        // Advance to the query resolvers, writing the identity token context safely into WebFlux/Reactive paths
        return chain.next(request);
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

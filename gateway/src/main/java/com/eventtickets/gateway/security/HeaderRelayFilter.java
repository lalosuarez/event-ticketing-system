package com.eventtickets.gateway.security;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class HeaderRelayFilter implements GlobalFilter, Ordered {

    private final String gatewaySecret;

    public HeaderRelayFilter(@Value("${gateway.graphql.secret}") String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Fetch SecurityContext reactively from the WebFlux context
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .flatMap(jwt -> {
                    // Extract claims safely
                    String userId = jwt.getSubject() != null ? jwt.getSubject() : "";
                    String username = jwt.getClaimAsString("preferred_username") != null
                            ? jwt.getClaimAsString("preferred_username")
                            : "";

                    // Mutate the incoming request headers reactively
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Name", username)
                            .header("X-Gateway-Secret", this.gatewaySecret)
                            .build();

                    // Forward mutated exchange down the filter chain
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                // Fallback if request is unauthenticated or has no JWT
                .switchIfEmpty(Mono.defer(() -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-Gateway-Secret", this.gatewaySecret)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }));
    }

    @Override
    public int getOrder() {
        // High precedence so headers are injected BEFORE route forwarding
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}

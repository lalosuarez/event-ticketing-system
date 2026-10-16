package com.eventtickets.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    @Value("${gateway.graphql.secret:mydefaultgateewaysecret}")
    private String gatewaySecret;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ticket Service - HTTP GraphQL (excluding WS upgrades)
                .route("ticket-service-http", r -> r.path("/ticket")
                        .filters(f -> f.setPath("/graphql"))
                        .uri("lb://ticket-service"))

                // Order Service - Standard HTTP
                .route("order-service-http", r -> r.path("/order")
                        .filters(f -> f.setPath("/graphql"))
                        .uri("lb://order-service"))

                // Payment Service - Standard HTTP
                .route("payment-service-http", r -> r.path("/payment")
                        .filters(f -> f.setPath("/graphql"))
                        .uri("lb://payment-service"))

                .build();
    }

    /*@Bean
    public RouterFunction<ServerResponse> apolloRoute(@Value("${gateway.graphql.cors.allowed-origins}")
                                                      List<String> corsAllowedOrigins) {
        return route("graphql-service-route")
                .POST("/graphql", http())
                .filter(requestHeaderGatewaySecret(gatewaySecret))
                .filter(lb("apollo-gateway"))
                // To remove duplicated Access-Control-Allow-Origin: "*" from Apollo
                // Strip out Apollo's wildcard header completely
                .filter(removeResponseHeader("Access-Control-Allow-Origin"))
                // Inject your exact required web UI client origin
                .filter(addResponseHeader("Access-Control-Allow-Origin",
                        corsAllowedOrigins.toArray(corsAllowedOrigins.toArray(new String[0]))))
                .build();
    }*/
}

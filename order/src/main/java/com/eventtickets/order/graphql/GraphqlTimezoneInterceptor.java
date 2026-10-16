package com.eventtickets.order.graphql;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.List;

@Component
class GraphqlTimezoneInterceptor implements WebGraphQlInterceptor {

    private static final String TIMEZONE_HEADER = "X-User-Timezone"; // e.g. "America/Chicago" or "-05:00"

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        // Retrieve header (case-insensitive check)
        List<String> headerValues = request.getHeaders().get(TIMEZONE_HEADER);
        String timezoneHeader = (headerValues != null && !headerValues.isEmpty()) ? headerValues.getFirst() : null;

        ZoneId userZoneId;
        try {
            if (timezoneHeader != null && !timezoneHeader.isBlank()) {
                userZoneId = ZoneId.of(timezoneHeader.trim());
            } else {
                userZoneId = ZoneId.systemDefault();
            }
        } catch (Exception e) {
            userZoneId = ZoneId.systemDefault();
        }

        // Add to ExecutionInput context
        final ZoneId finalZoneId = userZoneId;
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(ctx -> ctx.put(GraphqlConfiguration.CONTEXT_USER_ZONE_ID, finalZoneId))
                        .build()
        );

        return chain.next(request);
    }
}

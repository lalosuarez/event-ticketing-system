package com.eventtickets.order.graphql;

import graphql.GraphQLContext;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.graphql.server.WebGraphQlInterceptor;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
class GraphqlConfiguration {
    public static final String CONTEXT_USER_ZONE_ID = "userZoneId";

    private static final DateTimeFormatter ISO_LOCAL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");

    private static final DateTimeFormatter ISO_UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.GraphQLBigDecimal)
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.DateTime)
                .scalar(dateTimeUtcScalar())
                .scalar(dateTimeLocalScalar());
    }

    // Scalar for UTC
    private GraphQLScalarType dateTimeUtcScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTimeUtc")
                .description("ISO-8601 UTC DateTime Scalar")
                .coercing(new Coercing<OffsetDateTime, String>() {
                    @Override
                    public String serialize(@NonNull Object dataFetcherResult, @NonNull GraphQLContext context,
                                            java.util.@NonNull Locale locale) {
                        if (dataFetcherResult instanceof OffsetDateTime dt) {
                            return dt.withOffsetSameInstant(ZoneOffset.UTC).format(ISO_UTC_FORMATTER);
                        }
                        throw new CoercingSerializeException("Expected OffsetDateTime object.");
                    }
                })
                .build();
    }

    // Scalar for Client-Local Timezone
    private GraphQLScalarType dateTimeLocalScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTimeLocal")
                .description("ISO-8601 Client Localized DateTime Scalar")
                .coercing(new Coercing<OffsetDateTime, String>() {
                    @Override
                    public String serialize(@NonNull Object dataFetcherResult, @NonNull GraphQLContext context,
                                            java.util.@NonNull Locale locale) {
                        if (dataFetcherResult instanceof OffsetDateTime dt) {
                            ZoneId zoneId = context.getOrDefault(CONTEXT_USER_ZONE_ID, ZoneOffset.systemDefault());
                            return dt.atZoneSameInstant(zoneId).format(ISO_LOCAL_FORMATTER);
                        }
                        throw new CoercingSerializeException("Expected OffsetDateTime object.");
                    }
                })
                .build();
    }

    @Bean
    public WebGraphQlInterceptor timezoneHeaderInterceptor() {
        return new GraphqlTimezoneInterceptor();
    }
}


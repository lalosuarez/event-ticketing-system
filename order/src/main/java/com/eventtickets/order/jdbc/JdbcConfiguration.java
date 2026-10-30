package com.eventtickets.order.jdbc;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    private final ObjectMapper objectMapper;

    public JdbcConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<?> userConverters() {
        return List.of(
                new JsonNodeConverters.PGobjectToJsonNodeConverter(objectMapper),
                new JsonNodeConverters.JsonNodeToPGobjectConverter()
        );
    }
}

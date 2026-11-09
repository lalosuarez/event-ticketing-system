package com.eventtickets.payment.outbox.jdbc;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.SQLException;

class JsonNodeConverters {

    @ReadingConverter
    static class PGobjectToJsonNodeConverter implements Converter<PGobject, JsonNode> {
        private final ObjectMapper objectMapper;

        public PGobjectToJsonNodeConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public JsonNode convert(PGobject source) {
            String json = source.getValue();
            if (json == null || json.isBlank()) {
                return null;
            }
            try {
                return objectMapper.readTree(json);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse PGobject JSON into JsonNode", e);
            }
        }
    }

    @WritingConverter
    static class JsonNodeToPGobjectConverter implements Converter<JsonNode, PGobject> {
        @Override
        public PGobject convert(JsonNode source) {
            if (source == null || source.isNull()) {
                return null;
            }
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            try {
                pgObject.setValue(source.toString());
            } catch (SQLException e) {
                throw new IllegalArgumentException("Failed to write JsonNode to PGobject", e);
            }
            return pgObject;
        }
    }
}

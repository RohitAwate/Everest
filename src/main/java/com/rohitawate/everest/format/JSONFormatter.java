package com.rohitawate.everest.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * Formats JSON strings using Jackson's ObjectMapper.
 */
public class JSONFormatter implements Formatter {
    private static ObjectMapper mapper;

    JSONFormatter() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public String format(String unformatted) throws IOException {
        JsonNode tree = mapper.readTree(unformatted);
        return mapper.writeValueAsString(tree);
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName();
    }
}

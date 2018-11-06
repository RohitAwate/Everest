package com.rohitawate.everest.server.mock;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Endpoint {
    String path;
    String resource;
    int responseCode;

    String serializationFormat;

    public Endpoint(String path, int responseCode, String resource, String serializationFormat) throws JsonProcessingException {
        this.path = path;
        this.resource = resource;
        this.responseCode = responseCode;
        this.serializationFormat = serializationFormat;
    }
}

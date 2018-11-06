package com.rohitawate.everest.server.mock;

public class Endpoint {
    String path;
    String resource;
    int responseCode;

    String serializationFormat;

    public Endpoint(String path, int responseCode, String resource, String serializationFormat) {
        this.path = path;
        this.resource = resource;
        this.responseCode = responseCode;
        this.serializationFormat = serializationFormat;
    }
}

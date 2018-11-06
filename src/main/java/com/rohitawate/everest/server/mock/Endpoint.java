package com.rohitawate.everest.server.mock;

public class Endpoint {
    String method;
    String path;
    String resource;
    int responseCode;

    String serializationFormat;

    public Endpoint(String method, String path, int responseCode, String resource, String serializationFormat) {
        this.method = method;
        this.path = path;
        this.resource = resource;
        this.responseCode = responseCode;
        this.serializationFormat = serializationFormat;
    }
}

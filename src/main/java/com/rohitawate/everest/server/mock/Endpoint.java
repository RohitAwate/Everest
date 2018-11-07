package com.rohitawate.everest.server.mock;

public class Endpoint {
    public String method;
    public String path;
    public String resource;
    public int responseCode;

    public String contentType;

    public Endpoint(String method, String path, int responseCode, String resource, String contentType) {
        this.method = method;
        this.path = path;
        this.resource = resource;
        this.responseCode = responseCode;
        this.contentType = contentType;
    }
}

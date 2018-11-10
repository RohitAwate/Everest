package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.models.requests.HTTPConstants;

import javax.ws.rs.core.MediaType;

public class Endpoint {
    public String method;
    public String path;
    public String resource;
    public int responseCode;
    public int latency;

    public String contentType;

    public Endpoint() {
        this.method = HTTPConstants.GET;
        this.path = "";
        this.resource = "{\n\t\n}";
        this.responseCode = 200;
        this.contentType = MediaType.APPLICATION_JSON;
    }

    public Endpoint(String method, String path, int responseCode, String resource, String contentType) {
        this.method = method;
        this.path = path;
        this.resource = resource;
        this.responseCode = responseCode;
        this.contentType = contentType;
    }
}

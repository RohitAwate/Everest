package com.rohitawate.everest.models.requests;

import javax.ws.rs.core.MediaType;

public class HTTPConstants {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";

    public static final String PLAIN_TEXT = "PLAIN TEXT";
    public static final String JSON = "JSON";
    public static final String XML = "XML";
    public static final String HTML = "HTML";

    public static String getSimpleContentType(String contentType) {
        switch (contentType) {
            case MediaType.APPLICATION_JSON:
                return JSON;
            case MediaType.APPLICATION_XML:
                return XML;
            case MediaType.TEXT_HTML:
                return HTML;
            default:
                return PLAIN_TEXT;
        }
    }

    public static String getComplexContentType(String contentType) {
        switch (contentType) {
            case JSON:
                return MediaType.APPLICATION_JSON;
            case XML:
                return MediaType.APPLICATION_XML;
            case HTML:
                return MediaType.TEXT_HTML;
            default:
                return MediaType.TEXT_PLAIN;
        }
    }
}

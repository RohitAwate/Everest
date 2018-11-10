/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

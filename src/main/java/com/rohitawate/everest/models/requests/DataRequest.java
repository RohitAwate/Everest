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

import java.util.HashMap;

/**
 * Represents HTTP requests which use the HTTP POST, PUT and PATCH methods.
 */
public class DataRequest extends EverestRequest {
    private String requestType;
    private String body;
    private String contentType;
    private HashMap<String, String> stringTuples;
    private HashMap<String, String> fileTuples;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setStringTuples(HashMap<String, String> stringTuples) {
        this.stringTuples = stringTuples;
    }

    public HashMap<String, String> getStringTuples() {
        return stringTuples;
    }

    public HashMap<String, String> getFileTuples() {
        return fileTuples;
    }

    public void setFileTuples(HashMap<String, String> fileTuples) {
        this.fileTuples = fileTuples;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
}

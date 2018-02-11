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

package com.rohitawate.restaurant.models.requests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Represents HTTP requests which contain data viz. POST and PUT.
 */
public class DataDispatchRequest extends RestaurantRequest {
    private String requestType;
    private String body;
    private String contentType;
    private HashMap<String, String> stringTuples;
    private HashMap<String, String> fileTuples;

    public DataDispatchRequest(String requestType) {
        this.requestType = requestType;
    }

    public DataDispatchRequest(URL target, String requestType) {
        super(target);
        this.requestType = requestType;
    }

    public DataDispatchRequest(String target, String requestType) throws MalformedURLException {
        super(target);
        this.requestType = requestType;
    }

    public void setTarget(String target) throws MalformedURLException {
        this.target = new URL(target);
    }

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
}

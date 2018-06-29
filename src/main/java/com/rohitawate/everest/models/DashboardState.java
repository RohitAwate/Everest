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

package com.rohitawate.everest.models;

import com.rohitawate.everest.models.requests.DataDispatchRequest;

import java.util.HashMap;

/**
 * Convenience class to abstract the state of the application.
 */
public class DashboardState extends DataDispatchRequest {
    private HashMap<String, String> params;
    private String httpMethod;

    public DashboardState() {

    }

    /*
        Special copy constructor to instantiate DashboardState from
        BodyTabController's getBasicRequest()
    */
    public DashboardState(DataDispatchRequest dataDispatchRequest) {
        super();
        this.setHttpMethod(dataDispatchRequest.getRequestType());
        this.setBody(dataDispatchRequest.getBody());
        this.setContentType(dataDispatchRequest.getContentType());
        this.setStringTuples(dataDispatchRequest.getStringTuples());
        this.setFileTuples(dataDispatchRequest.getFileTuples());
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}

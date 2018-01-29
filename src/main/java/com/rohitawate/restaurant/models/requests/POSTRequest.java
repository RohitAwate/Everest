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

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class POSTRequest extends RestaurantRequest {
    private String requestBody;
    private MediaType requestBodyMediaType;
    private File binaryBody;

    public POSTRequest() {
    }

    public POSTRequest(URL target) {
        super(target);
    }

    public POSTRequest(String target) throws MalformedURLException {
        super(target);
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public MediaType getRequestBodyMediaType() {
        return requestBodyMediaType;
    }

    public void setRequestBodyMediaType(MediaType requestBodyMediaType) {
        this.requestBodyMediaType = requestBodyMediaType;
    }
}

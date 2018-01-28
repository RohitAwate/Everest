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

package com.rohitawate.restaurant.requestsmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rohitawate.restaurant.models.requests.GETRequest;
import com.rohitawate.restaurant.models.responses.RestaurantResponse;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class GETRequestManager extends RequestManager {
    private GETRequest request;

    public GETRequestManager(GETRequest request) {
        this.request = request;
    }

    @Override
    protected RestaurantResponse call() throws Exception {
        RestaurantResponse response = new RestaurantResponse();
        WebTarget target = client.target(request.getTarget().toString());

        long initialTime = System.currentTimeMillis();
        Response serverResponse = target.request().get();
        response.setTime(initialTime, System.currentTimeMillis());

        if (serverResponse == null)
            throw new IOException();
        else if (serverResponse.getStatus() == 301) {
            response.setStatusCode(301);
            String newLocation = serverResponse.getHeaderString("location");
            String responseHelpText;

            if (newLocation == null)
                responseHelpText = "The resource has been permanently moved to another location.\n\n" +
                        "Here's what you can do:\n" +
                        "- Find the new URL from the API documentation.\n" +
                        "- Try using https instead of http if you're not already.";
            else
                responseHelpText = "The resource has been permanently moved to: " + newLocation;

            response.setBody(responseHelpText);
            return response;
        }

        String type = (String) serverResponse.getHeaders().getFirst("Content-type");
        String responseBody = serverResponse.readEntity(String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        switch (type.toLowerCase()) {
            case "application/json; charset=utf-8":
            case "application/json":
                JsonNode node = mapper.readTree(responseBody);
                response.setBody(mapper.writeValueAsString(node));
                break;
            case "application/xml; charset=utf-8":
            case "application/xml":
                response.setBody(mapper.writeValueAsString(responseBody));
                break;
            case "text/html":
            case "text/html; charset=utf-8":
                response.setBody(responseBody);
                break;
        }

        response.setMediaType(serverResponse.getMediaType());
        response.setStatusCode(serverResponse.getStatus());
        response.setSize(responseBody.length());

        return response;
    }
}

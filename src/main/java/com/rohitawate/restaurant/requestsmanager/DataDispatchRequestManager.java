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
import com.rohitawate.restaurant.exceptions.UnreliableResponseException;
import com.rohitawate.restaurant.models.requests.DataDispatchRequest;
import com.rohitawate.restaurant.models.responses.RestaurantResponse;
import com.rohitawate.restaurant.util.Services;
import javafx.concurrent.Task;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes DataDispatchRequest by automatically determining whether it
 * is a POST or a PUT request.
 */
public class DataDispatchRequestManager extends RequestManager {
    @Override
    protected Task<RestaurantResponse> createTask() {
        return new Task<RestaurantResponse>() {
            @Override
            protected RestaurantResponse call() throws Exception {
                DataDispatchRequest dataDispatchRequest = (DataDispatchRequest) request;
                String requestType = dataDispatchRequest.getRequestType();

                RestaurantResponse response = new RestaurantResponse();
                WebTarget target = client.target(dataDispatchRequest.getTarget().toString());
                Map.Entry<String, String> mapEntry;

                Builder requestBuilder = target.request();

                // Add the headers to the request.
                HashMap<String, String> headers = dataDispatchRequest.getHeaders();
                for (Map.Entry entry : headers.entrySet()) {
                    mapEntry = (Map.Entry) entry;
                    requestBuilder.header(mapEntry.getKey(), mapEntry.getValue());
                }

                // Adds the request body based on the content type and generates an invocation.
                Invocation invocation;
                switch (dataDispatchRequest.getContentType()) {
                    case MediaType.MULTIPART_FORM_DATA:
                        FormDataMultiPart formData = new FormDataMultiPart();

                        HashMap<String, String> pairs = dataDispatchRequest.getStringTuples();
                        for (Map.Entry entry : pairs.entrySet()) {
                            mapEntry = (Map.Entry) entry;
                            formData.field(mapEntry.getKey(), mapEntry.getValue());
                        }

                        String filePath;
                        File file;
                        InputStream stream;
                        pairs = dataDispatchRequest.getFileTuples();
                        for (Map.Entry entry : pairs.entrySet()) {
                            mapEntry = (Map.Entry) entry;
                            filePath = mapEntry.getValue();
                            file = new File(filePath);

                            if (file.exists())
                                formData.bodyPart(new FileDataBodyPart(mapEntry.getKey(),
                                        file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
                            else
                                throw new FileNotFoundException();
                        }

                        formData.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

                        if (requestType.equals("POST"))
                            invocation = requestBuilder.buildPost(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
                        else
                            invocation = requestBuilder.buildPut(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
                        break;
                    case MediaType.APPLICATION_OCTET_STREAM:
                        stream = new FileInputStream(dataDispatchRequest.getBody());
                        if (requestType.equals("POST"))
                            invocation = requestBuilder.buildPost(Entity.entity(stream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
                        else
                            invocation = requestBuilder.buildPut(Entity.entity(stream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
                        break;
                    case MediaType.APPLICATION_FORM_URLENCODED:
                        Form form = new Form();

                        for (Map.Entry entry : dataDispatchRequest.getStringTuples().entrySet()) {
                            mapEntry = (Map.Entry) entry;
                            form.param(mapEntry.getKey(), mapEntry.getValue());
                        }

                        if (requestType.equals("POST"))
                            invocation = requestBuilder.buildPost(Entity.form(form));
                        else
                            invocation = requestBuilder.buildPut(Entity.form(form));
                        break;
                    default:
                        // Handles raw data types (JSON, Plain text, XML, HTML)
                        if (requestType.equals("POST"))
                            invocation = requestBuilder
                                    .buildPost(Entity.entity(dataDispatchRequest.getBody(), dataDispatchRequest.getContentType()));
                        else
                            invocation = requestBuilder
                                    .buildPut(Entity.entity(dataDispatchRequest.getBody(), dataDispatchRequest.getContentType()));
                }

                long initialTime = System.currentTimeMillis();
                Response serverResponse = invocation.invoke();
                response.setTime(initialTime, System.currentTimeMillis());

                if (serverResponse == null)
                    throw new UnreliableResponseException("The server did not respond.",
                            "Like that crush from high school..");
                else if (serverResponse.getStatus() == 301) {
                    String newLocation = serverResponse.getHeaderString("location");

                    String responseHelpText;
                    if (newLocation == null)
                        responseHelpText = "The resource has been permanently moved to another location.\n" +
                                "Here's what you can do:\n" +
                                "- Find the new URL from the API documentation.\n" +
                                "- Try using https instead of http if you're not already.";
                    else
                        responseHelpText = "The resource has been permanently moved to: " + newLocation +
                                "\nRESTaurant doesn't automatically redirect your requests.";

                    throw new UnreliableResponseException("301: Resource Moved Permanently", responseHelpText);
                }

                String type = (String) serverResponse.getHeaders().getFirst("Content-type");
                String responseBody = serverResponse.readEntity(String.class);

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

                if (type != null) {
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
                        default:
                            response.setBody(responseBody);
                    }
                } else {
                    response.setBody("No body found in the response.");
                }

                response.setMediaType(serverResponse.getMediaType());
                response.setStatusCode(serverResponse.getStatus());
                response.setSize(responseBody.length());

                return response;
            }
        };
    }
}

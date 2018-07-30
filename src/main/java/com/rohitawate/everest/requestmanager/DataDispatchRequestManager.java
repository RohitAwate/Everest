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

package com.rohitawate.everest.requestmanager;

import com.rohitawate.everest.models.requests.DataDispatchRequest;
import com.rohitawate.everest.models.responses.EverestResponse;
import javafx.concurrent.Task;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes DataDispatchRequest by automatically determining whether it
 * is a POST, PUT or PATCH request.
 */
public class DataDispatchRequestManager extends RequestManager {
    private DataDispatchRequest dataDispatchRequest;
    private String requestType;

    DataDispatchRequestManager() {

    }

    @Override
    protected Task<EverestResponse> createTask() throws ProcessingException {
        return new Task<EverestResponse>() {
            @Override
            protected EverestResponse call() throws Exception {
                dataDispatchRequest = (DataDispatchRequest) request;
                requestType = dataDispatchRequest.getRequestType();

                Invocation invocation = appendBody();
                initialTime = System.currentTimeMillis();
                Response serverResponse = invocation.invoke();
                finalTime = System.currentTimeMillis();

                processServerResponse(serverResponse);

                return response;
            }
        };
    }


    /**
     * Adds the request body based on the content type and generates an invocation.
     *
     * @return invocation object
     */
    private Invocation appendBody() throws Exception {
        /*
            Checks if a custom mime-type is mentioned in the headers.
            If present, it will override the logical Content-Type.
         */
        String overriddenContentType = request.getHeaders().get("Content-Type");
        Invocation invocation = null;
        Map.Entry<String, String> mapEntry;

        switch (dataDispatchRequest.getContentType()) {
            case MediaType.MULTIPART_FORM_DATA:
                FormDataMultiPart formData = new FormDataMultiPart();

                // Adding the string tuples to the request
                HashMap<String, String> pairs = dataDispatchRequest.getStringTuples();
                for (Map.Entry entry : pairs.entrySet()) {
                    mapEntry = (Map.Entry) entry;
                    formData.field(mapEntry.getKey(), mapEntry.getValue());
                }

                String filePath;
                File file;
                boolean fileException = false;
                String fileExceptionMessage = null;
                pairs = dataDispatchRequest.getFileTuples();

                // Adding the file tuples to the request
                for (Map.Entry entry : pairs.entrySet()) {
                    mapEntry = (Map.Entry) entry;
                    filePath = mapEntry.getValue();
                    file = new File(filePath);

                    if (file.exists())
                        formData.bodyPart(new FileDataBodyPart(mapEntry.getKey(),
                                file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
                    else {
                        fileException = true;
                        // For pretty-printing FileNotFoundException to the UI
                        fileExceptionMessage = " - " + filePath + "\n";
                    }
                }

                if (fileException) {
                    throw new FileNotFoundException(fileExceptionMessage);
                }

                formData.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

                if (requestType.equals("POST"))
                    invocation = requestBuilder.buildPost(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
                else
                    invocation = requestBuilder.buildPut(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
                break;
            case MediaType.APPLICATION_OCTET_STREAM:
                if (overriddenContentType == null)
                    overriddenContentType = MediaType.APPLICATION_OCTET_STREAM;
                filePath = dataDispatchRequest.getBody();

                File check = new File(filePath);

                if (!check.exists()) {
                    throw new FileNotFoundException(filePath);
                }

                FileInputStream stream = new FileInputStream(filePath);

                if (requestType.equals("POST"))
                    invocation = requestBuilder.buildPost(Entity.entity(stream, overriddenContentType));
                else
                    invocation = requestBuilder.buildPut(Entity.entity(stream, overriddenContentType));
                break;
            case MediaType.APPLICATION_FORM_URLENCODED:
                if (overriddenContentType == null)
                    overriddenContentType = MediaType.APPLICATION_FORM_URLENCODED;

                Form form = new Form();

                for (Map.Entry entry : dataDispatchRequest.getStringTuples().entrySet()) {
                    mapEntry = (Map.Entry) entry;
                    form.param(mapEntry.getKey(), mapEntry.getValue());
                }

                if (requestType.equals("POST"))
                    invocation = requestBuilder.buildPost(Entity.entity(form, overriddenContentType));
                else
                    invocation = requestBuilder.buildPut(Entity.entity(form, overriddenContentType));
                break;
            default:
                // Handles raw data types (JSON, Plain text, XML, HTML)
                String originalContentType = dataDispatchRequest.getContentType();
                if (overriddenContentType == null)
                    overriddenContentType = originalContentType;
                switch (requestType) {
                    case "POST":
                        invocation = requestBuilder
                                .buildPost(Entity.entity(dataDispatchRequest.getBody(), overriddenContentType));
                        break;
                    case "PUT":
                        invocation = requestBuilder
                                .buildPut(Entity.entity(dataDispatchRequest.getBody(), overriddenContentType));
                        break;
                    case "PATCH":
                        invocation = requestBuilder
                                .build("PATCH", Entity.entity(dataDispatchRequest.getBody(), overriddenContentType));
                        break;
                }
        }

        return invocation;
    }
}

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

import com.rohitawate.everest.exceptions.NullResponseException;
import com.rohitawate.everest.exceptions.RedirectException;
import com.rohitawate.everest.models.requests.*;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.settings.Settings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all the requests made through Everest.
 * Converts EverestRequests into JAX-RS Invocations, which are then processed by Jersey.
 * Also parses the ServerResponse and returns an EverestResponse.
 * <p>
 * Previously, Everest used separate managers for GET, Data (POST, PUT and PATCH) and DELETE requests.
 * However, RequestManager extends JavaFX's Service class, which is expensive to create objects of.
 * This made the creation of separate pools for every kind of RequestManager too expensive, memory-wise.
 * Thus, now a single class manages all kinds of Requests thereby requiring only a single kind of pool.
 * Also, this enables us to re-use inactive RequestManagers for all kinds of requests.
 * For example, previously, if a GETRequestManager was requested by Everest, and all GETRequestManagers were running,
 * a new one would be created even if a DELETERequestManager was idle.
 * <p>
 * TLDR: At the cost of some reduced semantic clarity, the old, separate-for-every-type-of-request RequestManagers
 * are now replaced by this single works-for-all one to save some serious amount of memory and to facilitate better re-use.
 */
public class RequestManager extends Service<EverestResponse> {
    private static final Client client;

    static {
        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();

        // Required for making PATCH requests through Jersey
        client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

        if (Settings.connectionTimeOutEnable)
            client.property(ClientProperties.CONNECT_TIMEOUT, Settings.connectionTimeOut);
        if (Settings.connectionReadTimeOutEnable)
            client.property(ClientProperties.READ_TIMEOUT, Settings.connectionReadTimeOut);
    }

    private long initialTime;
    private long finalTime;

    private EverestRequest request;
    private EverestResponse response;
    private Builder requestBuilder;

    /**
     * Creates a JavaFX Task for processing the required kind of request.
     */
    @Override
    protected Task<EverestResponse> createTask() throws ProcessingException {
        return new Task<EverestResponse>() {
            @Override
            protected EverestResponse call() throws Exception {
                Response serverResponse = null;

                if (request.getClass().equals(GETRequest.class)) {
                    initialTime = System.currentTimeMillis();
                    serverResponse = requestBuilder.get();
                    finalTime = System.currentTimeMillis();
                } else if (request.getClass().equals(DataRequest.class)) {
                    DataRequest dataRequest = (DataRequest) request;

                    Invocation invocation = appendBody(dataRequest);
                    initialTime = System.currentTimeMillis();
                    serverResponse = invocation.invoke();
                    finalTime = System.currentTimeMillis();
                } else if (request.getClass().equals(DELETERequest.class)) {
                    initialTime = System.currentTimeMillis();
                    serverResponse = requestBuilder.delete();
                    finalTime = System.currentTimeMillis();
                }

                processServerResponse(serverResponse);

                return response;
            }
        };
    }

    public void setRequest(EverestRequest request) {
        this.request = request;
        this.requestBuilder = client.target(request.getTarget().toString()).request();
        appendHeaders();
    }

    public EverestRequest getRequest() {
        return this.request;
    }

    private void appendHeaders() {
        request.getHeaders().forEach((key, value) -> requestBuilder.header(key, value));
        requestBuilder.header("User-Agent", "Everest");
    }

    /**
     * Takes a ServerResponse and extracts all the headers, the body, the response time and other details
     * into a EverestResponse.
     */
    private void processServerResponse(Response serverResponse)
            throws NullResponseException, RedirectException {
        if (serverResponse == null) {
            throw new NullResponseException("The server did not respond.",
                    "Like that crush from high school..");
        } else if (serverResponse.getStatus() == 301 || serverResponse.getStatus() == 302) {
            throw new RedirectException(
                    serverResponse.getHeaderString("location"));
        }

        String responseBody = serverResponse.readEntity(String.class);
        response = new EverestResponse();

        response.setHeaders(serverResponse.getStringHeaders());
        response.setTime(initialTime, finalTime);
        response.setBody(responseBody);
        response.setMediaType(serverResponse.getMediaType());
        response.setStatusCode(serverResponse.getStatus());
        response.setSize(responseBody.length());
    }

    public void addHandlers(EventHandler<WorkerStateEvent> running,
                            EventHandler<WorkerStateEvent> succeeded,
                            EventHandler<WorkerStateEvent> failed,
                            EventHandler<WorkerStateEvent> cancelled) {
        setOnRunning(running);
        setOnSucceeded(succeeded);
        setOnFailed(failed);
        setOnCancelled(cancelled);
    }

    public void removeHandlers() {
        removeEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, getOnRunning());
        removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, getOnSucceeded());
        removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, getOnFailed());
        removeEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, getOnCancelled());
    }

    /**
     * Adds the request body based on the content type and generates an invocation.
     * Used for DataRequests.
     *
     * @return invocation object
     */
    private Invocation appendBody(DataRequest dataRequest) throws Exception {
        /*
            Checks if a custom mime-type is mentioned in the headers.
            If present, it will override the auto-determined one.
         */
        String overriddenContentType = request.getHeaders().get("Content-Type");
        Invocation invocation = null;
        Map.Entry<String, String> mapEntry;
        String requestType = dataRequest.getRequestType();

        switch (dataRequest.getContentType()) {
            case MediaType.MULTIPART_FORM_DATA:
                FormDataMultiPart formData = new FormDataMultiPart();

                // Adding the string tuples to the request
                HashMap<String, String> pairs = dataRequest.getStringTuples();
                for (Map.Entry<String, String> entry : pairs.entrySet()) {
                    mapEntry = entry;
                    formData.field(mapEntry.getKey(), mapEntry.getValue());
                }

                String filePath;
                File file;
                boolean fileException = false;
                String fileExceptionMessage = null;
                pairs = dataRequest.getFileTuples();

                // Adding the file tuples to the request
                for (Map.Entry<String, String> entry : pairs.entrySet()) {
                    mapEntry = entry;
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

                invocation = getInvocation(MediaType.MULTIPART_FORM_DATA, requestType, formData, requestBuilder);
                break;
            case MediaType.APPLICATION_OCTET_STREAM:
                if (overriddenContentType == null)
                    overriddenContentType = MediaType.APPLICATION_OCTET_STREAM;
                filePath = dataRequest.getBody();

                if (filePath.equals("")) {
                    throw new FileNotFoundException("No file selected");
                }

                File check = new File(filePath);

                if (!check.exists()) {
                    throw new FileNotFoundException(filePath);
                }

                FileInputStream stream = new FileInputStream(filePath);

                invocation = getInvocation(overriddenContentType, requestType, stream, requestBuilder);
                break;
            case MediaType.APPLICATION_FORM_URLENCODED:
                if (overriddenContentType == null)
                    overriddenContentType = MediaType.APPLICATION_FORM_URLENCODED;

                Form form = new Form();

                for (Map.Entry<String, String> entry : dataRequest.getStringTuples().entrySet()) {
                    mapEntry = entry;
                    form.param(mapEntry.getKey(), mapEntry.getValue());
                }

                invocation = getInvocation(overriddenContentType, requestType, form, requestBuilder);
                break;
            default:
                // Handles raw data types (JSON, Plain text, XML, HTML)
                String originalContentType = dataRequest.getContentType();
                if (overriddenContentType == null)
                    overriddenContentType = originalContentType;
                switch (requestType) {
                    case HTTPConstants.POST:
                        invocation = requestBuilder
                                .buildPost(Entity.entity(dataRequest.getBody(), overriddenContentType));
                        break;
                    case HTTPConstants.PUT:
                        invocation = requestBuilder
                                .buildPut(Entity.entity(dataRequest.getBody(), overriddenContentType));
                        break;
                    case HTTPConstants.PATCH:
                        invocation = requestBuilder
                                .build(HTTPConstants.PATCH, Entity.entity(dataRequest.getBody(), overriddenContentType));
                        break;
                }
        }

        return invocation;
    }

    private static Invocation getInvocation(String overriddenContentType, String requestType, Object entity, Builder requestBuilder) {
        Invocation invocation;
        switch (requestType) {
            case HTTPConstants.POST:
                invocation = requestBuilder.buildPost(Entity.entity(entity, overriddenContentType));
                break;
            case HTTPConstants.PUT:
                invocation = requestBuilder.buildPut(Entity.entity(entity, overriddenContentType));
                break;
            default:
                invocation = requestBuilder.build(HTTPConstants.PATCH, Entity.entity(entity, overriddenContentType));
                break;
        }
        return invocation;
    }
}

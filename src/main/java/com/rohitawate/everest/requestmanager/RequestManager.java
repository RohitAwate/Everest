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

import com.rohitawate.everest.exceptions.RedirectException;
import com.rohitawate.everest.exceptions.UnreliableResponseException;
import com.rohitawate.everest.models.requests.EverestRequest;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.settings.Settings;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

public abstract class RequestManager extends Service<EverestResponse> {
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

    long initialTime;
    long finalTime;

    EverestRequest request;
    EverestResponse response;
    Builder requestBuilder;

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

    void processServerResponse(Response serverResponse)
            throws UnreliableResponseException, RedirectException {
        if (serverResponse == null) {
            throw new UnreliableResponseException("The server did not respond.",
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
}

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
import com.rohitawate.everest.util.settings.Settings;
import javafx.concurrent.Service;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public abstract class RequestManager extends Service<EverestResponse> {
    private final Client client;
    long initialTime;
    long finalTime;

    EverestRequest request;
    EverestResponse response;
    Builder requestBuilder;

    RequestManager() {
        this.client = initClient();
    }

    private Client initClient() {
        Client client;
        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();

        client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        if (Settings.connectionTimeOutEnable)
            client.property(ClientProperties.CONNECT_TIMEOUT, Settings.connectionTimeOut);
        if (Settings.connectionReadTimeOutEnable)
            client.property(ClientProperties.READ_TIMEOUT, Settings.connectionReadTimeOut);
        return client;
    }

    public void setRequest(EverestRequest request) {
        this.request = request;
        this.requestBuilder = client.target(request.getTarget().toString()).request();
        appendHeaders();
    }

    private void appendHeaders() {
        HashMap<String, String> headers = request.getHeaders();
        Map.Entry<String, String> mapEntry;

        for (Map.Entry entry : headers.entrySet()) {
            mapEntry = (Map.Entry) entry;
            requestBuilder.header(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    void processServerResponse(Response serverResponse)
            throws UnreliableResponseException, RedirectException {
        if (serverResponse == null)
            throw new UnreliableResponseException("The server did not respond.",
                    "Like that crush from high school..");
        else if (serverResponse.getStatus() == 301) {
            String newLocation = serverResponse.getHeaderString("location");
            throw new RedirectException(newLocation);
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
}

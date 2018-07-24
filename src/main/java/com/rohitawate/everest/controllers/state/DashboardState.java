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

package com.rohitawate.everest.controllers.state;

import com.rohitawate.everest.controllers.DashboardController.ResponseLayer;
import com.rohitawate.everest.exceptions.RedirectException;
import com.rohitawate.everest.exceptions.UnreliableResponseException;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.models.requests.EverestRequest;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.requestmanager.DataDispatchRequestManager;
import com.rohitawate.everest.requestmanager.RequestManager;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;

import javax.ws.rs.ProcessingException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.HashMap;

public class DashboardState {
    public ComposerState composer;
    public ResponseLayer visibleLayer;

    // ResponseLayer parameters
    public int responseCode;
    public String responseType;
    public String responseBody;
    public int responseTime;
    public int responseSize;
    public HashMap<String, String> responseHeaders;

    // ErrorLayer parameters
    public String errorTitle;
    public String errorDetails;

    // ResponseLayer parameters
    private RequestManager requestManager;
    public void setRequestManager(RequestManager manager) {
        this.requestManager = manager;
        requestManager.removeEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, requestManager.getOnRunning());
        requestManager.removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, requestManager.getOnSucceeded());
        requestManager.removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, requestManager.getOnFailed());

        requestManager.setOnFailed(this::onRequestFailed);
        requestManager.setOnSucceeded(this::onRequestSucceeded);
    }

    private void onRequestSucceeded(Event e) {
        this.visibleLayer = ResponseLayer.RESPONSE;
        EverestResponse response = requestManager.getValue();
        responseCode = response.getStatusCode();
        responseType = response.getMediaType().toString();
        responseTime = (int) response.getTime();
        responseSize = response.getSize();
        responseBody = response.getBody();

        if (responseHeaders == null)
            responseHeaders = new HashMap<>();
        else
            responseHeaders.clear();

        response.getHeaders().forEach((key, value) -> responseHeaders.put(key, value.get(0)));
    }

    private void onRequestFailed(Event e) {
        this.visibleLayer = ResponseLayer.ERROR;
        Throwable throwable = requestManager.getException();
        Exception exception = (Exception) throwable;
        Services.loggingService.logWarning(this.composer.httpMethod + " request could not be processed.", exception, LocalDateTime.now());

        if (throwable.getClass() == UnreliableResponseException.class) {
            UnreliableResponseException URE = (UnreliableResponseException) throwable;
            errorTitle = URE.getExceptionTitle();
            errorDetails = URE.getExceptionDetails();
        } else if (throwable.getClass() == ProcessingException.class) {
            errorTitle = "Everest couldn't connect.";
            errorDetails = "Either you are not connected to the Internet or the server is offline.";
        } else if (throwable.getClass() == RedirectException.class) {
            RedirectException redirect = (RedirectException) throwable;
            this.composer.target = redirect.getNewLocation();
            EverestRequest request = requestManager.getRequest();
            try {
                request.setTarget(redirect.getNewLocation());
                requestManager.restart();
            } catch (MalformedURLException MURLE) {
                Services.loggingService.logInfo("Invalid URL: " + this.composer.target, LocalDateTime.now());
            }

            return;
        }

        if (requestManager.getClass() == DataDispatchRequestManager.class) {
            if (throwable.getCause() != null && throwable.getCause().getClass() == IllegalArgumentException.class) {
                errorTitle = "Did you forget something?";
                errorDetails = "Please specify a body for your " + this.composer.httpMethod + " request.";
            } else if (throwable.getClass() == FileNotFoundException.class) {
                errorTitle = "File(s) not found:";
                errorDetails = throwable.getMessage();
            }
        }

        requestManager.reset();
    }

    public DashboardState() {
    }

    public DashboardState(ComposerState composer) {
        this.composer = composer;
    }
}

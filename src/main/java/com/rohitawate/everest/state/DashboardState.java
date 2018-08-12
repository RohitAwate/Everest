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

package com.rohitawate.everest.state;

import com.rohitawate.everest.controllers.DashboardController.ComposerTab;
import com.rohitawate.everest.controllers.DashboardController.ResponseLayer;
import com.rohitawate.everest.controllers.DashboardController.ResponseTab;
import com.rohitawate.everest.exceptions.NullResponseException;
import com.rohitawate.everest.exceptions.RedirectException;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.models.requests.DataRequest;
import com.rohitawate.everest.models.requests.EverestRequest;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.requestmanager.RequestManager;
import javafx.event.Event;

import javax.ws.rs.ProcessingException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Represents the state of Everest's Dashboard.
 */
public class DashboardState {
    public ComposerState composer;
    public ResponseLayer visibleResponseLayer;
    public ResponseTab visibleResponseTab;
    public ComposerTab visibleComposerTab;

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

    /**
     * Accepts a RequestManager from the DashboardController
     * which is in the RUNNING state and switches its handlers.
     * <p>
     * The new handlers make changes to the DashboardState object
     * rather than the Dashboard.
     * <p>
     * If we switch back to the tab with DashboardState while
     * the manager is running, it is handed back over to the Dashboard.
     */
    public void handOverRequest(RequestManager requestManager) {
        this.requestManager = requestManager;
        this.requestManager.removeHandlers();

        this.requestManager.setOnFailed(this::onRequestFailed);
        this.requestManager.setOnSucceeded(this::onRequestSucceeded);
        this.requestManager.setOnCancelled(this::onRequestCancelled);
    }

    private void onRequestCancelled(Event event) {
        this.visibleResponseLayer = ResponseLayer.PROMPT;
        requestManager.reset();
    }

    private void onRequestSucceeded(Event event) {
        visibleResponseLayer = ResponseLayer.RESPONSE;
        EverestResponse response = requestManager.getValue();
        responseCode = response.getStatusCode();
        if (response.getMediaType() != null)
            responseType = response.getMediaType().toString();
        else
            responseType = "";
        responseTime = (int) response.getTime();
        responseSize = response.getSize();
        responseBody = response.getBody();

        if (responseHeaders == null)
            responseHeaders = new HashMap<>();
        else
            responseHeaders.clear();

        response.getHeaders().forEach((key, value) -> responseHeaders.put(key, value.get(0)));
    }

    // TODO: Clean this method
    private void onRequestFailed(Event event) {
        this.visibleResponseLayer = ResponseLayer.ERROR;
        Throwable throwable = requestManager.getException();
        Exception exception = (Exception) throwable;
        LoggingService.logWarning(this.composer.httpMethod + " request could not be processed.", exception, LocalDateTime.now());

        if (throwable.getClass() == NullResponseException.class) {
            NullResponseException URE = (NullResponseException) throwable;
            errorTitle = URE.getExceptionTitle();
            errorDetails = URE.getExceptionDetails();
        } else if (throwable.getClass() == ProcessingException.class) {
            System.out.println(throwable.getCause().toString());
            errorTitle = "Everest couldn't connect.";
            errorDetails = "Either you are not connected to the Internet or the server is offline.";
        } else if (throwable.getClass() == RedirectException.class) {
            RedirectException redirect = (RedirectException) throwable;
            this.composer.target = redirect.getNewLocation();
            EverestRequest request = requestManager.getRequest();

            try {
                request.setTarget(redirect.getNewLocation());
                requestManager.restart();
                return;
            } catch (MalformedURLException MURLE) {
                LoggingService.logInfo("Invalid URL: " + this.composer.target, LocalDateTime.now());
            }
        } else {
            errorTitle = "Oops... That's embarrassing!";
            errorDetails = "Something went wrong. Try to make another request.Restart Everest if that doesn't work.";
        }

        if (requestManager.getRequest().getClass().equals(DataRequest.class)) {
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

    public RequestManager getRequestManager() {
        return this.requestManager;
    }

    public DashboardState() {
    }

    public DashboardState(ComposerState composer) {
        this.composer = composer;
    }
}

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

package com.rohitawate.everest.controllers;

import com.rohitawate.everest.controllers.search.Searchable;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.state.FieldState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class HistoryItemController implements Initializable, Searchable<ComposerState> {
    @FXML
    private Label methodLabel, address;
    @FXML
    private Tooltip tooltip;

    private static final String GETStyle = "-fx-text-fill: orangered";
    private static final String POSTStyle = "-fx-text-fill: cornflowerblue";
    private static final String PUTStyle = "-fx-text-fill: deeppink";
    private static final String PATCHStyle = "-fx-text-fill: teal";
    private static final String DELETEStyle = "-fx-text-fill: limegreen";

    private ComposerState state;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tooltip.textProperty().bind(address.textProperty());
    }

    public ComposerState getState() {
        return state;
    }

    public void setState(ComposerState state) {
        this.state = state;
        this.methodLabel.setText(state.httpMethod);
        switch (state.httpMethod) {
            case HTTPConstants.GET:
                methodLabel.setStyle(GETStyle);
                break;
            case HTTPConstants.POST:
                methodLabel.setStyle(POSTStyle);
                break;
            case HTTPConstants.PUT:
                methodLabel.setStyle(PUTStyle);
                break;
            case HTTPConstants.PATCH:
                methodLabel.setStyle(PATCHStyle);
                break;
            case HTTPConstants.DELETE:
                methodLabel.setStyle(DELETEStyle);
                break;
        }
        this.address.setText(state.target);
    }

    public int getRelativityIndex(String searchString) {
        int index = 0;
        searchString = searchString.toLowerCase();
        String comparisonString;

        // Checks if matches with target
        comparisonString = state.target.toLowerCase();
        if (comparisonString.contains(searchString))
            index += 10;

        try {
            URL url = new URL(state.target);

            // Checks if matches with target's hostname
            comparisonString = url.getHost().toLowerCase();
            if (comparisonString.contains(searchString))
                index += 10;

            // Checks if matches with target's path
            comparisonString = url.getPath().toLowerCase();
            if (comparisonString.contains(searchString))
                index += 9;
        } catch (MalformedURLException e) {
            LoggingService.logInfo("Failed to parse URL while calculating relativity index.", LocalDateTime.now());
        }

        // Checks if matches with HTTP method
        comparisonString = state.httpMethod.toLowerCase();
        if (comparisonString.contains(searchString))
            index += 7;

        // Checks for a match in the params
        for (FieldState state : state.params) {
            if (state.key.toLowerCase().contains(searchString) ||
                    state.value.toLowerCase().contains(searchString))
                index += 5;
        }

        // Checks for a match in the headers
        for (FieldState state : state.headers) {
            if (state.key.toLowerCase().contains(searchString) ||
                    state.value.toLowerCase().contains(searchString))
                index += 6;
        }

        if (!(state.httpMethod.equals(HTTPConstants.GET) || state.httpMethod.equals(HTTPConstants.DELETE))) {
            switch (state.contentType) {
                case MediaType.TEXT_PLAIN:
                case MediaType.APPLICATION_JSON:
                case MediaType.APPLICATION_XML:
                case MediaType.TEXT_HTML:
                case MediaType.APPLICATION_OCTET_STREAM:
                    // Checks for match in rawBody of the request
                    comparisonString = state.rawBody.toLowerCase();
                    if (comparisonString.contains(searchString))
                        index += 8;
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    // Checks for match in string tuples
                    for (FieldState state : state.urlStringTuples) {
                        if (state.key.toLowerCase().contains(searchString) ||
                                state.value.toLowerCase().contains(searchString))
                            index += 8;
                    }
                    break;
                case MediaType.MULTIPART_FORM_DATA:
                    // Checks for match in string and file tuples
                    for (FieldState state : state.formStringTuples) {
                        if (state.key.toLowerCase().contains(searchString) ||
                                state.value.toLowerCase().contains(searchString))
                            index += 8;
                    }

                    for (FieldState state : state.formFileTuples) {
                        if (state.key.toLowerCase().contains(searchString) ||
                                state.value.toLowerCase().contains(searchString))
                            index += 8;
                    }
                    break;
            }
        }
        return index;
    }
}

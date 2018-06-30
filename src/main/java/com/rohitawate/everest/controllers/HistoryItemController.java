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

import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.controllers.state.FieldState;
import com.rohitawate.everest.misc.Services;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class HistoryItemController implements Initializable {
    @FXML
    private Label requestType, address;
    @FXML
    private Tooltip tooltip;

    private DashboardState state;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tooltip.textProperty().bind(address.textProperty());
    }

    public DashboardState getState() {
        return state;
    }

    public void setState(DashboardState state) {
        this.state = state;
        this.requestType.setText(state.httpMethod);
        this.address.setText(state.target);
    }

    public int getRelativityIndex(String searchString) {
        searchString = searchString.toLowerCase();
        String comparisonString;

        // Checks if matches with target
        comparisonString = state.target.toLowerCase();
        if (comparisonString.contains(searchString))
            return 10;

        try {
            URL url = new URL(state.target);

            // Checks if matches with target's hostname
            comparisonString = url.getHost().toLowerCase();
            if (comparisonString.contains(searchString))
                return 10;

            // Checks if matches with target's path
            comparisonString = url.getPath().toLowerCase();
            if (comparisonString.contains(searchString))
                return 9;
        } catch (MalformedURLException e) {
            Services.loggingService.logInfo("Failed to parse URL while calculating relativity index.", LocalDateTime.now());
        }

        // Checks if matches with HTTP method
        comparisonString = state.httpMethod.toLowerCase();
        if (comparisonString.contains(searchString))
            return 7;

        // Checks for a match in the params
        for (FieldState state : state.params) {
            if (state.key.toLowerCase().contains(searchString) ||
                    state.value.toLowerCase().contains(searchString))
                return 5;
        }

        // Checks for a match in the headers
        for (FieldState state : state.headers) {
            if (state.key.toLowerCase().contains(searchString) ||
                    state.value.toLowerCase().contains(searchString))
                return 6;
        }

        if (state.httpMethod.equals("POST") || state.httpMethod.equals("PUT")) {
            switch (state.contentType) {
                case MediaType.TEXT_PLAIN:
                case MediaType.APPLICATION_JSON:
                case MediaType.APPLICATION_XML:
                case MediaType.TEXT_HTML:
                case MediaType.APPLICATION_OCTET_STREAM:
                    // Checks for match in rawBody of the request
                    comparisonString = state.rawBody.toLowerCase();
                    if (comparisonString.contains(searchString))
                        return 8;
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    // Checks for match in string tuples
                    for (FieldState state : state.urlStringTuples) {
                        if (state.key.toLowerCase().contains(searchString) ||
                                state.value.toLowerCase().contains(searchString))
                            return 8;
                    }
                    break;
                case MediaType.MULTIPART_FORM_DATA:
                    // Checks for match in string and file tuples
                    for (FieldState state : state.formStringTuples) {
                        if (state.key.toLowerCase().contains(searchString) ||
                                state.value.toLowerCase().contains(searchString))
                            return 8;
                    }

                    for (FieldState state : state.formFileTuples) {
                        if (state.key.toLowerCase().contains(searchString) ||
                                state.value.toLowerCase().contains(searchString))
                            return 8;
                    }
                    break;
            }
        }
        return 0;
    }
}

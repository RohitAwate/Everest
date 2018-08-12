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

import com.rohitawate.everest.models.responses.EverestResponse;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;

public class ResponseHeadersViewer extends ScrollPane {
    private VBox container;
    private HashMap<String, String> map;

    private static final String responseHeaderLabel = "response-header-label";
    private static final String keyLabelStyleClass = "response-header-key-label";
    private static final String valueLabelStyleClass = "response-header-value-label";

    ResponseHeadersViewer() {
        this.container = new VBox();
        container.setPadding(new Insets(10, 20, 10, 20));
        this.setContent(container);

        this.setFitToHeight(true);
        this.setFitToWidth(true);

        map = new HashMap<>();
    }

    void populate(HashMap<String, String> headers) {
        map.clear();
        headers.forEach((key, value) -> map.put(key, value));
        populate();
    }

    void populate(EverestResponse response) {
        map.clear();
        response.getHeaders().forEach((key, value) -> map.put(key, value.get(0)));
        populate();
    }

    private void populate() {
        container.getChildren().clear();

        map.forEach((key, value) -> {
            Label keyLabel = new Label(key + ": ");
            keyLabel.getStyleClass().addAll(keyLabelStyleClass, responseHeaderLabel);

            Label valueLabel = new Label(value);
            valueLabel.getStyleClass().addAll(valueLabelStyleClass, responseHeaderLabel);

            container.getChildren().add(new HBox(keyLabel, valueLabel));
        });

    }

    public HashMap<String, String> getHeaders() {
        return new HashMap<>(map);
    }
}

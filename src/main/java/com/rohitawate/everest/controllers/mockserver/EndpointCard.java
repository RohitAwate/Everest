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

package com.rohitawate.everest.controllers.mockserver;

import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.server.mock.Endpoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

class EndpointCard extends HBox {
    Label method;
    Label path;
    Endpoint endpoint;
    private ImageView alertIcon;
    private HBox alertBox;

    EndpointCard(Endpoint endpoint) {
        this.endpoint = endpoint;

        method = new Label(endpoint.method);
        applyStyle(method);
        method.getStyleClass().add("endpoint-card-method");

        path = new Label(endpoint.path);
        path.getStyleClass().add("endpoint-card-path");

        alertIcon = new ImageView(getClass().getResource("/assets/Alert.png").toString());
        alertIcon.setFitWidth(15);
        alertIcon.setFitHeight(15);
        Tooltip.install(alertIcon, new Tooltip("Duplicate endpoint"));
        alertIcon.setVisible(false);

        alertBox = new HBox(alertIcon);
        alertBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(alertBox, Priority.ALWAYS);

        getStyleClass().add("endpoint-card");
        getChildren().addAll(method, path, alertBox);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(2, 10, 2, 10));
    }

    void showAlert() {
        alertIcon.setVisible(true);
    }

    void hideAlert() {
        alertIcon.setVisible(false);
    }

    private static final String GETStyle = "-fx-background-color: orangered";
    private static final String POSTStyle = "-fx-background-color: cornflowerblue";
    private static final String PUTStyle = "-fx-background-color: deeppink";
    private static final String PATCHStyle = "-fx-background-color: teal";
    private static final String DELETEStyle = "-fx-background-color: limegreen";

    static void applyStyle(Label label) {
        switch (label.getText()) {
            case HTTPConstants.GET:
                label.setStyle(GETStyle);
                break;
            case HTTPConstants.POST:
                label.setStyle(POSTStyle);
                break;
            case HTTPConstants.PUT:
                label.setStyle(PUTStyle);
                break;
            case HTTPConstants.PATCH:
                label.setStyle(PATCHStyle);
                break;
            case HTTPConstants.DELETE:
                label.setStyle(DELETEStyle);
                break;
        }
    }
}

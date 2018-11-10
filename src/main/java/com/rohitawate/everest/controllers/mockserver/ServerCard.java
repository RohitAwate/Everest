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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.server.mock.MockServer;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.rohitawate.everest.controllers.mockserver.MockServerDashboardController.pushServerNotification;

class ServerCard extends HBox {
    private Label name;
    private final JFXToggleButton toggle;
    private final JFXButton optionsButton;
    MockServer server;

    ServerCard(MockServer server) {
        this.server = server;

        name = new Label(server.name);

        toggle = new JFXToggleButton();
        toggle.setGraphicTextGap(20);
        toggle.setToggleColor(Paint.valueOf("#ff4500"));
        toggle.setToggleLineColor(Paint.valueOf("#cacaca"));
        toggle.setContentDisplay(ContentDisplay.RIGHT);
        toggle.setOnAction(this::toggleService);

        optionsButton = new JFXButton();
        ImageView optionsImage = new ImageView(getClass().getResource("/assets/Settings.png").toString());
        optionsImage.setFitWidth(20);
        optionsImage.setFitHeight(20);
        optionsButton.setGraphic(optionsImage);

        HBox filler = new HBox(toggle, optionsButton);
        HBox.setHgrow(filler, Priority.ALWAYS);
        filler.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(name, filler);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("server-card");
        setSpacing(20);
        setPadding(new Insets(0, 10, 0, 10));
    }

    void setOptionsStage(Stage optionsStage, ServerDetailsController controller, MockServerDashboardController dashboardController) {
        this.optionsButton.setOnAction(e -> {
            controller.setServer(server);
            controller.setMode(ServerDetailsController.UPDATE_MODE);
            optionsStage.showAndWait();
            dashboardController.setFinalURLField();
        });
    }

    private void toggleService(ActionEvent actionEvent) {
        if (toggle.isSelected()) {
            try {
                server.start();
                String msg = String.format("Mock server '%s' has started.", server.name);
                LoggingService.logInfo(msg, LocalDateTime.now());
                pushServerNotification(msg, 7000);
            } catch (IOException e) {
                String error = String.format("Could not start mock server '%s'.", server.name);
                LoggingService.logSevere(error, e, LocalDateTime.now());
                pushServerNotification(error, 7000);
            }
        } else {
            try {
                server.stop();
                String msg = String.format("Mock server '%s' has stopped.", server.name);
                LoggingService.logInfo(msg, LocalDateTime.now());
                pushServerNotification(msg, 7000);
            } catch (IOException e) {
                String error = String.format("Could not stop mock server '%s'.", server.name);
                LoggingService.logSevere(error, e, LocalDateTime.now());
                pushServerNotification(error, 7000);
            }
        }
    }
}

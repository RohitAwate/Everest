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
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.server.mock.MockServer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static com.rohitawate.everest.controllers.mockserver.MockServerDashboardController.pushServerNotification;

class ServerCard extends HBox {
    private Label name;
    private final JFXToggleButton toggle;
    private final JFXButton optionsButton;
    MockServer server;

    final JFXButton deleteButton;
    final JFXButton cloneButton;

    private final HBox filler;
    private final HBox optionsBox;

    private FadeTransition fadeTransition;

    ServerCard(MockServer server) {
        this.server = server;

        deleteButton = new JFXButton();
        ImageView deleteImage = new ImageView(getClass().getResource("/assets/Delete.png").toString());
        deleteImage.setFitWidth(20);
        deleteImage.setFitHeight(20);
        deleteButton.setGraphic(deleteImage);

        cloneButton = new JFXButton();
        ImageView duplicateImage = new ImageView(getClass().getResource("/assets/Copy.png").toString());
        duplicateImage.setFitWidth(20);
        duplicateImage.setFitHeight(20);
        cloneButton.setGraphic(duplicateImage);

        name = new Label(server.name);
        name.setTextOverrun(OverrunStyle.ELLIPSIS);
        name.maxWidthProperty().bind(widthProperty().multiply(0.28));

        toggle = new JFXToggleButton();
        toggle.setToggleColor(Paint.valueOf("#ff4500"));
        toggle.setToggleLineColor(Paint.valueOf("#cacaca"));
        toggle.setContentDisplay(ContentDisplay.RIGHT);
        toggle.setOnAction(this::toggleService);
        toggle.setPadding(new Insets(0));

        optionsButton = new JFXButton();
        ImageView optionsImage = new ImageView(getClass().getResource("/assets/Settings.png").toString());
        optionsImage.setFitWidth(20);
        optionsImage.setFitHeight(20);
        optionsButton.setGraphic(optionsImage);

        optionsBox = new HBox(optionsButton, cloneButton, deleteButton);
        fadeTransition = new FadeTransition(Duration.millis(400), optionsBox);
        optionsBox.setAlignment(Pos.CENTER);

        filler = new HBox(toggle);
        filler.setSpacing(5);
        HBox.setHgrow(filler, Priority.ALWAYS);
        filler.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(name, filler);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("server-card");
        setPadding(new Insets(0, 10, 0, 10));

        setOnMouseEntered(this::hoverEntered);
        setOnMouseExited(this::hoverExited);
    }

    private void hoverEntered(MouseEvent event) {
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_IN);
        fadeTransition.play();

        filler.getChildren().add(0, optionsBox);
    }

    private void hoverExited(MouseEvent event) {
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();

        filler.getChildren().remove(optionsBox);
    }

    void setOptionsStage(Stage optionsStage, ServerDetailsController controller,
                         MockServerDashboardController dashboardController,
                         EventHandler<ActionEvent> onDelete,
                         EventHandler<ActionEvent> onClone) {
        this.optionsButton.setOnAction(e -> {
            controller.setServer(server);
            controller.setMode(ServerDetailsController.UPDATE_MODE);
            optionsStage.showAndWait();
            dashboardController.setFinalURLField();
            name.setText(server.name);
        });

        this.deleteButton.setOnAction(onDelete);
        this.cloneButton.setOnAction(onClone);
    }

    private void toggleService(ActionEvent actionEvent) {
        if (toggle.isSelected()) {
            try {
                server.start();
                String msg = String.format("Mock server '%s' has started.", server.name);
                Logger.info(msg);
                pushServerNotification(msg, 7000);
            } catch (IOException e) {
                String error = String.format("Could not start mock server '%s'.", server.name);
                Logger.severe(error, e);
                pushServerNotification(error, 7000);
            }
        } else {
            try {
                server.stop();
                String msg = String.format("Mock server '%s' has stopped.", server.name);
                Logger.info(msg);
                pushServerNotification(msg, 7000);
            } catch (IOException e) {
                String error = String.format("Could not stop mock server '%s'.", server.name);
                Logger.severe(error, e);
                pushServerNotification(error, 7000);
            }
        }
    }
}

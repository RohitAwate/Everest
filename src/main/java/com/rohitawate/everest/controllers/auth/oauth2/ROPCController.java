/*
 * Copyright 2019 Rohit Awate.
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

package com.rohitawate.everest.controllers.auth.oauth2;

import com.jfoenix.controls.*;
import com.rohitawate.everest.Main;
import com.rohitawate.everest.state.auth.ROPCState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ROPCController implements Initializable {
    @FXML
    private VBox ropcBox, accessTokenBox;
    @FXML
    private JFXCheckBox enabled;
    @FXML
    private JFXTextField tokenURLField, usernameField, clientIDField,
            clientSecretField, redirectURLField, scopeField,
            headerPrefixField, accessTokenField, refreshTokenField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXButton refreshTokenButton;
    @FXML
    private Label expiryLabel;

    private JFXRippler rippler;

    private ROPCState state;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshTokenButton.setOnAction(this::refreshToken);
        expiryLabel.setVisible(false);

        rippler = new JFXRippler(accessTokenBox);
        rippler.setPrefSize(ropcBox.getPrefWidth(), ropcBox.getPrefHeight());
        ropcBox.getChildren().add(rippler);

        Platform.runLater(() -> {
            if (Main.preferences.auth.enableAccessTokenExpiryTimer) {
                Timeline timeline = new Timeline();
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(1),
                                new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        setExpiryLabel();
                                    }
                                })
                );

                timeline.play();
            } else {
                expiryLabel.setOnMouseClicked(e -> setExpiryLabel());
                expiryLabel.setTooltip(new Tooltip("Click to update expiry status"));
                expiryLabel.setCursor(Cursor.HAND);
            }
        });
    }

    private void refreshToken(ActionEvent actionEvent) {

    }

    private void setExpiryLabel() {
        if (state != null && state.accessToken.getTimeToExpiry() >= 0) {
            expiryLabel.setVisible(true);

            if (state.accessToken.getExpiresIn() == 0) {
                expiryLabel.setText("Never expires.");
            } else {
                long timeToExpiry = state.accessToken.getTimeToExpiry();
                if (timeToExpiry < 0) {
                    expiryLabel.setText("Token expired.");
                } else {
                    int hours, minutes, seconds;
                    hours = (int) (timeToExpiry / 3600);
                    timeToExpiry %= 3600;
                    minutes = (int) timeToExpiry / 60;
                    seconds = (int) timeToExpiry % 60;

                    Platform.runLater(() -> {
                        if (hours == 0 && minutes != 0) {
                            expiryLabel.setText(String.format("Expires in %dm %ds", minutes, seconds));
                        } else if (hours == 0 && minutes == 0) {
                            expiryLabel.setText(String.format("Expires in %ds", seconds));
                        } else {
                            expiryLabel.setText(String.format("Expires in %dh %dm %ds", hours, minutes, seconds));
                        }
                    });
                }
            }
        }
    }

    public ROPCState getState() {
        return this.state;
    }

    public void setState(ROPCState state) {

    }
}

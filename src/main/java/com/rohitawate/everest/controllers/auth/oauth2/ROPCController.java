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
import com.rohitawate.everest.auth.oauth2.ROPCProvider;
import com.rohitawate.everest.auth.oauth2.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.tokens.ROPCToken;
import com.rohitawate.everest.controllers.DashboardController;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.notifications.NotificationsManager;
import com.rohitawate.everest.state.auth.ROPCState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class ROPCController implements Initializable {
    @FXML
    private VBox ropcBox, accessTokenBox;
    @FXML
    private JFXCheckBox enabled;
    @FXML
    private JFXTextField tokenURLField, usernameField, clientIDField,
            clientSecretField, scopeField, headerPrefixField,
            accessTokenField, refreshTokenField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXButton refreshTokenButton;
    @FXML
    private Label expiryLabel;

    private JFXRippler rippler;

    private ROPCState state;
    private ROPCProvider provider;

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

    @FXML
    private void refreshToken(ActionEvent actionEvent) {
        if (provider == null) {
            provider = new ROPCProvider(this);
        }

        ExecutorService service = EverestUtilities.newDaemonSingleThreadExecutor();
        service.submit(new TokenFetcher());
    }

    public ROPCProvider getAuthProvider() {
        if (provider == null) {
            provider = new ROPCProvider(this);
        }

        return provider;
    }

    private void setExpiryLabel() {
        if (state != null && state.accessToken != null && state.accessToken.getTimeToExpiry() >= 0) {
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
        if (state == null) {
            state = new ROPCState();
        }

        state.enabled = enabled.isSelected();
        state.accessTokenURL = tokenURLField.getText();

        state.clientID = clientIDField.getText();
        state.clientSecret = clientSecretField.getText();
        state.username = usernameField.getText();
        state.password = passwordField.getText();

        state.headerPrefix = headerPrefixField.getText();
        state.scope = scopeField.getText();

        // Setting these values again since they can be modified from the UI
        if (state.accessToken != null) {
            String accessToken = accessTokenField.getText();
            String refreshToken = refreshTokenField.getText();

            if (accessToken.isBlank() && refreshToken.isBlank()) {
                state.accessToken = null;
            } else {
                state.accessToken.setAccessToken(accessToken);
                state.accessToken.setRefreshToken(refreshToken);
            }
        }

        return this.state;
    }

    public void setState(ROPCState state) {
        if (state == null) {
            return;
        }

        this.state = state;

        enabled.setSelected(state.enabled);
        tokenURLField.setText(state.accessTokenURL);

        clientIDField.setText(state.clientID);
        clientSecretField.setText(state.clientSecret);
        usernameField.setText(state.username);
        passwordField.setText(state.password);

        headerPrefixField.setText(state.headerPrefix);
        scopeField.setText(state.scope);

        if (state.accessToken != null) {
            onRefreshSucceeded();
        }
    }

    public void reset() {
        this.state = null;

        enabled.setSelected(false);
        tokenURLField.clear();
        clientIDField.clear();
        clientSecretField.clear();
        usernameField.clear();
        passwordField.clear();
        headerPrefixField.clear();
        scopeField.clear();
        accessTokenField.clear();
        refreshTokenField.clear();
        expiryLabel.setVisible(false);
    }

    public void setAccessToken(ROPCToken accessToken) {
        state.accessToken = accessToken;
        Platform.runLater(() -> {
            onRefreshSucceeded();
            accessTokenField.requestLayout();
            refreshTokenField.requestLayout();
        });
    }

    private void onRefreshSucceeded() {
        accessTokenField.clear();
        refreshTokenField.clear();

        accessTokenField.setText(state.accessToken.getAccessToken());

        if (state.accessToken.getRefreshToken() != null) {
            refreshTokenField.setText(state.accessToken.getRefreshToken());
        }

        setExpiryLabel();

        rippler.createManualRipple().run();
    }

    private void onRefreshFailed(Throwable exception) {
        String errorMessage;
        if (exception.getClass().equals(AuthWindowClosedException.class)) {
            // DashboardController already shows an error for this
            return;
        } else if (exception.getClass().equals(AccessTokenDeniedException.class)) {
            errorMessage = "Access token denied by token endpoint.";
        } else if (exception.getClass().equals(MalformedURLException.class)) {
            errorMessage = "Invalid URL(s).";
        } else {
            errorMessage = "Could not refresh OAuth 2.0 ROPC tokens.";
        }

        NotificationsManager.push(DashboardController.CHANNEL_ID, errorMessage, 10000);
        Logger.warning(errorMessage, (Exception) exception);
    }

    private class TokenFetcher extends Task<ROPCToken> {
        @Override
        protected ROPCToken call() throws Exception {
            ROPCProvider provider = ROPCController.this.provider;
            return provider.getAccessToken();
        }

        @Override
        protected void succeeded() {
            state.accessToken = getValue();
            onRefreshSucceeded();
        }

        @Override
        protected void failed() {
            Throwable exception = getException();
            onRefreshFailed(exception);
        }
    }
}

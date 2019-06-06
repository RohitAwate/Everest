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
import com.rohitawate.everest.auth.oauth2.Flow;
import com.rohitawate.everest.auth.oauth2.OAuth2FlowProvider;
import com.rohitawate.everest.auth.oauth2.ROPCFlowProvider;
import com.rohitawate.everest.auth.oauth2.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.tokens.OAuth2Token;
import com.rohitawate.everest.auth.oauth2.tokens.ROPCToken;
import com.rohitawate.everest.controllers.DashboardController;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.notifications.NotificationsManager;
import com.rohitawate.everest.state.auth.OAuth2FlowState;
import com.rohitawate.everest.state.auth.ROPCState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class ROPCController extends OAuth2FlowController {
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

    private JFXRippler rippler;

    private ROPCFlowProvider provider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshTokenButton.setOnAction(this::refreshToken);
        expiryLabel.setVisible(false);

        rippler = new JFXRippler(accessTokenBox);
        rippler.setPrefSize(ropcBox.getPrefWidth(), ropcBox.getPrefHeight());
        ropcBox.getChildren().add(rippler);

        initExpiryCountdown();
    }

    @Override
    void refreshToken(ActionEvent actionEvent) {
        if (provider == null) {
            provider = (ROPCFlowProvider) OAuth2FlowProvider.getProvider(
                    Flow.RESOURCE_OWNER_PASSWORD_CREDS, this);
        }

        ExecutorService service = EverestUtilities.newDaemonSingleThreadExecutor();
        service.submit(new TokenFetcher());
    }

    @Override
    public ROPCFlowProvider getAuthProvider() {
        if (provider == null) {
            provider = (ROPCFlowProvider) OAuth2FlowProvider.getProvider(
                    Flow.RESOURCE_OWNER_PASSWORD_CREDS, this);
        }

        return provider;
    }

    @Override
    public OAuth2FlowState getState() {
        if (state == null) {
            state = new ROPCState();
        }

        ROPCState ropcState = (ROPCState) state;

        ropcState.enabled = enabled.isSelected();
        ropcState.accessTokenURL = tokenURLField.getText();

        ropcState.clientID = clientIDField.getText();
        ropcState.clientSecret = clientSecretField.getText();
        ropcState.username = usernameField.getText();
        ropcState.password = passwordField.getText();

        ropcState.headerPrefix = headerPrefixField.getText();
        ropcState.scope = scopeField.getText();

        // Setting these values again since they can be modified from the UI
        if (ropcState.accessToken != null) {
            String accessToken = accessTokenField.getText();
            String refreshToken = refreshTokenField.getText();

            if (accessToken.isBlank() && refreshToken.isBlank()) {
                ropcState.accessToken = null;
            } else {
                if (ropcState.accessToken == null) {
                    ropcState.accessToken = new ROPCToken();
                }

                ROPCToken token = (ROPCToken) ropcState.accessToken;
                token.setAccessToken(accessToken);
                token.setRefreshToken(refreshToken);
            }
        }

        return ropcState;
    }

    @Override
    public void setState(OAuth2FlowState state) {
        if (state == null) {
            return;
        }

        this.state = state;

        ROPCState ropcState = (ROPCState) state;

        enabled.setSelected(ropcState.enabled);
        tokenURLField.setText(ropcState.accessTokenURL);

        clientIDField.setText(ropcState.clientID);
        clientSecretField.setText(ropcState.clientSecret);
        usernameField.setText(ropcState.username);
        passwordField.setText(ropcState.password);

        headerPrefixField.setText(ropcState.headerPrefix);
        scopeField.setText(ropcState.scope);

        if (ropcState.accessToken != null) {
            onRefreshSucceeded();
        }
    }

    @Override
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

    @Override
    public void setAccessToken(OAuth2Token accessToken) {
        state.accessToken = accessToken;
        Platform.runLater(() -> {
            onRefreshSucceeded();
            accessTokenField.requestLayout();
            refreshTokenField.requestLayout();
        });
    }

    @Override
    void onRefreshSucceeded() {
        accessTokenField.clear();
        refreshTokenField.clear();

        if (state == null || state.accessToken == null) return;

        ROPCToken token = (ROPCToken) state.accessToken;
        accessTokenField.setText(token.getAccessToken());

        if (token.getRefreshToken() != null) {
            refreshTokenField.setText(token.getRefreshToken());
        }

        setExpiryLabel();
        rippler.createManualRipple().run();
    }

    @Override
    void onRefreshFailed(Throwable exception) {
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

    private class TokenFetcher extends Task<OAuth2Token> {
        @Override
        protected OAuth2Token call() throws Exception {
            ROPCFlowProvider provider = ROPCController.this.provider;
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

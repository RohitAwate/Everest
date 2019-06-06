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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import com.rohitawate.everest.auth.captors.CaptureMethod;
import com.rohitawate.everest.auth.oauth2.ImplicitFlowProvider;
import com.rohitawate.everest.auth.oauth2.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.tokens.OAuth2Token;
import com.rohitawate.everest.controllers.DashboardController;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.notifications.NotificationsManager;
import com.rohitawate.everest.state.auth.ImplicitState;
import com.rohitawate.everest.state.auth.OAuth2FlowState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/*
 * The Implicit Grant flow can only use the WebView and not the system browser.
 * This is because it uses a URL fragment (https://en.wikipedia.org/wiki/Fragment_identifier)
 * for transferring the access token, as opposed to query parameters or the response body.
 * URL fragments are not a part of HTTP requests and are only accessible within the browser.
 * As such, Everest's CaptureServer cannot capture them via requests.
 *
 * Hence, the System Browser option has been removed and the combo-box disabled
 * for this flow.
 */
public class ImplicitController extends OAuth2FlowController {
    @FXML
    private VBox implicitBox, accessTokenBox;
    @FXML
    private JFXCheckBox enabled;
    @FXML
    private ComboBox<String> captureMethodBox;
    @FXML
    private JFXTextField authURLField, redirectURLField,
            clientIDField, scopeField, stateField,
            headerPrefixField, accessTokenField;
    @FXML
    private JFXButton refreshTokenButton;

    private JFXRippler rippler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        captureMethodBox.getItems().add(CaptureMethod.WEB_VIEW);
        captureMethodBox.setValue(CaptureMethod.WEB_VIEW);
        refreshTokenButton.setOnAction(this::refreshToken);
        expiryLabel.setVisible(false);

        rippler = new JFXRippler(accessTokenBox);
        rippler.setPrefSize(implicitBox.getPrefWidth(), implicitBox.getPrefHeight());
        implicitBox.getChildren().add(rippler);

        initExpiryCountdown();
    }

    @Override
    void refreshToken(ActionEvent actionEvent) {
        TokenFetcher tokenFetcher = new TokenFetcher();
        try {
            state.accessToken = tokenFetcher.call();
            onRefreshSucceeded();
        } catch (Exception e) {
            onRefreshFailed(e);
        }
    }

    @Override
    public void setState(OAuth2FlowState state) {
        this.state = state;
        ImplicitState implicitState = (ImplicitState) state;

        if (implicitState != null) {
            authURLField.setText(implicitState.authURL);
            redirectURLField.setText(implicitState.redirectURL);
            clientIDField.setText(implicitState.clientID);
            scopeField.setText(implicitState.scope);
            stateField.setText(implicitState.state);
            headerPrefixField.setText(implicitState.headerPrefix);
            enabled.setSelected(implicitState.enabled);

            if (implicitState.accessToken != null) {
                onRefreshSucceeded();
            }
        }
    }

    @Override
    public void reset() {
        authURLField.clear();
        redirectURLField.clear();
        clientIDField.clear();
        scopeField.clear();
        stateField.clear();
        headerPrefixField.clear();
        accessTokenField.clear();
        expiryLabel.setVisible(false);
        enabled.setSelected(false);
        state = null;
    }

    @Override
    public OAuth2FlowState getState() {
        if (state == null) {
            state = new ImplicitState();
            return state;
        }

        ImplicitState implicitState = (ImplicitState) state;

        implicitState.authURL = authURLField.getText();
        implicitState.redirectURL = redirectURLField.getText();
        implicitState.clientID = clientIDField.getText();
        implicitState.scope = scopeField.getText();
        implicitState.state = stateField.getText();
        implicitState.headerPrefix = headerPrefixField.getText();
        implicitState.enabled = enabled.isSelected();

        if (implicitState.accessToken != null) {
            // Setting this value again since it can be modified from the UI
            implicitState.accessToken.setAccessToken(accessTokenField.getText());
        }

        return state;
    }

    @Override
    public ImplicitFlowProvider getAuthProvider() {
        /*
            This method is always called on the JavaFX application thread, which is also required for
            creating and using the WebView. Hence, refreshToken() is called here itself if the accessToken is absent,
            so that when RequestManager invokes AuthCodeProvider's getAuthHeader() from a different thread,
            the accessToken is already present and hence the WebView wouldn't need to be opened.
         */
        if (accessTokenField.getText().isEmpty() && enabled.isSelected()) {
            refreshToken(null);
        }

        return new ImplicitFlowProvider(this);
    }

    @Override
    void onRefreshSucceeded() {
        accessTokenField.clear();
        accessTokenField.setText(state.accessToken.getAccessToken());
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
            errorMessage = "Could not refresh OAuth 2.0 Implicit Grant tokens.";
        }

        NotificationsManager.push(DashboardController.CHANNEL_ID, errorMessage, 10000);
        Logger.warning(errorMessage, (Exception) exception);
    }

    @Override
    public void setAccessToken(OAuth2Token accessToken) {
        state.accessToken = accessToken;
        Platform.runLater(() -> {
            onRefreshSucceeded();
            accessTokenField.requestLayout();
        });
    }

    private class TokenFetcher extends Task<OAuth2Token> {
        @Override
        protected OAuth2Token call() throws Exception {
            ImplicitFlowProvider provider = new ImplicitFlowProvider(ImplicitController.this);
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

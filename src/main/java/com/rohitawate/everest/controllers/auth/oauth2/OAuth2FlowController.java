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

import com.rohitawate.everest.Main;
import com.rohitawate.everest.auth.oauth2.OAuth2FlowProvider;
import com.rohitawate.everest.auth.oauth2.tokens.OAuth2Token;
import com.rohitawate.everest.state.auth.OAuth2FlowState;
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
import javafx.util.Duration;

public abstract class OAuth2FlowController implements Initializable {
    @FXML
    Label expiryLabel;

    OAuth2FlowState state;

    /**
     * @return The OAuth 2.0 Provider responsible for handling
     * the flow logic for the controller.
     */
    public abstract OAuth2FlowProvider getAuthProvider();

    /**
     * Triggered when a token request completes successfully.
     */
    abstract void onRefreshSucceeded();

    /**
     * Triggered when a token request fails.
     *
     * @param exception - The exception thrown by the failed request
     */
    abstract void onRefreshFailed(Throwable exception);

    /**
     * Adds the token to the controller's state and displays it.
     *
     * @param token
     */
    public abstract void setAccessToken(OAuth2Token token);

    /**
     * @return The state of the controller.
     */
    public abstract OAuth2FlowState getState();

    /**
     * Accepts the state to be saved in and applied to the controller.
     *
     * @param state
     */
    public abstract void setState(OAuth2FlowState state);

    /**
     * Clears the state of the controller.
     */
    public abstract void reset();

    /**
     * Event handler that is triggered when the user clicks on the REFRESH button
     * in the UI or when a token is requested programmatically by RequestManager.
     * <p>
     * This method must resolve the call by forking a concurrent thread for token
     * fetching or do so in a blocking manner depending on how it is invoked.
     */
    abstract void refreshToken(ActionEvent actionEvent);

    void initExpiryCountdown() {
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

    void setExpiryLabel() {
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
}

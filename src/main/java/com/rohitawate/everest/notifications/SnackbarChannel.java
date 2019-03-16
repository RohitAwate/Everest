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

package com.rohitawate.everest.notifications;

import com.jfoenix.controls.JFXSnackbar;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * JFoenix Snackbar implementation of a notification channel.
 */
public class SnackbarChannel extends JFXSnackbar implements Channel {
    private Label label = new Label();

    public SnackbarChannel(Pane container) {
        super(container);
        label.getStyleClass().add("snackbar-label");
    }

    @Override
    public void push(String message, long duration) {
        label.setText(message);
        enqueue(new SnackbarEvent(label, Duration.millis(duration), null));
    }
}

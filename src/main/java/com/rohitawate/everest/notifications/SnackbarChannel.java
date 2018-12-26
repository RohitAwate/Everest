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
        enqueue(new SnackbarEvent(label, Duration.seconds(duration), null));
    }
}

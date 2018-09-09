package com.rohitawate.everest.controllers.auth;

import com.jfoenix.controls.JFXCheckBox;
import com.rohitawate.everest.state.SimpleAuthState;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SimpleAuthController {
    @FXML
    private TextField usernameField, passwordField;
    @FXML
    private JFXCheckBox checkBox;

    boolean isSelected() {
        return checkBox.isSelected();
    }

    String getUsername() {
        if (usernameField.getText() == null)
            return "";

        return usernameField.getText();
    }

    String getPassword() {
        if (passwordField.getText() == null)
            return "";

        return passwordField.getText();
    }

    SimpleAuthState getState() {
        return new SimpleAuthState(usernameField.getText(), passwordField.getText(), checkBox.isSelected());
    }

    void setState(SimpleAuthState state) {
        if (state != null) {
            usernameField.setText(state.username);
            passwordField.setText(state.password);
            checkBox.setSelected(state.enabled);
        }
    }

    public void reset() {
        usernameField.clear();
        passwordField.clear();
        checkBox.setSelected(false);
    }
}

package com.rohitawate.everest.controllers.auth;

import com.jfoenix.controls.JFXCheckBox;
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

    void setState(String username, String password, boolean enabled) {
        usernameField.setText(username);
        passwordField.setText(password);
        checkBox.setSelected(enabled);
    }
}

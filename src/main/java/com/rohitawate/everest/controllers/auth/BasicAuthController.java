package com.rohitawate.everest.controllers.auth;

import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class BasicAuthController {
    @FXML
    private TextField usernameField, passwordField;
    @FXML
    private JFXCheckBox checkBox;

    boolean isSelected() {
        return checkBox.isSelected();
    }

    String getUsername() {
        return usernameField.getText();
    }

    String getPassword() {
        return passwordField.getText();
    }

    void setState(String username, String password, boolean enabled) {
        usernameField.setText(username);
        passwordField.setText(password);
        checkBox.setSelected(enabled);
    }
}

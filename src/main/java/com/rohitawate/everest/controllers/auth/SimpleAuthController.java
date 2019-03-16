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

package com.rohitawate.everest.controllers.auth;

import com.jfoenix.controls.JFXCheckBox;
import com.rohitawate.everest.state.auth.SimpleAuthState;
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

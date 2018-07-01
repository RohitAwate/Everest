/*
 * Copyright 2018 Rohit Awate.
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

package com.rohitawate.everest.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.rohitawate.everest.controllers.state.FieldState;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

public class StringKeyValueFieldController implements Initializable {
    @FXML
    private TextField keyField, valueField;
    @FXML
    private JFXCheckBox checkBox;
    @FXML
    protected JFXButton deleteButton;

    /*
        Set to true when user manually un-checks the field
        to prevent ChangeListeners from checking it again on further edits to the field.
     */
    private boolean uncheckedAlready = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkBox.disableProperty()
                .bind(Bindings.or(keyField.textProperty().isEmpty(), valueField.textProperty().isEmpty()));
        checkBox.disableProperty()
                .addListener(observable -> {
                    if (isChecked() && (keyField.getText().equals("") || valueField.getText().equals(""))) {
                        checkBox.setSelected(false);
                        uncheckedAlready = false;
                    }
                });
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == true && newValue == false)
                uncheckedAlready = true;
        });
        valueField.textProperty()
                .addListener(observable -> {
                    if (!keyField.getText().equals("") && !valueField.getText().equals("") && !uncheckedAlready)
                        checkBox.selectedProperty().set(true);
                });
        keyField.textProperty()
                .addListener(observable -> {
                    if (!keyField.getText().equals("") && !valueField.getText().equals("") && !uncheckedAlready)
                        checkBox.selectedProperty().set(true);
                });
    }

    public Pair<String, String> getHeader() {
        return new Pair<>(keyField.getText(), valueField.getText());
    }

    public boolean isChecked() {
        return checkBox.isSelected();
    }

    public void setKeyField(String key) {
        keyField.setText(key);
    }

    public void setValueField(String value) {
        valueField.setText(value);
    }

    public boolean isKeyFieldEmpty() {
        return keyField.getText().isEmpty();
    }

    public boolean isValueFieldEmpty() {
        return valueField.getText().isEmpty();
    }

    public FieldState getState() {
        return new FieldState(keyField.getText(), valueField.getText(), checkBox.isSelected());
    }

    public void setChecked(boolean checked) {
        checkBox.setSelected(checked);
    }
}

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
import com.rohitawate.everest.state.FieldState;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

public class FileKeyValueFieldController implements Initializable {
    @FXML
    private TextField fileKeyField, fileValueField;
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
                .bind(Bindings.or(fileKeyField.textProperty().isEmpty(), fileValueField.textProperty().isEmpty()));
        checkBox.disableProperty()
                .addListener(observable -> {
                    if (isChecked() && (fileKeyField.getText().equals("") || fileValueField.getText().equals(""))) {
                        checkBox.setSelected(false);
                        uncheckedAlready = false;
                    }
                });
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == true && newValue == false)
                uncheckedAlready = true;
        });
        fileValueField.textProperty()
                .addListener(observable -> {
                    if (!fileKeyField.getText().equals("") && !fileValueField.getText().equals("") && !uncheckedAlready)
                        checkBox.selectedProperty().set(true);
                });
        fileKeyField.textProperty()
                .addListener(observable -> {
                    if (!fileKeyField.getText().equals("") && !fileValueField.getText().equals("") && !uncheckedAlready)
                        checkBox.selectedProperty().set(true);
                });
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a binary file to add to the request");
        Window dashboardWindow = fileValueField.getScene().getWindow();
        String filePath;
        try {
            filePath = fileChooser.showOpenDialog(dashboardWindow).getAbsolutePath();
        } catch (NullPointerException NPE) {
            filePath = "";
        }
        fileValueField.setText(filePath);
    }

    public Pair<String, String> getHeader() {
        return new Pair<>(fileKeyField.getText(), fileValueField.getText());
    }

    public boolean isChecked() {
        return checkBox.isSelected();
    }

    public void setFileKeyField(String key) {
        fileKeyField.setText(key);
    }

    public void setFileValueField(String value) {
        fileValueField.setText(value);
    }

    public boolean isFileKeyFieldEmpty() {
        return fileKeyField.getText().isEmpty();
    }

    public boolean isFileValueFieldEmpty() {
        return fileValueField.getText().isEmpty();
    }

    public FieldState getState() {
        return new FieldState(fileKeyField.getText(), fileValueField.getText(), checkBox.isSelected());
    }

    public void setChecked(boolean checked) {
        checkBox.setSelected(checked);
    }

    public void setKeyHandler(EventHandler<KeyEvent> handler) {
        fileKeyField.setOnKeyPressed(handler);
        fileValueField.setOnKeyPressed(handler);
    }
}

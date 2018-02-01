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

package com.rohitawate.restaurant.dashboard;

import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
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

    public Pair<String, String> getHeader() {
        return new Pair<>(fileKeyField.getText(), fileValueField.getText());
    }

    public boolean isChecked() {
        return checkBox.isSelected();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkBox.disableProperty().bind(Bindings.or(fileKeyField.textProperty().isEmpty(), fileValueField.textProperty().isEmpty()));
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a binary file to add to request...");
        Window dashboardWindow = fileValueField.getScene().getWindow();
        String filePath;
        try {
            filePath = fileChooser.showOpenDialog(dashboardWindow).getAbsolutePath();
        } catch (NullPointerException NPE) {
            filePath = "";
        }
        fileValueField.setText(filePath);
    }
}

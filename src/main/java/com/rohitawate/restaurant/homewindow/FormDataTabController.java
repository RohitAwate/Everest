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

package com.rohitawate.restaurant.homewindow;

import com.rohitawate.restaurant.util.themes.ThemeManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class FormDataTabController implements Initializable {
    @FXML
    private VBox fieldsBox;

    private List<StringKeyValueFieldController> stringControllers;
    private List<FileKeyValueFieldController> fileControllers;
    private IntegerProperty fileControllersCount, stringControllersCount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stringControllers = new ArrayList<>();
        stringControllersCount = new SimpleIntegerProperty(stringControllers.size());

        fileControllers = new ArrayList<>();
        fileControllersCount = new SimpleIntegerProperty(fileControllers.size());

        addFileField();
        addStringField();
    }

    private void addFileField() {
        addFileField("", "", null);
    }

    @FXML
    private void addFileField(ActionEvent event) {
        addFileField("", "", event);
    }

    public void addFileField(String key, String value) {
        addFileField(key, value, null);
    }

    private void addFileField(String key, String value, ActionEvent event) {
        /*
            Re-uses previous field if it is empty,
            else loads a new one.
         */
        if (fileControllers.size() > 0 && event == null) {
            FileKeyValueFieldController previousController = fileControllers.get(fileControllers.size() - 1);

            if (previousController.isFileKeyFieldEmpty() &&
                    previousController.isFileValueFieldEmpty()) {
                previousController.setFileKeyField(key);
                previousController.setFileValueField(value);
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/FileKeyValueField.fxml"));
            Parent fileField = loader.load();
            ThemeManager.setTheme(fileField);
            FileKeyValueFieldController controller = loader.getController();
            controller.setFileKeyField(key);
            controller.setFileValueField(value);
            fileControllers.add(controller);
            fileControllersCount.set(fileControllersCount.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(fileControllersCount, 1));
            controller.deleteButton.setOnAction(e -> {
                fieldsBox.getChildren().remove(fileField);
                fileControllers.remove(controller);
                fileControllersCount.set(fileControllersCount.get() - 1);
            });
            fieldsBox.getChildren().add(fileField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addStringField() {
        addStringField("", "", null);
    }

    @FXML
    private void addStringField(ActionEvent event) {
        addStringField("", "", event);
    }

    public void addStringField(String key, String value) {
        addStringField(key, value, null);
    }

    private void addStringField(String key, String value, ActionEvent event) {
        /*
            Re-uses previous field if it is empty,
            else loads a new one.
         */
        if (stringControllers.size() > 0 && event == null) {
            StringKeyValueFieldController previousController = stringControllers.get(stringControllers.size() - 1);

            if (previousController.isKeyFieldEmpty() &&
                    previousController.isValueFieldEmpty()) {
                previousController.setKeyField(key);
                previousController.setValueField(value);
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/StringKeyValueField.fxml"));
            Parent stringField = loader.load();
            StringKeyValueFieldController controller = loader.getController();
            controller.setKeyField(key);
            controller.setValueField(value);
            stringControllers.add(controller);
            stringControllersCount.set(stringControllersCount.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(stringControllersCount, 1));
            controller.deleteButton.setOnAction(e -> {
                fieldsBox.getChildren().remove(stringField);
                stringControllers.remove(controller);
                stringControllersCount.set(stringControllersCount.get() - 1);
            });
            fieldsBox.getChildren().add(stringField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getStringTuples() {
        HashMap<String, String> headers = new HashMap<>();
        for (StringKeyValueFieldController controller : stringControllers) {
            if (controller.isChecked())
                headers.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }
        return headers;
    }

    public HashMap<String, String> getFileTuples() {
        HashMap<String, String> headers = new HashMap<>();
        for (FileKeyValueFieldController controller : fileControllers) {
            if (controller.isChecked())
                headers.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }
        return headers;
    }
}

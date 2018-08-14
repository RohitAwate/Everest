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

import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.ThemeManager;
import com.rohitawate.everest.state.FieldState;
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
import java.time.LocalDateTime;
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

    private HashMap<String, String> stringMap;
    private HashMap<String, String> fileMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stringControllers = new ArrayList<>();
        stringControllersCount = new SimpleIntegerProperty(0);

        fileControllers = new ArrayList<>();
        fileControllersCount = new SimpleIntegerProperty(0);

        addFileField();
        addStringField();
    }

    public void addFileField(FieldState state) {
        addFileField(state.key, state.value, state.checked);
    }

    private void addFileField() {
        addFileField("", "", false);
    }

    private void addFileField(String key, String value, boolean checked) {
        /*
            Re-uses previous field if it is empty, else loads a new one.
            A value of null for the 'event' parameter indicates that the method call
            came from code and not from the user. This call is made while recovering
            the application state.
         */
        if (fileControllers.size() > 0) {
            FileKeyValueFieldController previousController = fileControllers.get(fileControllers.size() - 1);

            if (previousController.isFileKeyFieldEmpty() &&
                    previousController.isFileValueFieldEmpty()) {
                previousController.setFileKeyField(key);
                previousController.setFileValueField(value);

                /*
                    For when the last field is loaded from setState.
                    This makes sure an extra blank field is always present.
                */
                if (!key.equals("") && !value.equals(""))
                    addFileField();

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
            controller.setChecked(checked);
            fileControllers.add(controller);
            fileControllersCount.set(fileControllersCount.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(fileControllersCount, 1));
            controller.deleteButton.setOnAction(e -> {
                fieldsBox.getChildren().remove(fileField);
                fileControllers.remove(controller);
                fileControllersCount.set(fileControllersCount.get() - 1);
            });
            controller.setKeyHandler(keyEvent -> addFileField());
            fieldsBox.getChildren().add(fileField);
        } catch (IOException e) {
            LoggingService.logSevere("Could not add file field.", e, LocalDateTime.now());
        }
    }

    public void addStringField(FieldState state) {
        addStringField(state.key, state.value, null, state.checked);
    }

    private void addStringField() {
        addStringField("", "", null, false);
    }

    private void addStringField(String key, String value, ActionEvent event, boolean checked) {
        /*
            Re-uses previous field if it is empty, else loads a new one.
            A value of null for the 'event' parameter indicates that the method call
            came from code and not from the user. This call is made while recovering
            the application state.
         */
        if (stringControllers.size() > 0 && event == null) {
            StringKeyValueFieldController previousController = stringControllers.get(stringControllers.size() - 1);

            if (previousController.isKeyFieldEmpty() &&
                    previousController.isValueFieldEmpty()) {
                previousController.setKeyField(key);
                previousController.setValueField(value);

                /*
                    For when the last field is loaded from setState.
                    This makes sure an extra blank field is always present.
                */
                if (!key.equals("") && !value.equals(""))
                    addStringField();

                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/StringKeyValueField.fxml"));
            Parent stringField = loader.load();
            StringKeyValueFieldController controller = loader.getController();
            controller.setKeyField(key);
            controller.setValueField(value);
            controller.setChecked(checked);
            stringControllers.add(controller);
            stringControllersCount.set(stringControllersCount.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(stringControllersCount, 1));
            controller.deleteButton.setOnAction(e -> {
                fieldsBox.getChildren().remove(stringField);
                stringControllers.remove(controller);
                stringControllersCount.set(stringControllersCount.get() - 1);
            });
            controller.setKeyHandler(keyEvent -> addStringField());
            fieldsBox.getChildren().add(stringField);
        } catch (IOException e) {
            LoggingService.logSevere("Could not add string field.", e, LocalDateTime.now());
        }
    }

    /**
     * @return Map of selected string tuples from multipart-form tab
     */
    public HashMap<String, String> getStringTuples() {
        if (stringMap == null)
            stringMap = new HashMap<>();

        stringMap.clear();
        for (StringKeyValueFieldController controller : stringControllers) {
            if (controller.isChecked())
                stringMap.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }

        return stringMap;
    }

    /**
     * @return Map of selected file tuples from multipart-form tab
     */
    public HashMap<String, String> getFileTuples() {
        if (fileMap == null)
            fileMap = new HashMap<>();

        fileMap.clear();
        for (FileKeyValueFieldController controller : fileControllers) {
            if (controller.isChecked())
                fileMap.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }
        return fileMap;
    }


    /**
     * @return List of the states of all the string fields in the Form data tab.
     */
    public ArrayList<FieldState> getStringFieldStates() {
        ArrayList<FieldState> states = new ArrayList<>();

        for (StringKeyValueFieldController controller : stringControllers)
            states.add(controller.getState());

        return states;
    }


    /**
     * @return List of the states of all the file fields in the Form data tab.
     */
    public ArrayList<FieldState> getFileFieldStates() {
        ArrayList<FieldState> states = new ArrayList<>();

        for (FileKeyValueFieldController controller : fileControllers)
            states.add(controller.getState());

        return states;
    }

    void clear() {
        if (stringMap != null)
            stringMap.clear();

        if (fileMap != null)
            fileMap.clear();

        if (stringControllers != null)
            stringControllers.clear();

        if (fileControllers != null)
            fileControllers.clear();

        fieldsBox.getChildren().clear();

        stringControllersCount.set(0);
        fileControllersCount.set(0);

        addStringField();
        addFileField();
    }
}

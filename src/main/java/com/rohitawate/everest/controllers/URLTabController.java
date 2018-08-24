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

public class URLTabController implements Initializable {
    @FXML
    private VBox fieldsBox;

    private List<StringKeyValueFieldController> controllers;
    private IntegerProperty controllersCount;
    private HashMap<String, String> tuples;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controllers = new ArrayList<>();
        controllersCount = new SimpleIntegerProperty(controllers.size());
        addField();
    }


    public void addField(FieldState state) {
        addField(state.key, state.value, state.checked);
    }

    private void addField() {
        addField("", "", false);
    }

    private void addField(String key, String value, boolean checked) {
        /*
            Re-uses previous field if it is empty, else loads a new one.
            A value of null for the 'event' parameter indicates that the method call
            came from code and not from the user. This call is made while recovering
            the application state.
         */
        if (controllers.size() > 0) {
            StringKeyValueFieldController previousController = controllers.get(controllers.size() - 1);

            if (previousController.isKeyFieldEmpty() &&
                    previousController.isValueFieldEmpty()) {
                previousController.setKeyField(key);
                previousController.setValueField(value);
                previousController.setChecked(checked);

                /*
                    For when the last field is loaded from setState.
                    This makes sure an extra blank field is always present.
                */
                if (!key.equals("") && !value.equals(""))
                    addField();

                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/StringKeyValueField.fxml"));
            Parent stringField = loader.load();
            ThemeManager.setTheme(stringField);
            StringKeyValueFieldController controller = loader.getController();
            controller.setKeyField(key);
            controller.setValueField(value);
            controller.setChecked(checked);
            controllers.add(controller);
            controllersCount.set(controllersCount.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(controllersCount, 1));
            controller.deleteButton.setOnAction(e -> {
                fieldsBox.getChildren().remove(stringField);
                controllers.remove(controller);
                controllersCount.set(controllersCount.get() - 1);
            });
            controller.setKeyHandler(keyEvent -> addField());
            fieldsBox.getChildren().add(stringField);
        } catch (IOException e) {
            LoggingService.logSevere("Could not add string field.", e, LocalDateTime.now());
        }
    }

    /**
     * @return Map of selected string tuples from URL-encoded tab.
     */
    public HashMap<String, String> getStringTuples() {
        if (tuples == null)
            tuples = new HashMap<>();

        tuples.clear();
        for (StringKeyValueFieldController controller : controllers) {
            if (controller.isChecked())
                tuples.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }

        return tuples;
    }


    /**
     * @return A list of the states of all the non-empty fields in the URL-encoded tab.
     */
    public ArrayList<FieldState> getFieldStates() {
        ArrayList<FieldState> states = new ArrayList<>();

        for (StringKeyValueFieldController controller : controllers) {
            if (!(controller.isKeyFieldEmpty() && controller.isValueFieldEmpty()))
                states.add(controller.getState());
        }

        return states;
    }

    void clear() {
        if (tuples != null)
            tuples.clear();
        if (controllers != null)
            controllers.clear();

        fieldsBox.getChildren().clear();
        controllersCount.set(0);
        addField();
    }
}

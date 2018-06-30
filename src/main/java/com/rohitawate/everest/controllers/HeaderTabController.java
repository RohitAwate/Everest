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

import com.rohitawate.everest.controllers.state.FieldState;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
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

public class HeaderTabController implements Initializable {
    @FXML
    private VBox headersBox;

    private List<StringKeyValueFieldController> controllers;
    private IntegerProperty controllersCount;

    private HashMap<String, String> headers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controllers = new ArrayList<>();
        controllersCount = new SimpleIntegerProperty(controllers.size());
        addHeader();
    }

    public void addHeader(FieldState state) {
        addHeader(state.key, state.value, null, state.checked);
    }

    private void addHeader() {
        addHeader("", "", null, false);
    }

    @FXML
    private void addHeader(ActionEvent event) {
        addHeader("", "", event, false);
    }

    private void addHeader(String key, String value, ActionEvent event, boolean checked) {
        /*
            Re-uses previous field if it is empty,
            else loads a new one.
         */
        if (controllers.size() > 0 && event == null) {
            StringKeyValueFieldController previousController = controllers.get(controllers.size() - 1);

            if (previousController.isKeyFieldEmpty() &&
                    previousController.isValueFieldEmpty()) {
                previousController.setKeyField(key);
                previousController.setValueField(value);
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/StringKeyValueField.fxml"));
            Parent headerField = loader.load();
            ThemeManager.setTheme(headerField);
            StringKeyValueFieldController controller = loader.getController();
            controller.setKeyField(key);
            controller.setValueField(value);
            controller.setChecked(checked);
            controllers.add(controller);
            controllersCount.set(controllersCount.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(controllersCount, 1));
            controller.deleteButton.setOnAction(e -> {
                headersBox.getChildren().remove(headerField);
                controllers.remove(controller);
                controllersCount.set(controllersCount.get() - 1);
            });
            headersBox.getChildren().add(headerField);
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not string field.", e, LocalDateTime.now());
        }
    }

    /**
     * Returns a map of the checked/unchecked headers.
     * @param onlyChecked - Returns only the headers which are selected by the user
     */
    public HashMap<String, String> getHeaders(boolean onlyChecked) {
        if (headers == null)
            headers = new HashMap<>();

        headers.clear();
        for (StringKeyValueFieldController controller : controllers) {
            if (onlyChecked)
                if (!controller.isChecked())
                    continue;

            headers.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }

        return headers;
    }

    /**
     * Return an ArrayList of the state of all the fields in the Headers tab.
     */
    public ArrayList<FieldState> getFieldStates() {
        ArrayList<FieldState> states = new ArrayList<>();

        for (StringKeyValueFieldController controller : controllers)
            states.add(controller.getState());

        return states;
    }
}
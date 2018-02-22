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

public class HeaderTabController implements Initializable {
    @FXML
    private VBox headersBox;

    private List<StringKeyValueFieldController> controllers;
    private IntegerProperty controllersCount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controllers = new ArrayList<>();
        controllersCount = new SimpleIntegerProperty(controllers.size());
        addHeader();
    }

    public void addHeader(String key, String value) {
        addHeader(key, value, null);
    }

    private void addHeader() {
        addHeader("", "", null);
    }

    @FXML
    private void addHeader(ActionEvent event) {
        addHeader("", "", event);
    }

    private void addHeader(String key, String value, ActionEvent event) {
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
            e.printStackTrace();
        }
    }


    public HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        for (StringKeyValueFieldController controller : controllers) {
            if (controller.isChecked())
                headers.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }
        return headers;
    }
}
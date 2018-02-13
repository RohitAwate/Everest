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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeWindowController implements Initializable {
    @FXML
    private TabPane homeWindowTabPane;

    private KeyCombination ctrlN = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addTab();
        Platform.runLater(() -> {
            Scene thisScene = homeWindowTabPane.getScene();
            thisScene.setOnKeyPressed(e -> {
                if (ctrlN.match(e))
                    addTab();
            });
        });
    }

    private void addTab() {
        try {
            Tab newTab = new Tab();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/Dashboard.fxml"));
            Parent dashboard = loader.load();
            DashboardController controller = loader.getController();
            newTab.setContent(dashboard);
            newTab.textProperty().bind(Bindings
                    .when(controller.getAddressProperty().isNotEmpty())
                    .then(controller.getAddressProperty())
                    .otherwise("New Tab"));
            newTab.setOnCloseRequest(e -> {
                if (homeWindowTabPane.getTabs().size() == 1)
                    addTab();
            });
            homeWindowTabPane.getTabs().add(newTab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

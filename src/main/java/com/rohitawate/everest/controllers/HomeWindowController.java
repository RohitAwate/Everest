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


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.KeyMap;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeWindowController implements Initializable {
    @FXML
    private StackPane homeWindowSP;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TabPane homeWindowTabPane;
    
    @FXML
    private SearchPaneController searchPaneController;

    private HashMap<Tab, DashboardController> tabControllerMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Using LinkedHashMap because they retain order
        tabControllerMap = new LinkedHashMap<>();
        recoverState();

        searchPaneController.addItemClickHandler(this::addTab);
        homeWindowSP.setFocusTraversable(true);

        Platform.runLater(() -> {
            homeWindowSP.requestFocus();
            this.setGlobalShortcuts();

            // Saves the state of the application before closing
            Stage thisStage = (Stage) homeWindowSP.getScene().getWindow();
            thisStage.setOnCloseRequest(e -> saveState());

         

        });
    }

    private void setGlobalShortcuts() {
        Scene thisScene = homeWindowSP.getScene();

        thisScene.setOnKeyPressed(e -> {
            if (KeyMap.newTab.match(e)) {
                addTab();
            } else if (KeyMap.focusAddressBar.match(e)) {
                Tab activeTab = getActiveTab();
                tabControllerMap.get(activeTab).addressField.requestFocus();
            } else if (KeyMap.focusMethodBox.match(e)) {
                Tab activeTab = getActiveTab();
                tabControllerMap.get(activeTab).httpMethodBox.show();
            } else if (KeyMap.sendRequest.match(e)) {
                Tab activeTab = getActiveTab();
                tabControllerMap.get(activeTab).sendRequest();
            } else if (KeyMap.toggleHistory.match(e)) {
                toggleHistoryPane();
            } else if (KeyMap.closeTab.match(e)) {
                Tab activeTab = getActiveTab();
                if (homeWindowTabPane.getTabs().size() == 1)
                    addTab();
                homeWindowTabPane.getTabs().remove(activeTab);
                tabControllerMap.remove(activeTab);
            } else if (KeyMap.searchHistory.match(e)) {
            	searchPaneController.focusSearchField();
            } else if (KeyMap.focusParams.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                controller.requestOptionsTab.getSelectionModel().select(controller.paramsTab);
            } else if (KeyMap.focusAuth.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                controller.requestOptionsTab.getSelectionModel().select(controller.authTab);
            } else if (KeyMap.focusHeaders.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                controller.requestOptionsTab.getSelectionModel().select(controller.headersTab);
            } else if (KeyMap.focusBody.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                String httpMethod = controller.httpMethodBox.getValue();
                if (!httpMethod.equals("GET") && !httpMethod.equals("DELETE")) {
                    controller.requestOptionsTab.getSelectionModel().select(controller.bodyTab);
                }
            } else if (KeyMap.refreshTheme.match(e)) {
                ThemeManager.refreshTheme();
            }
        });
    }

    private Tab getActiveTab() {
        return homeWindowTabPane.getSelectionModel().getSelectedItem();
    }

    private void toggleHistoryPane() {
        searchPaneController.toggleVisibilityIn(splitPane);
    }

    private void addTab() {
        addTab(null);
    }

    private void addTab(DashboardState dashboardState) {
        try {
            Tab newTab = new Tab();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/Dashboard.fxml"));
            Parent dashboard = loader.load();
            DashboardController controller = loader.getController();

            if (dashboardState != null)
                controller.setState(dashboardState);

            newTab.setContent(dashboard);

            // Binds the addressField text to the Tab text
            StringProperty addressProperty = controller.addressField.textProperty();
            newTab.textProperty().bind(
                    Bindings.when(addressProperty.isNotEmpty())
                            .then(addressProperty)
                            .otherwise("New Tab"));

            // Tab closing procedure
            newTab.setOnCloseRequest(e -> {
                if (homeWindowTabPane.getTabs().size() == 1)
                    addTab();
                tabControllerMap.remove(newTab);
            });

            homeWindowTabPane.getTabs().add(newTab);
            homeWindowTabPane.getSelectionModel().select(newTab);
            tabControllerMap.put(newTab, controller);
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not add a new tab.", e, LocalDateTime.now());
        }
    }

    private void saveState() {
        ArrayList<DashboardState> dashboardStates = new ArrayList<>();

        // Get the states of all the tabs
        for (DashboardController controller : tabControllerMap.values())
            dashboardStates.add(controller.getState());

        try {
            File stateFile = new File("Everest/config/state.json");
            EverestUtilities.jsonMapper.writeValue(stateFile, dashboardStates);
            Services.loggingService.logInfo("Application state saved.", LocalDateTime.now());
        } catch (IOException e) {
            Services.loggingService.logSevere("Failed to save application state.", e, LocalDateTime.now());
        }
    }

    private void recoverState() {
        try {
            File stateFile = new File("Everest/config/state.json");

            if (!stateFile.exists()) {
                Services.loggingService.logInfo("Application state file not found. Loading default state.", LocalDateTime.now());
                addTab();
                return;
            }

            ArrayList<DashboardState> dashboardStates = EverestUtilities.jsonMapper
                    .reader()
                    .forType(new TypeReference<ArrayList<DashboardState>>() {
                    })
                    .readValue(stateFile);

            if (dashboardStates.size() > 0) {
                for (DashboardState dashboardState : dashboardStates)
                    addTab(dashboardState);
            } else {
                addTab();
            }
        } catch (IOException e) {
            Services.loggingService.logWarning("Application state file is possibly corrupted. State recovery failed. Loading default state.", e, LocalDateTime.now());
        } finally {
            Services.loggingService.logInfo("Application loaded.", LocalDateTime.now());
        }

    }

	public void addHistoryItem(DashboardState state) {
		searchPaneController.addHistoryItem(state);
	}

  
}

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.rohitawate.everest.controllers.state.ComposerState;
import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.KeyMap;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class HomeWindowController implements Initializable {
    @FXML
    private StackPane homeWindowSP;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TabPane homeWindowTabPane;
    @FXML
    private HistoryPaneController historyPaneController;
    @FXML
    private VBox tabDashboardBox;

    private HashMap<Tab, DashboardState> tabStateMap;
    private DashboardController dashboard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/Dashboard.fxml"));
        try {
            Parent dashboardFXML = loader.load();
            dashboard = loader.getController();
            tabDashboardBox.getChildren().add(dashboardFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Using LinkedHashMap because they retain order
        tabStateMap = new LinkedHashMap<>();
        recoverState();

        historyPaneController.addItemClickHandler(this::addTab);
        homeWindowSP.setFocusTraversable(true);

        Platform.runLater(() -> {
            homeWindowSP.requestFocus();
            new KeymapHandler();

            // Saves the state of the application before closing
            Stage thisStage = (Stage) homeWindowSP.getScene().getWindow();
            thisStage.setOnCloseRequest(e -> saveState());
        });

        homeWindowTabPane.getSelectionModel().selectedItemProperty().addListener(this::onTabSwitched);
    }

    /**
     * Updates the current state of the Dashboard in the tabStateMap
     * corresponding to the previously selected tab. (calls DashboardController.getState())
     * Fetches the state of the new tab from tabStateMap and applies it to the Dashboard.
     *
     * @param prevTab The tab that was selected before the switch.
     * @param newTab  The tab that must be selected after the switch.
     */
    private void onTabSwitched(ObservableValue<? extends Tab> obs, Tab prevTab, Tab newTab) {
        DashboardState dashboardState = dashboard.getState();
        tabStateMap.replace(prevTab, dashboardState);

        dashboardState = tabStateMap.get(newTab);
        dashboard.reset();
        dashboard.setState(dashboardState);
    }

    /**
     * Updates the current state of the Dashboard in the tabStateMap
     * corresponding to the previously selected tab.
     * Fetches the state of the new tab from tabStateMap and applies it to the Dashboard.
     *
     * @param prevState The state of the Dashboard before the switch.
     * @param prevTab   The tab that was selected before the switch.
     * @param newTab    The tab that must be selected after the switch.
     */
    private void onTabSwitched(DashboardState prevState, Tab prevTab, Tab newTab) {
        tabStateMap.replace(prevTab, prevState);

        DashboardState newState = tabStateMap.get(newTab);
        dashboard.reset();
        dashboard.setState(newState);
    }

    private void addTab() {
        addTab(null);
    }

    /**
     * Adds a new tab to the homeWindowTabPane initialized with
     * the ComposerState provided.
     */
    private void addTab(ComposerState composerState) {
        Tab newTab = new Tab();

        StringProperty addressProperty = dashboard.addressField.textProperty();

        newTab.textProperty().bind(
                Bindings.when(addressProperty.isNotEmpty())
                        .then(addressProperty)
                        .otherwise("New Tab"));

        newTab.setOnCloseRequest(e -> {
            tabStateMap.remove(newTab);
            homeWindowTabPane.getTabs().remove(newTab);

            // Closes the application if the last tab is closed
            if (homeWindowTabPane.getTabs().size() == 0) {
                saveState();
                Stage thisStage = (Stage) homeWindowSP.getScene().getWindow();
                thisStage.close();
            }
        });

        DashboardState newState = new DashboardState(composerState);
        tabStateMap.put(newTab, newState);

        /*
            DO NOT mess with the following code. The sequence of these steps is very crucial:
             1. Get the currently selected tab.
             2. Get the current state of the dashboard to save to the map.
             3. Add the new tab, since the previous state is now with us.
             4. Switch to the new tab.
             5. Call onTabSwitched() to update the Dashboard and save the oldState.
         */
        Tab prevTab = homeWindowTabPane.getSelectionModel().getSelectedItem();
        DashboardState prevState = dashboard.getState();
        homeWindowTabPane.getTabs().add(newTab);
        homeWindowTabPane.getSelectionModel().select(newTab);
        onTabSwitched(prevState, prevTab, newTab);
    }

    private void saveState() {
        /*
            Updating the state of the selected tab before saving it.
            Other tabs will already have their states saved when they
            were loaded from state.json or on a tab switch.
          */
        Tab currentTab = homeWindowTabPane.getSelectionModel().getSelectedItem();
        DashboardState currentState = dashboard.getState();
        tabStateMap.put(currentTab, currentState);

        ArrayList<ComposerState> composerStates = new ArrayList<>();
        for (DashboardState dashboardState : tabStateMap.values())
            composerStates.add(dashboardState.composer);

        try {
            File stateFile = new File("Everest/config/state.json");
            EverestUtilities.jsonMapper.writeValue(stateFile, composerStates);
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

            ArrayList<ComposerState> composerStates = EverestUtilities.jsonMapper
                    .reader()
                    .forType(new TypeReference<ArrayList<ComposerState>>() {
                    })
                    .readValue(stateFile);

            if (composerStates.size() > 0) {
                for (ComposerState composerState : composerStates)
                    addTab(composerState);
            } else {
                addTab();
            }
        } catch (IOException e) {
            Services.loggingService.logWarning("Application state file is possibly corrupted. State recovery failed. Loading default state.", e, LocalDateTime.now());
        } finally {
            Services.loggingService.logInfo("Application loaded.", LocalDateTime.now());
        }
    }

    public void addHistoryItem(ComposerState state) {
        historyPaneController.addHistoryItem(state);
    }

    private void toggleHistoryPane() {
        historyPaneController.toggleVisibilityIn(splitPane);
    }

    private class KeymapHandler {
        private KeymapHandler() {
            Scene thisScene = homeWindowSP.getScene();

            thisScene.setOnKeyPressed(e -> {
                if (KeyMap.newTab.match(e)) {
                    addTab();
                } else if (KeyMap.focusAddressBar.match(e)) {
                    dashboard.addressField.requestFocus();
                } else if (KeyMap.focusMethodBox.match(e)) {
                    dashboard.httpMethodBox.show();
                } else if (KeyMap.sendRequest.match(e)) {
                    dashboard.sendRequest();
                } else if (KeyMap.toggleHistory.match(e)) {
                    toggleHistoryPane();
                } else if (KeyMap.closeTab.match(e)) {
                    Tab activeTab = homeWindowTabPane.getSelectionModel().getSelectedItem();
                    tabStateMap.remove(activeTab);
                    homeWindowTabPane.getTabs().remove(activeTab);
                    if (homeWindowTabPane.getTabs().size() == 0) {
                        saveState();
                        Stage thisStage = (Stage) homeWindowSP.getScene().getWindow();
                        thisStage.close();
                    }
                    homeWindowTabPane.getTabs().remove(activeTab);
                } else if (KeyMap.searchHistory.match(e)) {
                    historyPaneController.focusSearchField();
                } else if (KeyMap.focusParams.match(e)) {
                    dashboard.requestOptionsTab.getSelectionModel().select(dashboard.paramsTab);
                } else if (KeyMap.focusAuth.match(e)) {
                    dashboard.requestOptionsTab.getSelectionModel().select(dashboard.authTab);
                } else if (KeyMap.focusHeaders.match(e)) {
                    dashboard.requestOptionsTab.getSelectionModel().select(dashboard.headersTab);
                } else if (KeyMap.focusBody.match(e)) {
                    String httpMethod = dashboard.httpMethodBox.getValue();
                    if (!httpMethod.equals("GET") && !httpMethod.equals("DELETE")) {
                        dashboard.requestOptionsTab.getSelectionModel().select(dashboard.bodyTab);
                    }
                } else if (KeyMap.refreshTheme.match(e)) {
                    ThemeManager.refreshTheme();
                }
            });
        }
    }
}

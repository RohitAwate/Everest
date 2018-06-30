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
import com.jfoenix.controls.JFXButton;
import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.KeyMap;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class HomeWindowController implements Initializable {
    @FXML
    private StackPane homeWindowSP;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TabPane homeWindowTabPane;
    @FXML
    private TextField historyTextField;
    @FXML
    private VBox historyTab, searchBox, historyPane;
    @FXML
    private StackPane historyPromptLayer, searchLayer, searchFailedLayer;
    @FXML
    private JFXButton clearSearchFieldButton;

    private HashMap<Tab, DashboardController> tabControllerMap;
    private List<HistoryItemController> historyItemControllers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Using LinkedHashMap because they retain order
        tabControllerMap = new LinkedHashMap<>();
        historyItemControllers = new ArrayList<>();
        recoverState();

        searchLayer.visibleProperty().bind(historyTextField.textProperty().isNotEmpty());

        historyTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            searchBox.getChildren().remove(0, searchBox.getChildren().size());
            searchFailedLayer.setVisible(false);
            List<HistoryItemController> searchResults = getSearchResults(historyTextField.getText());

            // Method of sorting the HistoryItemControllers
            searchResults.sort((controller1, controller2) -> {
                int relativity1 = controller1.getRelativityIndex(historyTextField.getText());
                int relativity2 = controller2.getRelativityIndex(historyTextField.getText());
                if (relativity1 < relativity2)
                    return 1;
                else if (relativity1 > relativity2)
                    return -1;
                else
                    return 0;
            });

            if (searchResults.size() != 0) {
                for (HistoryItemController controller : searchResults) {
                    addSearchItem(controller.getState());
                }
            } else {
                searchFailedLayer.setVisible(true);
            }
        }));

        clearSearchFieldButton.setOnAction(e -> historyTextField.clear());

        homeWindowSP.setFocusTraversable(true);

        Platform.runLater(() -> {
            homeWindowSP.requestFocus();
            this.setGlobalShortcuts();

            // Saves the state of the application before closing
            Stage thisStage = (Stage) homeWindowSP.getScene().getWindow();
            thisStage.setOnCloseRequest(e -> saveState());

            // Loads the history
            Task<List<DashboardState>> historyLoader = new Task<List<DashboardState>>() {
                @Override
                protected List<DashboardState> call() {
                    return Services.historyManager.getHistory();
                }
            };

            // Appends the history items to the HistoryTab
            historyLoader.setOnSucceeded(e -> {
                try {
                    List<DashboardState> history = historyLoader.get();
                    if (history.size() == 0) {
                        historyPromptLayer.setVisible(true);
                        return;
                    }

                    for (DashboardState state : history)
                        addHistoryItem(state);
                } catch (InterruptedException | ExecutionException E) {
                    Services.loggingService.logSevere("Task thread interrupted while populating HistoryTab.", E, LocalDateTime.now());
                }
            });
            historyLoader.setOnFailed(e -> Services.loggingService.logWarning(
                    "Failed to load history.", (Exception) historyLoader.getException(), LocalDateTime.now()));
            new Thread(historyLoader).start();
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
                historyTextField.requestFocus();
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
        if (historyPane.isVisible()) {
            historyPane = (VBox) splitPane.getItems().remove(0);
        } else {
            splitPane.getItems().add(0, historyPane);
        }

        historyPane.setVisible(!historyPane.isVisible());
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
        HistoryItemController controller = appendToList(state, historyTab, true);
        historyItemControllers.add(controller);
    }

    private void addSearchItem(DashboardState state) {
        appendToList(state, searchBox, false);
    }

    private HistoryItemController appendToList(DashboardState state, VBox layer, boolean appendToStart) {
        historyPromptLayer.setVisible(false);
        HistoryItemController controller = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HistoryItem.fxml"));
            Parent historyItem = loader.load();

            controller = loader.getController();
            controller.setState(state);

            if (appendToStart)
                layer.getChildren().add(0, historyItem);
            else
                layer.getChildren().add(historyItem);

            // Clicking on HistoryItem opens it up in a new tab
            historyItem.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY)
                    addTab(state);
            });
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not append HistoryItem to list.", e, LocalDateTime.now());
        }
        return controller;
    }

    private List<HistoryItemController> getSearchResults(String searchString) {
        List<HistoryItemController> filteredList = new ArrayList<>();

        for (HistoryItemController controller : historyItemControllers) {

            int relativityIndex = controller.getRelativityIndex(searchString);

            // Split the string into words and get total relativity index as sum of individual indices.
            String words[] = searchString.split("\\s");
            for (String word : words)
                relativityIndex += controller.getRelativityIndex(word);

            if (relativityIndex != 0)
                filteredList.add(controller);
        }

        return filteredList;
    }
}

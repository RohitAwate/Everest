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

import com.rohitawate.restaurant.models.DashboardState;
import com.rohitawate.restaurant.util.Services;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class HomeWindowController implements Initializable {
    @FXML
    private TabPane homeWindowTabPane;
    @FXML
    private VBox historyTab;
    @FXML
    private StackPane historyPromptLayer;

    private KeyCombination newTab = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
    private List<DashboardController> dashboardControllers;
    private List<HistoryItemController> historyItemControllers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardControllers = new ArrayList<>();
        historyItemControllers = new ArrayList<>();
        recoverState();
        Platform.runLater(() -> {
            // Adds a new tab if the last tab is closed
            Scene thisScene = homeWindowTabPane.getScene();
            thisScene.setOnKeyPressed(e -> {
                if (newTab.match(e))
                    addTab();
            });

            // Saves the state of the application before closing
            Stage thisStage = (Stage) thisScene.getWindow();
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
                    E.printStackTrace();
                }
            });
            historyLoader.setOnFailed(e -> historyLoader.getException().printStackTrace());
            new Thread(historyLoader).start();
        });
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
            newTab.textProperty().bind(Bindings
                    .when(controller.getAddressProperty().isNotEmpty())
                    .then(controller.getAddressProperty())
                    .otherwise("New Tab"));
            newTab.setOnCloseRequest(e -> {
                if (homeWindowTabPane.getTabs().size() == 1)
                    addTab();
                dashboardControllers.remove(controller);
            });
            homeWindowTabPane.getTabs().add(newTab);
            homeWindowTabPane.getSelectionModel().select(newTab);
            dashboardControllers.add(controller);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveState() {
        List<DashboardState> dashboardStates = new ArrayList<>();

        // Get the states of all the tabs
        for (DashboardController controller : dashboardControllers)
            dashboardStates.add(controller.getState());

        try {
            File configFolder = new File("config/");
            if (!configFolder.exists())
                configFolder.mkdir();

            OutputStream fileStream = new FileOutputStream("config/restaurant.state");
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(dashboardStates);
            objectStream.close();
            fileStream.close();
            System.out.println("Application state saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save the application's state:");
            e.printStackTrace();
        }
    }

    private void recoverState() {
        try {
            InputStream fileStream = new FileInputStream("config/restaurant.state");
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);

            System.out.println("Application state file found. Recovering state... ");

            List<DashboardState> dashboardStates = (List<DashboardState>) objectStream.readObject();
            objectStream.close();
            fileStream.close();

            if (dashboardStates.size() > 0) {
                for (DashboardState dashboardState : dashboardStates)
                    addTab(dashboardState);
            } else {
                addTab();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Application state file not found. Loading default state... ");
            addTab();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Application state file is possibly corrupted. Could not recover the state.\nLoading default state... ");
            addTab();
        } finally {
            System.out.println("Application loaded.");
        }
    }

    public void addHistoryItem(DashboardState state) {
        historyPromptLayer.setVisible(false);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HistoryItem.fxml"));
            Parent historyItem = loader.load();
            HistoryItemController controller = loader.getController();

            controller.setRequestType(state.getHttpMethod());

            controller.setAddress(state.getTarget().toString());

            historyTab.getChildren().add(0, historyItem);
            historyItemControllers.add(controller);

            // Clicking on HistoryItem opens it up in a new tab
            historyItem.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY)
                    addTab(state);
            });
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
    }
}

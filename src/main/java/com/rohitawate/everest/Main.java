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
package com.rohitawate.everest;

import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
import com.rohitawate.everest.settings.SettingsLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Services.start();
        Services.startServicesThread.join();

        SettingsLoader settingsLoader = new SettingsLoader();
        settingsLoader.settingsLoaderThread.join();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HomeWindow.fxml"));
        Parent homeWindow = loader.load();
        Services.homeWindowController = loader.getController();
        Stage dashboardStage = new Stage();
        ThemeManager.setTheme(homeWindow);

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        dashboardStage.setWidth(screenBounds.getWidth() * 0.83);
        dashboardStage.setHeight(screenBounds.getHeight() * 0.74);

        dashboardStage.getIcons().add(new Image(getClass().getResource("/assets/Logo.png").toExternalForm()));
        dashboardStage.setScene(new Scene(homeWindow));
        dashboardStage.setTitle("Everest");
        dashboardStage.show();

        EverestUtilities.createBugReporter();
    }

    public static void main(String args[]) {
        launch(args);
    }
}

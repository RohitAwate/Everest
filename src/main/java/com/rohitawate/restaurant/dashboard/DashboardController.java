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
package com.rohitawate.restaurant.dashboard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.restaurant.models.requests.GETRequest;
import com.rohitawate.restaurant.models.responses.RestaurantResponse;
import com.rohitawate.restaurant.requestsmanager.GETRequestManager;
import com.rohitawate.restaurant.requestsmanager.RequestManager;
import com.rohitawate.restaurant.settings.Settings;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private BorderPane dashboard;
    @FXML
    private TextField addressField;
    @FXML
    private ComboBox<String> httpMethodBox;
    @FXML
    private VBox responseBox, loadingLayer;
    @FXML
    private HBox responseDetails;
    @FXML
    private TextArea responseArea;
    @FXML
    private Label statusCode, statusCodeDescription, responseTime, responseSize;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private Tab authTab, headersTab, bodyTab;

    private JFXSnackbar snackBar;
    private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
    private RequestManager requestManager;
    private HeaderTabController headerTabController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applySettings();
        Task<Parent> parentLoader = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/HeaderTab.fxml"));
                Parent parent = loader.load();
                headerTabController = loader.getController();
                return parent;
            }
        };

        parentLoader.setOnSucceeded(event -> headersTab.setContent(parentLoader.getValue()));
        parentLoader.setOnFailed(event -> parentLoader.getException().printStackTrace());
        new Thread(parentLoader).start();

        addressField.setText("https://api.chucknorris.io/jokes/random");
        responseBox.getChildren().remove(0);
        httpMethodBox.getItems().addAll(httpMethods);
        httpMethodBox.setValue("GET");

        snackBar = new JFXSnackbar(dashboard);
        bodyTab.disableProperty().bind(Bindings.and(httpMethodBox.valueProperty().isNotEqualTo("POST"),
                httpMethodBox.valueProperty().isNotEqualTo("PUT")));
    }

    @FXML
    private void sendAction() {
        try {
            String address = addressField.getText();
            if (address.equals("")) {
                snackBar.show("Please enter an address.", 3000);
                return;
            }
            switch (httpMethodBox.getValue()) {
                case "GET":
                    /*
                        Creates a new instance if its the first request of that session or
                        the HTTP method type was changed. Also checks if a request is already being processed.
                     */
                    if (requestManager == null || requestManager.getClass() != GETRequestManager.class)
                        requestManager = new GETRequestManager();
                    else if (requestManager.isRunning()) {
                        snackBar.show("Please wait while the current request is processed.", 3000);
                        return;
                    }

                    GETRequest getRequest = new GETRequest(addressField.getText());
                    getRequest.addHeaders(headerTabController.getHeaders());
                    requestManager.setRequest(getRequest);
                    cancelButton.setOnAction(e -> requestManager.cancel());
                    requestManager.setOnRunning(e -> {
                        responseArea.clear();
                        loadingLayer.setVisible(true);
                    });
                    requestManager.setOnSucceeded(e -> {
                        updateDashboard(requestManager.getValue());
                        loadingLayer.setVisible(false);
                        requestManager.reset();
                    });
                    requestManager.setOnCancelled(e -> {
                        loadingLayer.setVisible(false);
                        snackBar.show("Request canceled.", 2000);
                        requestManager.reset();
                    });
                    requestManager.start();
                    break;
                default:
                    loadingLayer.setVisible(false);
            }
        } catch (MalformedURLException ex) {
            snackBar.show("Invalid address. Please verify and try again.", 3000);
        }
    }

    private void updateDashboard(RestaurantResponse response) {
        responseArea.setText(response.getBody());

        if (responseBox.getChildren().size() != 2)
            responseBox.getChildren().add(0, responseDetails);

        statusCode.setText(Integer.toString(response.getStatusCode()));
        statusCodeDescription.setText(Response.Status.fromStatusCode(response.getStatusCode()).getReasonPhrase());
        responseTime.setText(Long.toString(response.getTime()) + " ms");
        responseSize.setText(Integer.toString(response.getSize()) + " B");
    }

    private void applySettings() {
        String responseAreaCSS = "-fx-font-family: " + Settings.responseAreaFont + ";" +
                "-fx-font-size: " + Settings.responseAreaFontSize;
        responseArea.setStyle(responseAreaCSS);
    }
}

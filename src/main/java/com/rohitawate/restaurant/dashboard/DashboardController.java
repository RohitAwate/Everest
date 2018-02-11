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
import com.rohitawate.restaurant.models.requests.DataDispatchRequest;
import com.rohitawate.restaurant.models.requests.GETRequest;
import com.rohitawate.restaurant.models.responses.RestaurantResponse;
import com.rohitawate.restaurant.requestsmanager.DataDispatchRequestManager;
import com.rohitawate.restaurant.requestsmanager.GETRequestManager;
import com.rohitawate.restaurant.requestsmanager.RequestManager;
import com.rohitawate.restaurant.util.Settings;
import com.rohitawate.restaurant.util.ThemeManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private BorderPane dashboard;
    @FXML
    private TextField addressField;
    @FXML
    private ComboBox<String> httpMethodBox;
    @FXML
    private VBox responseBox, loadingLayer, promptLayer, errorLayer, paramsBox;
    @FXML
    private HBox responseDetails;
    @FXML
    private TextArea responseArea;
    @FXML
    private Label statusCode, statusCodeDescription, responseTime, responseSize, errorTitle, errorDetails;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private TabPane requestOptionsTab;
    @FXML
    private Tab paramsTab, authTab, headersTab, bodyTab;

    private JFXSnackbar snackBar;
    private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
    private List<StringKeyValueFieldController> paramsControllers;
    private List<String> appendedParams;
    private RequestManager requestManager;
    private HeaderTabController headerTabController;
    private BodyTabController bodyTabController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applySettings();

        try {
            // Loading the headers tab
            FXMLLoader headerTabLoader = new FXMLLoader(getClass().getResource("/fxml/dashboard/HeaderTab.fxml"));
            Parent headerTabContent = headerTabLoader.load();
            ThemeManager.setTheme(headerTabContent);
            headerTabController = headerTabLoader.getController();
            headersTab.setContent(headerTabContent);

            // Loading the body tab
            FXMLLoader bodyTabLoader = new FXMLLoader(getClass().getResource("/fxml/dashboard/BodyTab.fxml"));
            Parent bodyTabContent = bodyTabLoader.load();
            ThemeManager.setTheme(bodyTabContent);
            bodyTabController = bodyTabLoader.getController();
            bodyTab.setContent(bodyTabContent);
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }

        addressField.setText("https://anapioficeandfire.com/api/characters/583");
        responseBox.getChildren().remove(0);
        promptLayer.setVisible(true);
        httpMethodBox.getItems().addAll(httpMethods);

        // Selects GET by default
        httpMethodBox.getSelectionModel().select(0);

        paramsControllers = new ArrayList<>();
        appendedParams = new ArrayList<>();
        addParam();

        snackBar = new JFXSnackbar(dashboard);
        bodyTab.disableProperty().bind(Bindings.and(httpMethodBox.valueProperty().isNotEqualTo("POST"),
                httpMethodBox.valueProperty().isNotEqualTo("PUT")));
    }

    @FXML
    private void sendAction() {
        promptLayer.setVisible(false);
        if (responseBox.getChildren().size() == 2) {
            responseBox.getChildren().remove(0);
            responseArea.clear();
        }
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
                        errorLayer.setVisible(false);
                        loadingLayer.setVisible(true);
                    });
                    requestManager.setOnSucceeded(e -> {
                        updateDashboard(requestManager.getValue());
                        errorLayer.setVisible(false);
                        loadingLayer.setVisible(false);
                        requestManager.reset();
                    });
                    requestManager.setOnCancelled(e -> {
                        loadingLayer.setVisible(false);
                        promptLayer.setVisible(true);
                        snackBar.show("Request canceled.", 2000);
                        requestManager.reset();
                    });
                    requestManager.setOnFailed(e -> {
                        loadingLayer.setVisible(false);
                        errorLayer.setVisible(true);
                        Throwable exception = requestManager.getException().getCause();

                        if (exception.getClass() == UnknownHostException.class) {
                            errorTitle.setText("No Internet Connection");
                            errorDetails.setText("Could not connect to the server. Please check your connection.");
                        }
                        requestManager.reset();
                    });
                    requestManager.start();
                    break;
                // DataDispatchRequestManager will generate appropriate request based on the type.
                case "POST":
                case "PUT":
                    if (requestManager == null || requestManager.getClass() != DataDispatchRequestManager.class)
                        requestManager = new DataDispatchRequestManager();
                    else if (requestManager.isRunning()) {
                        snackBar.show("Please wait while the current request is processed.", 3000);
                        return;
                    }

                    DataDispatchRequest dataDispatchRequest =
                            (DataDispatchRequest) bodyTabController.getBasicRequest(httpMethodBox.getValue());
                    dataDispatchRequest.setTarget(addressField.getText());
                    dataDispatchRequest.addHeaders(headerTabController.getHeaders());

                    requestManager.setRequest(dataDispatchRequest);
                    cancelButton.setOnAction(e -> requestManager.cancel());
                    requestManager.setOnRunning(e -> {
                        responseArea.clear();
                        errorLayer.setVisible(false);
                        loadingLayer.setVisible(true);
                    });
                    requestManager.setOnSucceeded(e -> {
                        updateDashboard(requestManager.getValue());
                        errorLayer.setVisible(false);
                        loadingLayer.setVisible(false);
                        requestManager.reset();
                    });
                    requestManager.setOnCancelled(e -> {
                        loadingLayer.setVisible(false);
                        promptLayer.setVisible(true);
                        snackBar.show("Request canceled.", 2000);
                        requestManager.reset();
                    });
                    requestManager.setOnFailed(e -> {
                        loadingLayer.setVisible(false);
                        promptLayer.setVisible(true);
                        if (requestManager.getException().getClass() == ConnectException.class)
                            snackBar.show("Request timed out. Server is unavailable or didn't respond.", 10000);
                        else if (requestManager.getException().getClass() == FileNotFoundException.class)
                            snackBar.show("File could not be found.", 5000);
                        requestManager.reset();
                    });
                    requestManager.start();
                    break;
                default:
                    loadingLayer.setVisible(false);
            }
        } catch (MalformedURLException MURLE) {
            snackBar.show("Invalid address. Please verify and try again.", 3000);
        } catch (Exception E) {
            E.printStackTrace();
            errorLayer.setVisible(true);
            errorTitle.setText("Oops... That's embarrassing!");
            errorDetails.setText("Something went wrong. Try to make another request.\nRestart RESTaurant if that doesn't work.");
        }
    }

    private void updateDashboard(RestaurantResponse response) {
        responseArea.setText(response.getBody());
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

    @FXML
    private void clearResponseArea() {
        responseBox.getChildren().remove(0);
        responseArea.clear();
        promptLayer.setVisible(true);
    }

    @FXML
    private void appendParams() {
        String pair, key, value;
        for (StringKeyValueFieldController controller : paramsControllers) {
            if (controller.isChecked()) {
                key = controller.getHeader().getKey();
                value = controller.getHeader().getValue();
                pair = key + value;
                if (!appendedParams.contains(pair)) {
                    addressField.appendText("?" + key + "=" + value + "&");
                    appendedParams.add(pair);
                }
            }
        }
    }

    @FXML
    private void addParam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard/StringKeyValueField.fxml"));
            Parent headerField = loader.load();
            StringKeyValueFieldController controller = loader.getController();
            paramsControllers.add(controller);
            paramsBox.getChildren().add(headerField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

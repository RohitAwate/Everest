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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.restaurant.exceptions.UnreliableResponseException;
import com.rohitawate.restaurant.models.DashboardState;
import com.rohitawate.restaurant.models.requests.DELETERequest;
import com.rohitawate.restaurant.models.requests.DataDispatchRequest;
import com.rohitawate.restaurant.models.requests.GETRequest;
import com.rohitawate.restaurant.models.responses.RestaurantResponse;
import com.rohitawate.restaurant.requestsmanager.DELETERequestManager;
import com.rohitawate.restaurant.requestsmanager.DataDispatchRequestManager;
import com.rohitawate.restaurant.requestsmanager.GETRequestManager;
import com.rohitawate.restaurant.requestsmanager.RequestManager;
import com.rohitawate.restaurant.util.Services;
import com.rohitawate.restaurant.util.settings.Settings;
import com.rohitawate.restaurant.util.themes.ThemeManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;

public class DashboardController implements Initializable {
    @FXML
    private StackPane dashboard;
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
    private JFXButton sendButton, cancelButton;
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
    private IntegerProperty paramsCountProperty;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyDashboardSettings();

        try {
            // Loading the headers tab
            FXMLLoader headerTabLoader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HeaderTab.fxml"));
            Parent headerTabContent = headerTabLoader.load();
            ThemeManager.setTheme(headerTabContent);
            headerTabController = headerTabLoader.getController();
            headersTab.setContent(headerTabContent);

            // Loading the body tab
            FXMLLoader bodyTabLoader = new FXMLLoader(getClass().getResource("/fxml/homewindow/BodyTab.fxml"));
            Parent bodyTabContent = bodyTabLoader.load();
            ThemeManager.setTheme(bodyTabContent);
            bodyTabController = bodyTabLoader.getController();
            bodyTab.setContent(bodyTabContent);
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not load headers/body tabs.", e, LocalDateTime.now());
        }

        // Select GET by default
        httpMethodBox.getSelectionModel().select("GET");

        responseBox.getChildren().remove(0);
        promptLayer.setVisible(true);
        httpMethodBox.getItems().addAll(httpMethods);

        paramsControllers = new ArrayList<>();
        paramsCountProperty = new SimpleIntegerProperty(paramsControllers.size());

        appendedParams = new ArrayList<>();
        addParamField(); // Adds a blank param field

        snackBar = new JFXSnackbar(dashboard);
        bodyTab.disableProperty().bind(Bindings.and(httpMethodBox.valueProperty().isNotEqualTo("POST"),
                httpMethodBox.valueProperty().isNotEqualTo("PUT")));

        errorTitle.setText("Oops... That's embarrassing!");
        errorDetails.setText("Something went wrong. Try to make another request.\nRestart RESTaurant if that doesn't work.");
    }

    @FXML
    private void sendRequest() {
        promptLayer.setVisible(false);
        if (responseBox.getChildren().size() == 2) {
            responseBox.getChildren().remove(0);
            responseArea.clear();
        }
        try {
            String address = addressField.getText();
            if (address.equals("")) {
                promptLayer.setVisible(true);
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
                    getRequest.setHeaders(headerTabController.getHeaders());
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
                        promptLayer.setVisible(false);
                        Throwable exception = requestManager.getException();
                        exception.printStackTrace();

                        if (exception.getClass() == UnreliableResponseException.class) {
                            UnreliableResponseException URE = (UnreliableResponseException) exception;
                            errorTitle.setText(URE.getExceptionTitle());
                            errorDetails.setText(URE.getExceptionDetails());
                        } else if (exception.getClass() == ProcessingException.class) {
                            errorTitle.setText("RESTaurant couldn't connect.");
                            errorDetails.setText("Either you are not connected to the Internet or the server is offline.");
                        }
                        errorLayer.setVisible(true);
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
                            bodyTabController.getBasicRequest(httpMethodBox.getValue());
                    dataDispatchRequest.setTarget(addressField.getText());
                    dataDispatchRequest.setHeaders(headerTabController.getHeaders());

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
                        promptLayer.setVisible(false);
                        Throwable exception = requestManager.getException();
                        exception.printStackTrace();

                        if (exception.getClass() == UnreliableResponseException.class) {
                            UnreliableResponseException URE = (UnreliableResponseException) exception;
                            errorTitle.setText(URE.getExceptionTitle());
                            errorDetails.setText(URE.getExceptionDetails());
                        } else if (exception.getClass() == ProcessingException.class) {
                            if (exception.getCause().getClass() == UnknownHostException.class ||
                                    exception.getCause().getClass() == ConnectException.class) {
                                errorTitle.setText("RESTaurant couldn't connect.");
                                errorDetails.setText("Either you are not connected to the Internet or the server is offline.");
                            } else if (exception.getCause().getClass() == IllegalArgumentException.class) {
                                errorTitle.setText("Did you forget something?");
                                errorDetails.setText("Please specify at least one body part for your " + httpMethodBox.getValue() + " request.");
                            }
                        } else if (exception.getClass() == FileNotFoundException.class)
                            snackBar.show("File could not be found.", 5000);
                        errorLayer.setVisible(true);
                        requestManager.reset();
                    });
                    requestManager.start();
                    break;
                case "DELETE":
                    if (requestManager == null || requestManager.getClass() != DELETERequestManager.class)
                        requestManager = new DELETERequestManager();
                    else if (requestManager.isRunning()) {
                        snackBar.show("Please wait while the current request is processed.", 3000);
                        return;
                    }

                    DELETERequest deleteRequest = new DELETERequest(addressField.getText());
                    deleteRequest.setHeaders(headerTabController.getHeaders());
                    requestManager.setRequest(deleteRequest);
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
                        promptLayer.setVisible(false);
                        Throwable exception = requestManager.getException();
                        exception.printStackTrace();

                        if (exception.getClass() == UnreliableResponseException.class) {
                            UnreliableResponseException URE = (UnreliableResponseException) exception;
                            errorTitle.setText(URE.getExceptionTitle());
                            errorDetails.setText(URE.getExceptionDetails());
                        } else if (exception.getClass() == ProcessingException.class) {
                            errorTitle.setText("No Internet Connection");
                            errorDetails.setText("Could not connect to the server. Please check your connection.");
                        }
                        errorLayer.setVisible(true);
                        requestManager.reset();
                    });
                    requestManager.start();
                    break;
                default:
                    loadingLayer.setVisible(false);
            }
            Services.historyManager.saveHistory(getState());
        } catch (MalformedURLException MURLE) {
            promptLayer.setVisible(true);
            snackBar.show("Invalid address. Please verify and try again.", 3000);
        } catch (Exception E) {
            Services.loggingService.logSevere("Request execution failed.", E, LocalDateTime.now());
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

    private void applyDashboardSettings() {
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

    private HashMap<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();

        for (StringKeyValueFieldController controller : paramsControllers)
            if (controller.isChecked())
                params.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        return params;
    }

    private void addParamField() {
        addParamField("", "", null);
    }

    private void addParamField(String key, String value) {
        addParamField(key, value, null);
    }

    @FXML
    private void addParamField(ActionEvent event) {
        addParamField("", "", event);
    }

    // Adds a new URL-parameter field
    private void addParamField(String key, String value, ActionEvent event) {
        /*
            Re-uses previous field if it is empty,
            else loads a new one.
         */
        if (paramsControllers.size() > 0 && event == null) {
            StringKeyValueFieldController previousController = paramsControllers.get(paramsControllers.size() - 1);

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
            StringKeyValueFieldController controller = loader.getController();
            controller.setKeyField(key);
            controller.setValueField(value);
            paramsControllers.add(controller);
            paramsCountProperty.set(paramsCountProperty.get() + 1);
            controller.deleteButton.visibleProperty().bind(Bindings.greaterThan(paramsCountProperty, 1));
            controller.deleteButton.setOnAction(e -> {
                paramsBox.getChildren().remove(headerField);
                paramsControllers.remove(controller);
                paramsCountProperty.set(paramsCountProperty.get() - 1);
            });
            paramsBox.getChildren().add(headerField);
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not append params field.", e, LocalDateTime.now());
        }
    }

    public StringProperty getAddressProperty() {
        return addressField.textProperty();
    }

    /**
     * Returns the current state of the Dashboard
     *
     * @return DashboardState - Current state of the Dashboard
     */
    public DashboardState getState() {
        DashboardState dashboardState;
        switch (httpMethodBox.getValue()) {
            case "POST":
            case "PUT":
                dashboardState = new DashboardState(bodyTabController.getBasicRequest(httpMethodBox.getValue()));
                dashboardState.setHeaders(headerTabController.getHeaders());
                break;
            default:
                // For GET, DELETE requests
                dashboardState = new DashboardState();
        }

        try {
            dashboardState.setTarget(addressField.getText());
        } catch (MalformedURLException e) {
            Services.loggingService.logInfo("Dashboard state was saved with an invalid URL.", LocalDateTime.now());
        }
        dashboardState.setHttpMethod(httpMethodBox.getValue());
        dashboardState.setHeaders(headerTabController.getHeaders());
        dashboardState.setParams(getParams());

        return dashboardState;
    }

    /**
     * Sets the Dashboard to the given application state.
     *
     * @param dashboardState - State of the dashboard
     */
    public void setState(DashboardState dashboardState) {
        if (dashboardState.getTarget() != null)
            addressField.setText(dashboardState.getTarget().toString());

        httpMethodBox.getSelectionModel().select(dashboardState.getHttpMethod());

        if (dashboardState.getHeaders() != null)
            for (Map.Entry entry : dashboardState.getHeaders().entrySet())
                headerTabController.addHeader(entry.getKey().toString(), entry.getValue().toString());

        if (dashboardState.getParams() != null)
            for (Map.Entry entry : dashboardState.getParams().entrySet())
                addParamField(entry.getKey().toString(), entry.getValue().toString());

        if (httpMethodBox.getValue().equals("POST") || httpMethodBox.getValue().equals("PUT"))
            bodyTabController.setState(dashboardState);
    }
}

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

import com.fasterxml.jackson.databind.JsonNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea.HighlightMode;
import com.rohitawate.everest.exceptions.RedirectException;
import com.rohitawate.everest.exceptions.UnreliableResponseException;
import com.rohitawate.everest.models.DashboardState;
import com.rohitawate.everest.models.requests.DELETERequest;
import com.rohitawate.everest.models.requests.DataDispatchRequest;
import com.rohitawate.everest.models.requests.GETRequest;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.requestmanager.DataDispatchRequestManager;
import com.rohitawate.everest.requestmanager.RequestManager;
import com.rohitawate.everest.util.EverestUtilities;
import com.rohitawate.everest.util.Services;
import com.rohitawate.everest.util.themes.ThemeManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private StackPane dashboard;
    @FXML
    TextField addressField;
    @FXML
    ComboBox<String> httpMethodBox, responseTypeBox;
    @FXML
    private VBox responseBox, loadingLayer, promptLayer, errorLayer, paramsBox;
    @FXML
    private HBox responseDetails;
    @FXML
    private Label statusCode, statusCodeDescription, responseTime,
            responseSize, errorTitle, errorDetails;
    @FXML
    private JFXButton cancelButton, copyBodyButton;
    @FXML
    TabPane requestOptionsTab;
    @FXML
    Tab paramsTab, authTab, headersTab, bodyTab;
    @FXML
    private Tab responseBodyTab, visualizerTab, responseHeadersTab;
    @FXML
    private JFXProgressBar progressBar;

    private JFXSnackbar snackbar;
    private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
    private List<StringKeyValueFieldController> paramsControllers;
    private List<String> appendedParams;
    private RequestManager requestManager;
    private HeaderTabController headerTabController;
    private BodyTabController bodyTabController;
    private IntegerProperty paramsCountProperty;
    private Visualizer visualizer;
    private ResponseHeadersViewer responseHeadersViewer;

    private GETRequest getRequest;
    private DataDispatchRequest dataRequest;
    private DELETERequest deleteRequest;
    private HashMap<String, String> params;
    private EverestCodeArea responseArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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

        snackbar = new JFXSnackbar(dashboard);

        responseBox.getChildren().remove(0);
        promptLayer.setVisible(true);
        httpMethodBox.getItems().addAll(httpMethods);

        // Select GET by default
        httpMethodBox.getSelectionModel().select("GET");

        paramsControllers = new ArrayList<>();
        paramsCountProperty = new SimpleIntegerProperty(paramsControllers.size());

        appendedParams = new ArrayList<>();
        addParamField(); // Adds a blank param field

        bodyTab.disableProperty().bind(
                Bindings.or(httpMethodBox.valueProperty().isEqualTo("GET"),
                        httpMethodBox.valueProperty().isEqualTo("DELETE")));

        // Disabling Ctrl+Tab navigation
        requestOptionsTab.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.TAB) {
                e.consume();
            }
        });

        copyBodyButton.setOnAction(e -> {
            responseArea.selectAll();
            responseArea.copy();
            responseArea.deselect();
            snackbar.show("Response body copied to clipboard.", 5000);
        });

        responseTypeBox.getItems().addAll("JSON", "XML", "HTML", "PLAIN TEXT");

        responseTypeBox.valueProperty().addListener(change -> {
            String type = responseTypeBox.getValue();
            try {
                switch (type) {
                    case "JSON":
                        JsonNode node = EverestUtilities.jsonMapper.readTree(responseArea.getText());
                        responseArea.setText(EverestUtilities.jsonMapper.writeValueAsString(node), HighlightMode.JSON);
                        break;
                    case "XML":
                        responseArea.setText(responseArea.getText(), HighlightMode.XML);
                        break;
                    case "HTML":
                        responseArea.setText(responseArea.getText(), HighlightMode.HTML);
                        break;
                    default:
                        responseArea.setText(responseArea.getText(), HighlightMode.PLAIN);
                }
            } catch (IOException e) {
                Services.loggingService.logWarning("Response could not be parsed.", e, LocalDateTime.now());
            }
        });

        errorTitle.setText("Oops... That's embarrassing!");
        errorDetails.setText("Something went wrong. Try to make another request.\nRestart Everest if that doesn't work.");

        visualizer = new Visualizer();
        visualizerTab.setContent(visualizer);

        responseArea = new EverestCodeArea();
        responseArea.setEditable(false);
        ThemeManager.setSyntaxTheme(responseArea);
        responseBodyTab.setContent(new VirtualizedScrollPane<>(responseArea));

        responseHeadersViewer = new ResponseHeadersViewer();
        responseHeadersTab.setContent(responseHeadersViewer);
    }

    @FXML
    void sendRequest() {
        if (requestManager != null && requestManager.isRunning()) {
            snackbar.show("Please wait while the current request is processed.", 5000);
            return;
        }

        promptLayer.setVisible(false);
        if (responseBox.getChildren().size() == 2) {
            responseBox.getChildren().remove(0);
            responseArea.clear();
        }

        try {
            String address = addressField.getText();

            if (address.equals("")) {
                promptLayer.setVisible(true);
                snackbar.show("Please enter an address.", 3000);
                return;
            }

            // Prepends "https://" to the address if not already done.
            if (!(address.startsWith("https://") || address.startsWith("http://"))) {
                address = "https://" + address;
                addressField.setText(address);
                responseArea.requestFocus();
            }

            switch (httpMethodBox.getValue()) {
                case "GET":
                    if (getRequest == null)
                        getRequest = new GETRequest();

                    getRequest.setTarget(address);
                    getRequest.setHeaders(headerTabController.getSelectedHeaders());

                    requestManager = Services.pool.get();
                    requestManager.setRequest(getRequest);

                    cancelButton.setOnAction(e -> requestManager.cancel());
                    configureRequestManager();
                    requestManager.start();
                    break;
                case "POST":
                case "PUT":
                case "PATCH":
                    if (dataRequest == null)
                        dataRequest = new DataDispatchRequest();

                    dataRequest.setRequestType(httpMethodBox.getValue());
                    dataRequest.setTarget(address);
                    dataRequest.setHeaders(headerTabController.getSelectedHeaders());

                    if (bodyTabController.rawTab.isSelected()) {
                        String contentType;
                        switch (bodyTabController.rawInputTypeBox.getValue()) {
                            case "PLAIN TEXT":
                                contentType = MediaType.TEXT_PLAIN;
                                break;
                            case "JSON":
                                contentType = MediaType.APPLICATION_JSON;
                                break;
                            case "XML":
                                contentType = MediaType.APPLICATION_XML;
                                break;
                            case "HTML":
                                contentType = MediaType.TEXT_HTML;
                                break;
                            default:
                                contentType = MediaType.TEXT_PLAIN;
                        }
                        dataRequest.setContentType(contentType);
                        dataRequest.setBody(bodyTabController.rawInputArea.getText());
                    } else if (bodyTabController.formTab.isSelected()) {
                        dataRequest.setStringTuples(bodyTabController.formDataTabController.getStringTuples());
                        dataRequest.setFileTuples(bodyTabController.formDataTabController.getFileTuples());
                        dataRequest.setContentType(MediaType.MULTIPART_FORM_DATA);
                    } else if (bodyTabController.binaryTab.isSelected()) {
                        dataRequest.setBody(bodyTabController.filePathField.getText());
                        dataRequest.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    } else if (bodyTabController.urlTab.isSelected()) {
                        dataRequest.setStringTuples(bodyTabController.urlTabController.getStringTuples());
                        dataRequest.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    }

                    requestManager = Services.pool.data();
                    requestManager.setRequest(dataRequest);

                    cancelButton.setOnAction(e -> requestManager.cancel());
                    configureRequestManager();
                    requestManager.start();
                    break;
                case "DELETE":
                    if (deleteRequest == null)
                        deleteRequest = new DELETERequest();

                    deleteRequest.setTarget(address);
                    deleteRequest.setHeaders(headerTabController.getSelectedHeaders());

                    requestManager = Services.pool.delete();
                    requestManager.setRequest(deleteRequest);

                    cancelButton.setOnAction(e -> requestManager.cancel());
                    configureRequestManager();
                    requestManager.start();
                    break;
                default:
                    loadingLayer.setVisible(false);
            }
            Services.historyManager.saveHistory(getState());
        } catch (MalformedURLException MURLE) {
            promptLayer.setVisible(true);
            snackbar.show("Invalid address. Please verify and try again.", 3000);
        } catch (Exception E) {
            Services.loggingService.logSevere("Request execution failed.", E, LocalDateTime.now());
            errorLayer.setVisible(true);
            errorTitle.setText("Oops... That's embarrassing!");
            errorDetails.setText("Something went wrong. Try to make another request.\nRestart Everest if that doesn't work.");
        }
    }

    private void configureRequestManager() {
        progressBar.progressProperty().bind(requestManager.progressProperty());
        requestManager.setOnRunning(e -> whileRunning());
        requestManager.setOnSucceeded(e -> onSucceeded());
        requestManager.setOnCancelled(e -> onCancelled());
        requestManager.setOnFailed(e -> onFailed());
    }

    private void onFailed() {
        loadingLayer.setVisible(false);
        promptLayer.setVisible(false);
        Throwable throwable = requestManager.getException();
        Exception exception = (Exception) throwable;
        Services.loggingService.logWarning(httpMethodBox.getValue() + " request could not be processed.", exception, LocalDateTime.now());

        if (throwable.getClass() == UnreliableResponseException.class) {
            UnreliableResponseException URE = (UnreliableResponseException) throwable;
            errorTitle.setText(URE.getExceptionTitle());
            errorDetails.setText(URE.getExceptionDetails());
        } else if (throwable.getClass() == ProcessingException.class) {
            errorTitle.setText("Everest couldn't connect.");
            errorDetails.setText("Either you are not connected to the Internet or the server is offline.");
        } else if (throwable.getClass() == RedirectException.class) {
            RedirectException redirect = (RedirectException) throwable;
            addressField.setText(redirect.getNewLocation());
            snackbar.show("Resource moved permanently. Redirecting...", 3000);
            requestManager = null;
            sendRequest();
            return;
        }

        if (requestManager.getClass() == DataDispatchRequestManager.class) {
            if (throwable.getCause() != null && throwable.getCause().getClass() == IllegalArgumentException.class) {
                errorTitle.setText("Did you forget something?");
                errorDetails.setText("Please specify at least one body part for your " + httpMethodBox.getValue() + " request.");
            } else if (throwable.getClass() == FileNotFoundException.class) {
                errorTitle.setText("File(s) not found:");
                errorDetails.setText(throwable.getMessage());
            }
        }

        errorLayer.setVisible(true);
        requestManager.reset();
    }

    private void onCancelled() {
        loadingLayer.setVisible(false);
        promptLayer.setVisible(true);
        snackbar.show("Request canceled.", 2000);
        requestManager.reset();
    }

    private void onSucceeded() {
        displayResponse(requestManager.getValue());
        errorLayer.setVisible(false);
        loadingLayer.setVisible(false);
        requestManager.reset();
    }

    private void whileRunning() {
        responseArea.clear();
        errorLayer.setVisible(false);
        loadingLayer.setVisible(true);
    }

    private void displayResponse(EverestResponse response) {
        prettifyResponseBody(response);
        responseBox.getChildren().add(0, responseDetails);
        statusCode.setText(Integer.toString(response.getStatusCode()));
        statusCodeDescription.setText(Response.Status.fromStatusCode(response.getStatusCode()).getReasonPhrase());
        responseTime.setText(Long.toString(response.getTime()) + " ms");
        responseSize.setText(Integer.toString(response.getSize()) + " B");
        responseHeadersViewer.populate(response);
    }

    private void prettifyResponseBody(EverestResponse response) {
        String type;

        if (response.getMediaType() != null)
            type = response.getMediaType().toString();
        else
            type = null;

        String responseBody = response.getBody();

        visualizerTab.setDisable(true);
        visualizer.clear();
        try {
            if (type != null) {
                // Selects only the part preceding the ';', skipping the character encoding
                type = type.split(";")[0];

                switch (type.toLowerCase()) {
                    case "application/json":
                        responseTypeBox.setValue("JSON");
                        JsonNode node = EverestUtilities.jsonMapper.readTree(responseBody);
                        responseArea.setText(EverestUtilities.jsonMapper.writeValueAsString(node), HighlightMode.JSON);
                        visualizerTab.setDisable(false);
                        visualizer.populate(node);
                        break;
                    case "application/xml":
                        responseTypeBox.setValue("XML");
                        responseArea.setText(responseBody, HighlightMode.XML);
                        break;
                    case "text/html":
                        responseTypeBox.setValue("HTML");
                        responseArea.setText(responseBody, HighlightMode.HTML);
                        break;
                    default:
                        responseTypeBox.setValue("PLAIN TEXT");
                        responseArea.setText(responseBody, HighlightMode.PLAIN);
                }
            } else {
                responseTypeBox.setValue("PLAIN");
                responseArea.setText("No body found in the response.", HighlightMode.PLAIN);
            }
        } catch (Exception e) {
            snackbar.show("Response could not be parsed.", 5000);
            Services.loggingService.logSevere("Response could not be parsed.", e, LocalDateTime.now());
            errorLayer.setVisible(true);
            errorTitle.setText("Parsing Error");
            errorDetails.setText("Everest could not parse the response.");
        }
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
        if (params == null)
            params = new HashMap<>();

        params.clear();
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
            case "PATCH":
                dashboardState = new DashboardState(bodyTabController.getBasicRequest(httpMethodBox.getValue()));
                dashboardState.setHeaders(headerTabController.getSelectedHeaders());
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
        dashboardState.setHeaders(headerTabController.getSelectedHeaders());
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
            for (Entry entry : dashboardState.getHeaders().entrySet())
                headerTabController.addHeader(entry.getKey().toString(), entry.getValue().toString());

        if (dashboardState.getParams() != null)
            for (Entry entry : dashboardState.getParams().entrySet())
                addParamField(entry.getKey().toString(), entry.getValue().toString());

        if (!(httpMethodBox.getValue().equals("GET") || httpMethodBox.getValue().equals("DELETE")))
            bodyTabController.setState(dashboardState);
    }
}

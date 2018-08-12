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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.highlighters.HighlighterFactory;
import com.rohitawate.everest.controllers.state.ComposerState;
import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.controllers.state.FieldState;
import com.rohitawate.everest.controllers.visualizers.TreeVisualizer;
import com.rohitawate.everest.controllers.visualizers.Visualizer;
import com.rohitawate.everest.exceptions.RedirectException;
import com.rohitawate.everest.exceptions.NullResponseException;
import com.rohitawate.everest.format.FormatterFactory;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
import com.rohitawate.everest.models.requests.DELETERequest;
import com.rohitawate.everest.models.requests.DataRequest;
import com.rohitawate.everest.models.requests.EverestRequest;
import com.rohitawate.everest.models.requests.GETRequest;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.requestmanager.RequestManager;
import com.rohitawate.everest.requestmanager.RequestManagersPool;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private VBox dashboard;
    @FXML
    TextField addressField;
    @FXML
    ComboBox<String> httpMethodBox, responseTypeBox;
    @FXML
    private VBox responseBox, responseLayer, loadingLayer, promptLayer, errorLayer, paramsBox;
    @FXML
    private Label statusCode, statusCodeDescription, responseTime,
            responseSize, errorTitle, errorDetails;
    @FXML
    private JFXButton sendButton, cancelButton, copyBodyButton;
    @FXML
    TabPane requestOptionsTab, responseTabPane;
    @FXML
    Tab paramsTab, authTab, headersTab, bodyTab;
    @FXML
    private Tab responseBodyTab, visualizerTab, responseHeadersTab;
    @FXML
    private JFXProgressBar progressBar;

    private JFXSnackbar snackbar;
    private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
    private List<StringKeyValueFieldController> paramsControllers;
    private RequestManager requestManager;
    private HeaderTabController headerTabController;
    private BodyTabController bodyTabController;
    private IntegerProperty paramsCountProperty;
    private Visualizer visualizer;
    private ResponseHeadersViewer responseHeadersViewer;

    private GETRequest getRequest;
    private DataRequest dataRequest;
    private DELETERequest deleteRequest;
    private HashMap<String, String> params;
    private EverestCodeArea responseArea;
    private ResponseLayer visibleLayer;

    public enum ResponseLayer {
        PROMPT, LOADING, RESPONSE, ERROR
    }

    public enum ResponseTab {
        BODY, VISUALIZER, HEADERS
    }

    public enum ComposerTab {
        PARAMS, AUTH, HEADERS, BODY
    }

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
            LoggingService.logSevere("Could not load headers/body tabs.", e, LocalDateTime.now());
        }

        snackbar = new JFXSnackbar(dashboard);

        showLayer(ResponseLayer.PROMPT);
        httpMethodBox.getItems().addAll(httpMethods);

        // Select GET by default
        httpMethodBox.getSelectionModel().select("GET");

        paramsControllers = new ArrayList<>();
        paramsCountProperty = new SimpleIntegerProperty(0);

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

            if (type.equals("JSON")) {
                try {
                    responseArea.setText(responseArea.getText(),
                            FormatterFactory.getHighlighter(type),
                            HighlighterFactory.getHighlighter(type));
                } catch (IOException e) {
                    LoggingService.logWarning("Response could not be parsed.", e, LocalDateTime.now());
                }

                return;
            }

            responseArea.setHighlighter(HighlighterFactory.getHighlighter(type));
        });

        visualizer = new TreeVisualizer();
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
        if (requestManager != null) {
            while (requestManager.isRunning())
                requestManager.cancel();
            requestManager.reset();
        }

        try {
            String address = addressField.getText().trim();

            if (address.equals("")) {
                showLayer(ResponseLayer.PROMPT);
                snackbar.show("Please enter an address.", 3000);
                return;
            }

            // Prepends "https://" to the address if not already done.
            if (!(address.startsWith("https://") || address.startsWith("http://"))) {
                address = "https://" + address;
                responseArea.requestFocus();
            }

            // Set again in case the address is manipulated by the above logic
            addressField.setText(address);

            switch (httpMethodBox.getValue()) {
                case "GET":
                    if (getRequest == null)
                        getRequest = new GETRequest();

                    getRequest.setTarget(address);
                    getRequest.setHeaders(headerTabController.getHeaders());

                    requestManager = RequestManagersPool.manager();
                    requestManager.setRequest(getRequest);
                    break;
                case "POST":
                case "PUT":
                case "PATCH":
                    if (dataRequest == null)
                        dataRequest = new DataRequest();

                    dataRequest.setRequestType(httpMethodBox.getValue());
                    dataRequest.setTarget(address);
                    dataRequest.setHeaders(headerTabController.getHeaders());

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

                    requestManager = RequestManagersPool.manager();
                    requestManager.setRequest(dataRequest);
                    break;
                case "DELETE":
                    if (deleteRequest == null)
                        deleteRequest = new DELETERequest();

                    deleteRequest.setTarget(address);
                    deleteRequest.setHeaders(headerTabController.getHeaders());

                    requestManager = RequestManagersPool.manager();
                    requestManager.setRequest(deleteRequest);
                    break;
                default:
                    showLayer(ResponseLayer.PROMPT);
            }
            cancelButton.setOnAction(e -> requestManager.cancel());
            requestManager.addHandlers(this::whileRunning, this::onSucceeded, this::onFailed, this::onCancelled);
            requestManager.start();
            Services.historyManager.saveHistory(getState().composer);
        } catch (MalformedURLException MURLE) {
            showLayer(ResponseLayer.PROMPT);
            snackbar.show("Invalid address. Please verify and try again.", 3000);
        } catch (Exception E) {
            LoggingService.logSevere("Request execution failed.", E, LocalDateTime.now());
            showLayer(ResponseLayer.ERROR);
            errorTitle.setText("Oops... That's embarrassing!");
            errorDetails.setText("Something went wrong. Try to make another request.\nRestart Everest if that doesn't work.");
        }
    }

    // TODO: Clean this method
    private void onFailed(Event event) {
        showLayer(ResponseLayer.ERROR);
        Throwable throwable = requestManager.getException();
        Exception exception = (Exception) throwable;
        LoggingService.logWarning(httpMethodBox.getValue() + " request could not be processed.", exception, LocalDateTime.now());

        if (throwable.getClass() == NullResponseException.class) {
            NullResponseException URE = (NullResponseException) throwable;
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

        if (requestManager.getRequest().getClass().equals(DataRequest.class)) {
            if (throwable.getCause() != null && throwable.getCause().getClass() == IllegalArgumentException.class) {
                errorTitle.setText("Did you forget something?");
                errorDetails.setText("Please specify a body for your " + httpMethodBox.getValue() + " request.");
            } else if (throwable.getClass() == FileNotFoundException.class) {
                errorTitle.setText("File(s) not found:");
                errorDetails.setText(throwable.getMessage());
            }
        }

        requestManager.reset();
    }

    private void onCancelled(Event event) {
        showLayer(ResponseLayer.PROMPT);
        requestManager.reset();
        addressField.requestFocus();
    }

    private void onSucceeded(Event event) {
        showLayer(ResponseLayer.RESPONSE);
        showResponse(requestManager.getValue());
        requestManager.reset();
    }

    private void whileRunning(Event event) {
        progressBar.requestLayout();
        progressBar.progressProperty().bind(requestManager.progressProperty());
        responseArea.clear();
        showLayer(ResponseLayer.LOADING);
    }

    private void showLayer(ResponseLayer layer) {
        this.visibleLayer = layer;

        switch (layer) {
            case ERROR:
                errorLayer.setVisible(true);
                loadingLayer.setVisible(false);
                promptLayer.setVisible(false);
                responseLayer.setVisible(false);
                break;
            case LOADING:
                loadingLayer.setVisible(true);
                errorLayer.setVisible(false);
                promptLayer.setVisible(false);
                responseLayer.setVisible(false);
                break;
            case RESPONSE:
                responseLayer.setVisible(true);
                errorLayer.setVisible(false);
                loadingLayer.setVisible(false);
                promptLayer.setVisible(false);
                break;
            case PROMPT:
            default:
                promptLayer.setVisible(true);
                loadingLayer.setVisible(false);
                errorLayer.setVisible(false);
                responseLayer.setVisible(false);
                break;
        }
    }

    private void showResponse(EverestResponse response) {
        if (response == null)
            return;

        prettifyResponseBody(response);
        statusCode.setText(Integer.toString(response.getStatusCode()));
        statusCodeDescription.setText(Response.Status.fromStatusCode(response.getStatusCode()).getReasonPhrase());
        responseTime.setText(Long.toString(response.getTime()) + " ms");
        responseSize.setText(Integer.toString(response.getSize()) + " B");
        responseHeadersViewer.populate(response);
    }

    private void showResponse(DashboardState state) {
        prettifyResponseBody(state.responseBody, state.responseType);
        statusCode.setText(Integer.toString(state.responseCode));
        statusCodeDescription.setText(Response.Status.fromStatusCode(state.responseCode).getReasonPhrase());
        responseTime.setText(Long.toString(state.responseTime) + " ms");
        responseSize.setText(Integer.toString(state.responseSize) + " B");
        responseHeadersViewer.populate(state.responseHeaders);

        if (state.visibleResponseTab != null) {
            int tab;
            switch (state.visibleResponseTab) {
                case VISUALIZER:
                    tab = 1;
                    break;
                case HEADERS:
                    tab = 2;
                    break;
                default:
                    tab = 0;
            }

            responseTabPane.getSelectionModel().select(tab);
        }
    }

    private void prettifyResponseBody(String body, String contentType) {
        showLayer(ResponseLayer.RESPONSE);
        visualizerTab.setDisable(true);

        try {
            String simplifiedContentType;
            if (contentType != null) {
                // Selects only the part preceding the ';', skipping the character encoding
                contentType = contentType.split(";")[0];

                switch (contentType.toLowerCase()) {
                    case "application/json":
                        simplifiedContentType = "JSON";
                        visualizerTab.setDisable(false);
                        visualizer.populate(body);
                        break;
                    case "application/xml":
                        simplifiedContentType = "XML";
                        break;
                    case "text/html":
                        simplifiedContentType = "HTML";
                        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            snackbar.show("Open link in browser?", "YES", 5000, e -> {
                                snackbar.close();
                                new Thread(() -> {
                                    try {
                                        Desktop.getDesktop().browse(new URI(addressField.getText()));
                                    } catch (Exception ex) {
                                        LoggingService.logWarning("Invalid URL encountered while opening in browser.", ex, LocalDateTime.now());
                                    }
                                }).start();
                            });
                        }
                        break;
                    default:
                        simplifiedContentType = "PLAIN TEXT";
                }
            } else {
                simplifiedContentType = "PLAIN TEXT";
            }

            responseArea.setText(body,
                    FormatterFactory.getHighlighter(simplifiedContentType),
                    HighlighterFactory.getHighlighter(simplifiedContentType));

            responseTypeBox.setValue(simplifiedContentType);
        } catch (Exception e) {
            String errorMessage = "Response could not be parsed.";
            snackbar.show(errorMessage, 5000);
            LoggingService.logSevere(errorMessage, e, LocalDateTime.now());
            errorTitle.setText("Parsing Error");
            errorDetails.setText(errorMessage);
            showLayer(ResponseLayer.ERROR);
        }
    }


    private void prettifyResponseBody(EverestResponse response) {
        String type;
        if (response.getMediaType() != null)
            type = response.getMediaType().toString();
        else
            type = "";

        String responseBody = response.getBody();

        prettifyResponseBody(responseBody, type);
    }

    @FXML
    private void clearResponseArea() {
        responseArea.clear();
        showLayer(ResponseLayer.PROMPT);
        addressField.requestFocus();
    }

    @FXML
    private void appendParams() {
        StringBuilder url = new StringBuilder();
        url.append(addressField.getText().split("\\?")[0]);

        boolean addedQuestionMark = false;
        String key, value;
        for (StringKeyValueFieldController controller : paramsControllers) {
            if (controller.isChecked()) {
                if (!addedQuestionMark) {
                    url.append("?");
                    addedQuestionMark = true;
                } else {
                    url.append("&");
                }

                key = controller.getHeader().getKey();
                value = controller.getHeader().getValue();
                url.append(key);
                url.append("=");
                url.append(value);
            }
        }

        addressField.clear();
        addressField.setText(EverestUtilities.encodeURL(url.toString()));
    }

    /**
     * @return List of the states of all the non-empty fields in the Params tab.
     */
    public ArrayList<FieldState> getParamFieldStates() {
        ArrayList<FieldState> states = new ArrayList<>();

        for (StringKeyValueFieldController controller : paramsControllers)
            states.add(controller.getState());

        return states;
    }

    private void addParamField() {
        addParamField("", "", null, false);
    }

    private void addParamField(FieldState state) {
        addParamField(state.key, state.value, null, state.checked);
    }

    @FXML
    private void addParamField(ActionEvent event) {
        addParamField("", "", event, false);
    }

    /**
     * Adds a new URL-parameter field
     */
    private void addParamField(String key, String value, ActionEvent event, boolean checked) {
        /*
            Re-uses previous field if it is empty, else loads a new one.
            A value of null for the 'event' parameter indicates that the method call
            came from code and not from the user. This call is made while recovering
            the application state.
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
            controller.setChecked(checked);
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
            LoggingService.logSevere("Could not append params field.", e, LocalDateTime.now());
        }
    }

    public ComposerTab getVisibleComposerTab() {
        int visibleTab = requestOptionsTab.getSelectionModel().getSelectedIndex();
        switch (visibleTab) {
            case 1:
                return ComposerTab.AUTH;
            case 2:
                return ComposerTab.HEADERS;
            case 3:
                return ComposerTab.BODY;
            default:
                return ComposerTab.PARAMS;
        }
    }


    private ResponseTab getVisibleResponseTab() {
        int visibleTab = responseTabPane.getSelectionModel().getSelectedIndex();
        switch (visibleTab) {
            case 1:
                return ResponseTab.VISUALIZER;
            case 2:
                return ResponseTab.HEADERS;
            default:
                return ResponseTab.BODY;
        }
    }

    /**
     * @return Current state of the Dashboard.
     */
    public DashboardState getState() {
        DashboardState dashboardState = new DashboardState();
        ComposerState composerState;

        switch (httpMethodBox.getValue()) {
            case "POST":
            case "PUT":
            case "PATCH":
                composerState = bodyTabController.getState();
                break;
            default:
                // For GET, DELETE requests
                composerState = new ComposerState();
        }

        composerState.target = addressField.getText();
        composerState.httpMethod = httpMethodBox.getValue();
        composerState.headers = headerTabController.getFieldStates();
        composerState.params = getParamFieldStates();

        dashboardState.composer = composerState;
        dashboardState.visibleResponseLayer = visibleLayer;
        dashboardState.visibleComposerTab = getVisibleComposerTab();

        switch (visibleLayer) {
            case RESPONSE:
                dashboardState.visibleResponseTab = getVisibleResponseTab();
                dashboardState.responseHeaders = responseHeadersViewer.getHeaders();
                dashboardState.responseCode = Integer.parseInt(statusCode.getText());

                String temp = responseSize.getText();
                temp = temp.substring(0, temp.length() - 2);
                dashboardState.responseSize = Integer.parseInt(temp);

                temp = responseTime.getText();
                temp = temp.substring(0, temp.length() - 3);
                dashboardState.responseTime = Integer.parseInt(temp);

                dashboardState.responseBody = responseArea.getText();

                // TODO: Get rid of similar switches
                String contentType;
                switch (responseTypeBox.getValue()) {
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

                dashboardState.responseType = contentType;
                break;
            case ERROR:
                dashboardState.errorTitle = errorTitle.getText();
                dashboardState.errorDetails = errorDetails.getText();
                break;
            case LOADING:
                dashboardState.handOverRequest(requestManager);
                requestManager = null;
                break;
        }

        return dashboardState;
    }

    /**
     * Sets the Dashboard to the given application state.
     *
     * @param state - State of the dashboard
     */
    public void setState(DashboardState state) {
        if (state == null)
            return;

        if (state.visibleComposerTab != null) {
            int tab;
            switch (state.visibleComposerTab) {
                case AUTH:
                    tab = 1;
                    break;
                case HEADERS:
                    tab = 2;
                    break;
                case BODY:
                    tab = 3;
                    break;
                default:
                    tab = 0;
            }

            requestOptionsTab.getSelectionModel().select(tab);
        }

        if (state.visibleResponseLayer != null) {
            switch (state.visibleResponseLayer) {
                case RESPONSE:
                    showResponse(state);
                    break;
                case ERROR:
                    errorTitle.setText(state.errorTitle);
                    errorDetails.setText(state.errorDetails);
                    showLayer(ResponseLayer.ERROR);
                    break;
                case LOADING:
                    /*
                        Accepts a RequestManager which is in the RUNNING state
                        and switches its handlers.
                        The handlers affect the Dashboard directly rather than the DashboardState.
                     */
                    requestManager = state.getRequestManager();
                    requestManager.removeHandlers();
                    requestManager.addHandlers(this::whileRunning, this::onSucceeded, this::onFailed, this::onCancelled);
                    showLayer(ResponseLayer.LOADING);
                    break;
                default:
                    showLayer(ResponseLayer.PROMPT);
                    break;
            }
        } else {
            showLayer(ResponseLayer.PROMPT);
        }

        if (state.composer == null)
            return;

        /*
             The only value from a set of constants in the state.json file is the httpMethod
             which, if manipulated to a non-standard value by the user, would still
             be loaded into the httpMethodBox, causing severe errors while making requests.

             To prevent this, we check if it is a standard value (GET, POST, PUT, PATCH or DELETE) and
             only then proceed to applying the rest of the state values to the Dashboard.

             This is an extreme case, but still something to be taken care of.
         */
        boolean validMethod = false;
        for (String method : httpMethods) {
            if (method.equals(state.composer.httpMethod))
                validMethod = true;
        }

        if (!validMethod) {
            LoggingService.logInfo("Application state file was tampered with. State could not be recovered.", LocalDateTime.now());
            return;
        }

        httpMethodBox.setValue(state.composer.httpMethod);

        if (state.composer.target != null)
            addressField.setText(state.composer.target);

        if (state.composer.headers != null) {
            for (FieldState fieldState : state.composer.headers)
                headerTabController.addHeader(fieldState);
        }

        if (state.composer.params != null) {
            for (FieldState fieldState : state.composer.params)
                addParamField(fieldState);
        }

        if (!(httpMethodBox.getValue().equals("GET") || httpMethodBox.getValue().equals("DELETE")))
            bodyTabController.setState(state.composer);
    }

    void reset() {
        httpMethodBox.setValue("GET");
        addressField.clear();
        headerTabController.clear();
        clearParams();
        bodyTabController.reset();
        responseArea.clear();
        showLayer(ResponseLayer.PROMPT);
        responseTabPane.getSelectionModel().select(0);
    }

    void clearParams() {
        if (params != null)
            params.clear();

        if (paramsControllers != null)
            paramsControllers.clear();

        paramsBox.getChildren().clear();
        paramsCountProperty.set(0);
        addParamField();
    }
}

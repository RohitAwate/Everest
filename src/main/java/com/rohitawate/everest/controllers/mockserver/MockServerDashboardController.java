/*
 * Copyright 2019 Rohit Awate.
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

package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.rohitawate.everest.Main;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.highlighters.HighlighterFactory;
import com.rohitawate.everest.format.FormatterFactory;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.notifications.NotificationsManager;
import com.rohitawate.everest.notifications.SnackbarChannel;
import com.rohitawate.everest.server.mock.Endpoint;
import com.rohitawate.everest.server.mock.MockServer;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MockServerDashboardController implements Initializable {
    @FXML
    private StackPane mockDashboardSP;
    @FXML
    private VBox endpointsBox, endpointDetailsBox;
    @FXML
    private ComboBox<String> methodBox, contentTypeBox, responseCodeBox;
    @FXML
    private JFXListView<ServerCard> serversList;
    @FXML
    private JFXListView<EndpointCard> endpointsList;
    @FXML
    private JFXTextField endpointPathField, endpointLatencyField;
    @FXML
    private TextField finalURLField;
    @FXML
    private ScrollPane codeAreaScrollPane;
    @FXML
    private JFXButton copyButton, openBrowserButton;

    private EverestCodeArea codeArea;

    private ServerCard selectedServerCard;

    private EndpointCard selectedEndpointCard;

    private Stage serviceDetailsStage;
    private ServerDetailsController serverDetailsController;

    public static final String CHANNEL_ID = "MockServerDashboard";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        methodBox.getItems().addAll(
                HTTPConstants.GET,
                HTTPConstants.POST,
                HTTPConstants.PUT,
                HTTPConstants.PATCH,
                HTTPConstants.DELETE
        );
        methodBox.setValue(HTTPConstants.GET);

        contentTypeBox.getItems().addAll(
                HTTPConstants.JSON,
                HTTPConstants.XML,
                HTTPConstants.HTML,
                HTTPConstants.PLAIN_TEXT
        );

        contentTypeBox.valueProperty().addListener(change -> codeArea.setHighlighter(HighlighterFactory.getHighlighter(contentTypeBox.getValue())));

        EverestResponse.statusCodeReasonPhrases.forEach((key, value) -> responseCodeBox.getItems().add(key + " (" + value + ")"));

        NotificationsManager.registerChannel(CHANNEL_ID, new SnackbarChannel(mockDashboardSP));

        codeArea = new EverestCodeArea();
        codeAreaScrollPane.setContent(new VirtualizedScrollPane<>(codeArea));

        contentTypeBox.getSelectionModel().select(0);

        endpointsBox.setDisable(true);
        endpointDetailsBox.setDisable(true);

        endpointPathField.textProperty().addListener(this::pathListener);
        methodBox.valueProperty().addListener(this::methodListener);
        codeArea.textProperty().addListener(this::codeAreaListener);
        contentTypeBox.valueProperty().addListener(this::contentTypeBoxListener);
        responseCodeBox.valueProperty().addListener(this::responseCodeListener);
        endpointLatencyField.textProperty().addListener(this::latencyListener);

        copyButton.setOnAction(e -> {
            finalURLField.selectAll();
            finalURLField.copy();
            finalURLField.deselect();
        });

        openBrowserButton.setOnAction(e -> EverestUtilities.openLinkInBrowser(finalURLField.getText()));
    }

    @FXML
    private void showNewServerDialog(ActionEvent actionEvent) {
        if (serviceDetailsStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/mockserver/ServerDetails.fxml"));
                Parent serviceAdderFXML = loader.load();
                serviceDetailsStage = new Stage();
                serviceDetailsStage.setScene(new Scene(serviceAdderFXML));
                serverDetailsController = loader.getController();
                serviceDetailsStage.setTitle("Add new mock server - " + Main.APP_NAME);
                serviceDetailsStage.setResizable(false);
                serviceDetailsStage.initModality(Modality.APPLICATION_MODAL);
                serviceDetailsStage.getIcons().add(Main.APP_ICON);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        serverDetailsController.setMode(ServerDetailsController.ADD_MODE);
        serviceDetailsStage.showAndWait();

        if (serverDetailsController.getServer() != null) {
            addNewServer(serverDetailsController.getServer());
        }
    }

    private void addNewServer(MockServer server) {
        ServerCard serverCard = new ServerCard(server);
        serverCard.setOptionsStage(serviceDetailsStage, serverDetailsController,
                this, deleteServerHandler(serverCard), cloneServerHandler(serverCard));
        serversList.getItems().add(serverCard);
        serversList.getSelectionModel().select(serverCard);
        onServiceSelected(null);
        addNewEndpoint(null);
    }

    private EventHandler<ActionEvent> cloneServerHandler(ServerCard serverCard) {
        return event -> {
            MockServer clone = new MockServer("(Copy) " + serverCard.server.name, serverCard.server.getPort() + 1);
            addNewServer(clone);
        };
    }

    private EventHandler<ActionEvent> deleteServerHandler(ServerCard serverCard) {
        return event -> {
            selectedServerCard = null;
            selectedEndpointCard = null;
            serversList.getItems().remove(serverCard);
            endpointsList.getItems().clear();
            resetEndpointDetails();

            try {
                serverCard.server.stop();
            } catch (IOException e) {
                Logger.severe("Error while deleting server.", e);
            }
        };
    }

    @FXML
    private void addNewEndpoint(ActionEvent actionEvent) {
        Endpoint newEndpoint = new Endpoint();
        EndpointCard newCard = new EndpointCard(newEndpoint);
        newCard.path.setText("/");
        endpointsList.getItems().add(newCard);
        endpointsList.getSelectionModel().select(newCard);
        selectedServerCard.server.addEndpoint(newEndpoint);
        onEndpointSelected(null);
        checkDuplicateEndpoints();
        endpointPathField.requestFocus();
        setFinalURLField();
    }

    private void resetEndpointDetails() {
        selectedEndpointCard = null;

        methodBox.getSelectionModel().select(0);
        endpointPathField.clear();
        contentTypeBox.getSelectionModel().select(0);
        codeArea.clear();
        responseCodeBox.getSelectionModel().select(0);
        setFinalURLField();

        endpointDetailsBox.setDisable(true);
    }

    private void populateEndpointsList(MockServer service) {
        endpointsList.getItems().clear();

        for (Endpoint endpoint : service.getEndpoints()) {
            endpointsList.getItems().add(new EndpointCard(endpoint));
        }
    }

    private void setResponseCode(int responseCode) {
        responseCodeBox.setValue(responseCode + " (" + EverestResponse.getReasonPhrase(responseCode) + ")");
    }

    private void checkDuplicateEndpoints() {
        boolean duplicate;
        for (EndpointCard outerCard : endpointsList.getItems()) {
            duplicate = false;
            for (EndpointCard innerCard : endpointsList.getItems()) {
                if (innerCard != outerCard) {
                    if (innerCard.endpoint.method.equals(outerCard.endpoint.method) &&
                            innerCard.endpoint.path.equals(outerCard.endpoint.path)) {
                        outerCard.showAlert();
                        innerCard.showAlert();
                        duplicate = true;
                    }
                }

                if (!duplicate) {
                    outerCard.hideAlert();
                }
            }
        }
    }

    void setFinalURLField() {
        if (selectedServerCard != null && !endpointsBox.isDisable()) {
            String finalURL = "http://localhost:" + String.valueOf(selectedServerCard.server.getPort());

            if (selectedServerCard.server.isAttachPrefix()) {
                finalURL += selectedServerCard.server.getPrefix();
            }

            if (selectedEndpointCard != null) {
                if (endpointPathField.getText().isEmpty()) {
                    finalURL += "/";
                } else {
                    finalURL += selectedEndpointCard.endpoint.path;
                }
            }

            finalURLField.setText(EverestUtilities.encodeURL(finalURL));
        } else {
            finalURLField.clear();
        }
    }

    // Listeners
    private void pathListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            if (!newVal.isEmpty() && newVal.startsWith("/")) {
                newVal = newVal.substring(1);
                endpointPathField.setText(newVal);
            }

            newVal = "/" + newVal;
            selectedEndpointCard.path.setText(newVal);
            selectedEndpointCard.endpoint.path = newVal;
            setFinalURLField();
            checkDuplicateEndpoints();
        }
    }

    private void methodListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.method.setText(newVal);
            selectedEndpointCard.endpoint.method = newVal;
            EndpointCard.applyStyle(selectedEndpointCard.method);
            checkDuplicateEndpoints();
        }
    }

    private void responseCodeListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.endpoint.responseCode = Integer.parseInt(newVal.substring(0, 3));
        }
    }

    private void codeAreaListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.endpoint.resource = newVal;
        }
    }

    private void contentTypeBoxListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.endpoint.contentType = HTTPConstants.getComplexContentType(newVal);
        }
    }

    private void latencyListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            if (!newVal.matches("\\d*")) {
                newVal = newVal.replaceAll("[^\\d]", "");
                endpointLatencyField.setText(newVal);
            }

            if (!newVal.isEmpty()) {
                selectedEndpointCard.endpoint.latency = Integer.parseInt(newVal);
            }
        }
    }

    @FXML
    private void onServiceSelected(MouseEvent event) {
        selectedServerCard = serversList.getSelectionModel().getSelectedItem();
        if (selectedServerCard != null) {
            resetEndpointDetails();
            populateEndpointsList(selectedServerCard.server);
            endpointsBox.setDisable(false);
            setFinalURLField();
        } else {
            endpointsBox.setDisable(true);
        }
    }

    @FXML
    private void onEndpointSelected(MouseEvent event) {
        resetEndpointDetails();

        selectedEndpointCard = endpointsList.getSelectionModel().getSelectedItem();

        if (selectedEndpointCard != null) {
            endpointPathField.setText(selectedEndpointCard.endpoint.path);
            methodBox.setValue(selectedEndpointCard.endpoint.method);
            contentTypeBox.setValue(HTTPConstants.getSimpleContentType(selectedEndpointCard.endpoint.contentType));
            codeArea.setText(selectedEndpointCard.endpoint.resource, FormatterFactory.getFormatter(contentTypeBox.getValue()),
                    HighlighterFactory.getHighlighter(contentTypeBox.getValue()));
            setResponseCode(selectedEndpointCard.endpoint.responseCode);
            endpointDetailsBox.setDisable(false);
        }
    }
}

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

package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.*;
import com.rohitawate.everest.server.mock.MockServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerDetailsController implements Initializable {
    @FXML
    private VBox serverDetailsBox;
    @FXML
    private JFXTextField serverNameField, serverPortField, serverPrefixField, serverLatencyField;
    @FXML
    private JFXCheckBox attachPrefixCheckBox;
    @FXML
    private JFXToggleButton loggingEnableToggle;
    @FXML
    private Label titleLabel;
    @FXML
    private JFXButton serverActionButton, cancelActionButton;

    private MockServer server;

    static final String ADD_MODE = "ADD";
    static final String UPDATE_MODE = "UPDATE";

    private JFXSnackbar snackbar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverPrefixField.disableProperty().bind(attachPrefixCheckBox.selectedProperty().not());
        serverActionButton.setOnAction(this::onAction);
        cancelActionButton.setOnAction(e -> ((Stage) cancelActionButton.getScene().getWindow()).close());

        snackbar = new JFXSnackbar(serverDetailsBox);

        serverPortField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                newVal = newVal.replaceAll("[^\\d]", "");
                serverPortField.setText(newVal);
            }

            if (!newVal.equals("") && Integer.parseInt(newVal) > 65535) {
                serverPortField.setText("65535");
            }
        });

        serverLatencyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                newVal = newVal.replaceAll("[^\\d]", "");
                serverLatencyField.setText(newVal);
            }
        });
    }

    private void onAction(ActionEvent actionEvent) {
        if (serverNameField.getText().isEmpty() || serverNameField.getText().trim().equals("")) {
            serverNameField.clear();
            snackbar.show("Name required.", 5000);
            return;
        }

        if (serverPortField.getText().isEmpty()) {
            snackbar.show("Port number required.", 5000);
            return;
        }

        if (serverActionButton.getText().equals(ADD_MODE)) {
            server = new MockServer(serverNameField.getText(), Integer.parseInt(serverPortField.getText()));
        } else if (serverActionButton.getText().equals(UPDATE_MODE)) {
            server.name = serverNameField.getText();
        }

        if (attachPrefixCheckBox.isSelected()) {
            if (serverPrefixField.getText().trim().isEmpty()) {
                snackbar.show("Prefix required.", 5000);
                return;
            } else {
                if (serverPrefixField.getText().trim().startsWith("/")) {
                    serverPrefixField.setText(serverPrefixField.getText().trim().substring(1));
                }

                server.setPrefix("/" + serverPrefixField.getText().trim());
            }
        }

        if (!serverLatencyField.getText().isEmpty()) {
            server.latency = Integer.parseInt(serverLatencyField.getText());
        } else {
            server.latency = 0;
        }

        server.setAttachPrefix(attachPrefixCheckBox.isSelected());
        server.loggingEnabled = loggingEnableToggle.isSelected();
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    void setMode(String mode) {
        if (mode.equals(ADD_MODE)) {
            server = null;
            serverNameField.clear();
            serverPortField.clear();
            attachPrefixCheckBox.setSelected(false);
            serverPrefixField.clear();
            serverLatencyField.setText("0");
            loggingEnableToggle.setSelected(false);

            titleLabel.setText("A D D   N E W   S E R V E R");
            serverActionButton.setText(ADD_MODE);
            serverPortField.setDisable(false);
        } else if (mode.equals(UPDATE_MODE)) {
            titleLabel.setText("S E R V E R   D E T A I L S");
            serverActionButton.setText(UPDATE_MODE);
            serverPortField.setText(String.valueOf(server.getPort()));
            serverLatencyField.setText(String.valueOf(server.latency));
            serverPortField.setDisable(true);
            serverNameField.setText(server.name);

            if (server.getPrefix().startsWith("/")) {
                serverPrefixField.setText(server.getPrefix().substring(1));
            } else {
                serverPrefixField.setText(server.getPrefix());
            }

            attachPrefixCheckBox.setSelected(server.isAttachPrefix());
            loggingEnableToggle.setSelected(server.loggingEnabled);
        }
    }

    void setServer(MockServer server) {
        this.server = server;
    }

    MockServer getServer() {
        return server;
    }
}

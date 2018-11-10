package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.*;
import com.rohitawate.everest.server.mock.MockService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ServiceDetailsController implements Initializable {
    @FXML
    private VBox serviceDetailsBox;
    @FXML
    private JFXTextField serviceNameField, servicePortField, servicePrefixField, serviceLatencyField;
    @FXML
    private JFXCheckBox attachPrefixCheckBox;
    @FXML
    private JFXToggleButton loggingEnableToggle;
    @FXML
    private Label titleLabel;
    @FXML
    private JFXButton serviceActionButton, cancelActionButton;

    private MockService service;

    static final String ADD_MODE = "ADD";
    static final String UPDATE_MODE = "UPDATE";

    private JFXSnackbar snackbar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        servicePrefixField.disableProperty().bind(attachPrefixCheckBox.selectedProperty().not());
        serviceActionButton.setOnAction(this::onAction);
        cancelActionButton.setOnAction(e -> ((Stage) cancelActionButton.getScene().getWindow()).close());

        snackbar = new JFXSnackbar(serviceDetailsBox);

        servicePortField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                newVal = newVal.replaceAll("[^\\d]", "");
                servicePortField.setText(newVal);
            }

            if (!newVal.equals("") && Integer.parseInt(newVal) > 65535) {
                servicePortField.setText("65535");
            }
        });

        serviceLatencyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                newVal = newVal.replaceAll("[^\\d]", "");
                serviceLatencyField.setText(newVal);
            }
        });
    }

    private void onAction(ActionEvent actionEvent) {
        if (serviceNameField.getText().isEmpty() || serviceNameField.getText().trim().equals("")) {
            serviceNameField.clear();
            snackbar.show("Name required.", 5000);
            return;
        }

        if (servicePortField.getText().isEmpty()) {
            snackbar.show("Port number required.", 5000);
            return;
        }

        if (serviceActionButton.getText().equals(ADD_MODE)) {
            service = new MockService(serviceNameField.getText(), Integer.parseInt(servicePortField.getText()));
        } else if (serviceActionButton.getText().equals(UPDATE_MODE)) {
            service.name = serviceNameField.getText();
        }

        if (attachPrefixCheckBox.isSelected()) {
            if (servicePrefixField.getText().trim().isEmpty()) {
                snackbar.show("Prefix required.", 5000);
                return;
            } else {
                if (servicePrefixField.getText().trim().startsWith("/")) {
                    servicePrefixField.setText(servicePrefixField.getText().trim().substring(1));
                }

                service.setPrefix("/" + servicePrefixField.getText().trim());
            }
        }

        if (!serviceLatencyField.getText().isEmpty()) {
            service.latency = Integer.parseInt(serviceLatencyField.getText());
        }

        service.setAttachPrefix(attachPrefixCheckBox.isSelected());
        service.loggingEnabled = loggingEnableToggle.isSelected();
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    void setMode(String mode) {
        if (mode.equals(ADD_MODE)) {
            service = null;
            serviceNameField.clear();
            servicePortField.clear();
            attachPrefixCheckBox.setSelected(false);
            servicePrefixField.clear();
            serviceLatencyField.clear();
            loggingEnableToggle.setSelected(false);

            titleLabel.setText("A D D   N E W   S E R V I C E");
            serviceActionButton.setText(ADD_MODE);
            servicePortField.setDisable(false);
        } else if (mode.equals(UPDATE_MODE)) {
            titleLabel.setText("S E R V I C E   D E T A I L S");
            serviceActionButton.setText(UPDATE_MODE);
            servicePortField.setText(String.valueOf(service.getPort()));
            serviceLatencyField.setText(String.valueOf(service.latency));
            servicePortField.setDisable(true);
            serviceNameField.setText(service.name);

            if (service.getPrefix().startsWith("/")) {
                servicePrefixField.setText(service.getPrefix().substring(1));
            } else {
                servicePrefixField.setText(service.getPrefix());
            }

            attachPrefixCheckBox.setSelected(service.isAttachPrefix());
            loggingEnableToggle.setSelected(service.loggingEnabled);
        }
    }

    void setService(MockService service) {
        this.service = service;
    }

    MockService getService() {
        return service;
    }
}

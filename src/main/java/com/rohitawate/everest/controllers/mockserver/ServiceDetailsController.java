package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.rohitawate.everest.server.mock.MockService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ServiceDetailsController implements Initializable {
    @FXML
    private JFXTextField serviceNameField, servicePortField, servicePrefixField;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        servicePrefixField.disableProperty().bind(attachPrefixCheckBox.selectedProperty().not());
        serviceActionButton.setOnAction(this::onAction);
        cancelActionButton.setOnAction(e -> ((Stage) cancelActionButton.getScene().getWindow()).close());
    }

    private void onAction(ActionEvent actionEvent) {
        // TODO: Empty checks, trimming, NumberFormatException, prefix '/' trimming
        if (serviceActionButton.getText().equals(ADD_MODE)) {
            service = new MockService(serviceNameField.getText(), Integer.parseInt(servicePortField.getText()));
        } else if (serviceActionButton.getText().equals(UPDATE_MODE)) {
            service.name = serviceNameField.getText();
        }

        if (!servicePrefixField.getText().isEmpty()) {
            service.setPrefix("/" + servicePrefixField.getText());
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
            loggingEnableToggle.setSelected(false);

            titleLabel.setText("A D D   N E W   S E R V I C E");
            serviceActionButton.setText(ADD_MODE);
            servicePortField.setDisable(false);
        } else if (mode.equals(UPDATE_MODE)) {
            titleLabel.setText("S E R V I C E   D E T A I L S");
            serviceActionButton.setText(UPDATE_MODE);
            servicePortField.setText(String.valueOf(service.getPort()));
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

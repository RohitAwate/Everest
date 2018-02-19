package com.rohitawate.restaurant.homewindow;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryItemController implements Initializable {
    @FXML
    private Label requestType, address;
    @FXML
    private Tooltip tooltip;

    public void setRequestType(String requestType) {
        this.requestType.setText(requestType);
    }

    public void setAddress(String address) {
        this.address.setText(address);
    }

    public String getRequestType() {
        return requestType.getText();
    }

    public String getAddress() {
        return address.getText();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tooltip.textProperty().bind(address.textProperty());
    }
}

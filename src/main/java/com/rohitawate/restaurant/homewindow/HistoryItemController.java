package com.rohitawate.restaurant.homewindow;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryItemController {
    @FXML
    private Label requestType, address;

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
}

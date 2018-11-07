package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXToggleButton;
import com.rohitawate.everest.models.requests.HTTPConstants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MockServerDashboardController implements Initializable {
    @FXML
    private StackPane mockDashboardSP;
    @FXML
    private JFXToggleButton startButton;
    @FXML
    private JFXButton optionsButton;
    @FXML
    private ComboBox<String> methodBox;
    @FXML
    private JFXListView<ServiceCard> servicesList;
    @FXML
    private JFXListView<?> endpointsList;

    @FXML
    private TextField pathField;

    private JFXSnackbar snackbar;

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

        snackbar = new JFXSnackbar(mockDashboardSP);
    }
}

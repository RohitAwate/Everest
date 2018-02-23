package com.rohitawate.restaurant.homewindow;

import com.rohitawate.restaurant.models.DashboardState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class HistoryItemController implements Initializable {
    @FXML
    private Label requestType, address;
    @FXML
    private Tooltip tooltip;

    private DashboardState dashboardState;

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

    public DashboardState getDashboardState() {
        return dashboardState;
    }

    public void setDashboardState(DashboardState dashboardState) {
        this.dashboardState = dashboardState;
    }

    public int getRelativityIndex(String searchString) {
        searchString = searchString.toLowerCase();
        String comparisonString;

        // Checks if matches with HTTP method
        comparisonString = dashboardState.getHttpMethod().toLowerCase();
        if (comparisonString.contains(searchString))
            return 7;

        // Checks if matches with target
        comparisonString = dashboardState.getTarget().toString().toLowerCase();
        if (comparisonString.contains(searchString))
            return 10;

        // Checks for a match in the params
        for (Map.Entry param : dashboardState.getParams().entrySet()) {
            if (param.getKey().toString().toLowerCase().contains(searchString) ||
                    param.getKey().toString().toLowerCase().contains(searchString))
                return 5;
        }

        // Checks for a match in the headers
        for (Map.Entry header : dashboardState.getHeaders().entrySet()) {
            if (header.getKey().toString().toLowerCase().contains(searchString) ||
                    header.getValue().toString().toLowerCase().contains(searchString))
                return 6;
        }

        if (dashboardState.getHttpMethod().equals("POST") || dashboardState.getHttpMethod().equals("PUT")) {
            switch (dashboardState.getContentType()) {
                case MediaType.TEXT_PLAIN:
                case MediaType.APPLICATION_JSON:
                case MediaType.APPLICATION_XML:
                case MediaType.TEXT_HTML:
                case MediaType.APPLICATION_OCTET_STREAM:
                    // Checks for match in body of the request
                    comparisonString = dashboardState.getBody().toLowerCase();
                    if (comparisonString.contains(searchString))
                        return 8;
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    // Checks for match in string tuples
                    for (Map.Entry tuple : dashboardState.getStringTuples().entrySet()) {
                        if (tuple.getKey().toString().toLowerCase().contains(searchString) ||
                                tuple.getValue().toString().toLowerCase().contains(searchString))
                            return 8;
                    }
                    break;
                case MediaType.MULTIPART_FORM_DATA:
                    // Checks for match in string and file tuples
                    for (Map.Entry tuple : dashboardState.getStringTuples().entrySet()) {
                        if (tuple.getKey().toString().toLowerCase().contains(searchString) ||
                                tuple.getValue().toString().toLowerCase().contains(searchString))
                            return 8;
                    }

                    for (Map.Entry tuple : dashboardState.getFileTuples().entrySet()) {
                        if (tuple.getKey().toString().toLowerCase().contains(searchString) ||
                                tuple.getValue().toString().toLowerCase().contains(searchString))
                            return 8;
                    }
                    break;
            }
        }
        return 0;
    }
}

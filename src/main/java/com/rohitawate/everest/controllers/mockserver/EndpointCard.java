package com.rohitawate.everest.controllers.mockserver;

import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.server.mock.Endpoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

class EndpointCard extends HBox {
    private Label method;
    private Label path;

    EndpointCard(Endpoint endpoint) {
        method = new Label(endpoint.method);
        applyStyle(method);
        method.getStyleClass().add("endpoint-card-method");

        path = new Label(endpoint.path);
        path.getStyleClass().add("endpoint-card-path");

        getStyleClass().add("endpoint-card");
        getChildren().addAll(method, path);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(2, 10, 2, 10));
    }

    private static final String GETStyle = "-fx-background-color: orangered";
    private static final String POSTStyle = "-fx-background-color: cornflowerblue";
    private static final String PUTStyle = "-fx-background-color: deeppink";
    private static final String PATCHStyle = "-fx-background-color: teal";
    private static final String DELETEStyle = "-fx-background-color: limegreen";

    private static void applyStyle(Label label) {
        switch (label.getText()) {
            case HTTPConstants.GET:
                label.setStyle(GETStyle);
                break;
            case HTTPConstants.POST:
                label.setStyle(POSTStyle);
                break;
            case HTTPConstants.PUT:
                label.setStyle(PUTStyle);
                break;
            case HTTPConstants.PATCH:
                label.setStyle(PATCHStyle);
                break;
            case HTTPConstants.DELETE:
                label.setStyle(DELETEStyle);
                break;
        }
    }
}

package com.rohitawate.everest.controllers.mockserver;

import com.rohitawate.everest.server.mock.MockService;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

class ServiceCard extends HBox {
    private Label nameLabel;

    ServiceCard(MockService service) {
        this.nameLabel = new Label(service.name);
        this.getChildren().add(nameLabel);
        this.getStyleClass().add("service-card");
    }
}

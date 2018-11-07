package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import com.rohitawate.everest.server.mock.MockService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;

import java.io.IOException;

import static com.rohitawate.everest.controllers.mockserver.MockServerDashboardController.pushServerNotification;

class ServiceCard extends HBox {
    private Label name;
    private final JFXToggleButton toggle;
    private final JFXButton optionsButton;
    final MockService service;

    ServiceCard(MockService service) {
        this.service = service;

        name = new Label(service.name);

        toggle = new JFXToggleButton();
        toggle.setGraphicTextGap(20);
        toggle.setToggleColor(Paint.valueOf("#ff4500"));
        toggle.setToggleLineColor(Paint.valueOf("#cacaca"));
        toggle.setContentDisplay(ContentDisplay.RIGHT);
        toggle.setOnAction(this::toggleService);

        optionsButton = new JFXButton();
        ImageView optionsImage = new ImageView(getClass().getResource("/assets/Settings.png").toString());
        optionsImage.setFitWidth(20);
        optionsImage.setFitHeight(20);
        optionsButton.setGraphic(optionsImage);

        HBox filler = new HBox(toggle, optionsButton);
        HBox.setHgrow(filler, Priority.ALWAYS);
        filler.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(name, filler);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("service-card");
        setSpacing(20);
        setPadding(new Insets(0, 10, 0, 10));
    }

    private void toggleService(ActionEvent actionEvent) {
        if (toggle.isSelected()) {
            try {
                service.start();
                pushServerNotification(String.format("Mock service '%s' has started.", service.name), 7000);
            } catch (IOException e) {
                pushServerNotification(String.format("Could not start mock service '%s'.", service.name), 7000);
            }
        } else {
            service.stop();
            pushServerNotification(String.format("Mock service '%s' has stopped.", service.name), 7000);
        }
    }
}

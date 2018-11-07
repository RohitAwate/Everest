package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.highlighters.HighlighterFactory;
import com.rohitawate.everest.format.FormatterFactory;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.server.mock.Endpoint;
import com.rohitawate.everest.server.mock.MockService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MockServerDashboardController implements Initializable {
    @FXML
    private StackPane mockDashboardSP;
    @FXML
    private VBox endpointDetailsBox;
    @FXML
    private JFXButton optionsButton;
    @FXML
    private ComboBox<String> methodBox, contentTypeBox, responseCodeBox;
    @FXML
    private JFXListView<ServiceCard> servicesList;
    @FXML
    private JFXListView<EndpointCard> endpointsList;
    @FXML
    private TextField pathField;
    @FXML
    private ScrollPane codeAreaScrollPane;

    private static JFXSnackbar snackbar;
    private EverestCodeArea codeArea;

    private ServiceCard selectedServiceCard;
    private MockService selectedService;

    private EndpointCard selectedEndpointCard;
    private Endpoint selectedEndpoint;

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

        contentTypeBox.getItems().addAll(
                HTTPConstants.JSON,
                HTTPConstants.XML,
                HTTPConstants.HTML,
                HTTPConstants.PLAIN_TEXT
        );

        contentTypeBox.valueProperty().addListener(change -> codeArea.setHighlighter(HighlighterFactory.getHighlighter(contentTypeBox.getValue())));

        EverestResponse.statusCodeReasonPhrases.forEach((key, value) -> responseCodeBox.getItems().add(key + " (" + value + ")"));

        snackbar = new JFXSnackbar(mockDashboardSP);

        codeArea = new EverestCodeArea();
        codeAreaScrollPane.setContent(new VirtualizedScrollPane<>(codeArea));

        contentTypeBox.getSelectionModel().select(0);

        MockService service = null;
        try {
            service = new MockService("Summit", "/api", 9090);
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.loggingEnabled = true;

        MockService service2 = null;
        try {
            service2 = new MockService("Everest", 9091);
        } catch (IOException e) {
            e.printStackTrace();
        }
        service2.loggingEnabled = true;

        Endpoint endpoint = new Endpoint(HTTPConstants.GET, "/summit", 200,
                "{ \"name\": \"Rohit\", \"age\": 20 }", MediaType.APPLICATION_JSON);
        Endpoint endpoint2 = new Endpoint(HTTPConstants.PATCH, "/welcome", 200,
                "{ \"name\": \"Nolan\", \"age\": 48 }", MediaType.APPLICATION_JSON);
        Endpoint endpoint3 = new Endpoint(HTTPConstants.POST, "/post", 404,
                "<name>Rohit</name>", MediaType.APPLICATION_XML);

        service.addEndpoint(endpoint);
        service2.addEndpoint(endpoint2);
        service2.addEndpoint(endpoint3);

        servicesList.getItems().add(new ServiceCard(service));
        servicesList.getItems().add(new ServiceCard(service2));

        servicesList.setOnMouseClicked(this::onServiceSelected);
        endpointsList.setOnMouseClicked(this::onEndpointSelected);
    }

    private void onServiceSelected(MouseEvent event) {
        selectedServiceCard = servicesList.getSelectionModel().getSelectedItem();
        selectedService = selectedServiceCard.service;
        populateEndpointsList(selectedService);
        clearEndpointDetails();
    }


    private void onEndpointSelected(MouseEvent event) {
        selectedEndpointCard = endpointsList.getSelectionModel().getSelectedItem();
        selectedEndpoint = selectedEndpointCard.endpoint;
        populateEndpointDetails(selectedEndpoint);
    }

    private void populateEndpointsList(MockService service) {
        endpointsList.getItems().clear();

        for (Endpoint endpoint : service.getEndpoints()) {
            endpointsList.getItems().add(new EndpointCard(endpoint));
        }
    }

    private void populateEndpointDetails(Endpoint endpoint) {
        endpointDetailsBox.setDisable(false);
        methodBox.setValue(endpoint.method);
        pathField.setText(endpoint.path);
        contentTypeBox.setValue(HTTPConstants.getSimpleContentType(endpoint.contentType));
        codeArea.setText(endpoint.resource, FormatterFactory.getFormatter(contentTypeBox.getValue()),
                HighlighterFactory.getHighlighter(contentTypeBox.getValue()));
        setResponseCode(endpoint.responseCode);
    }

    private void clearEndpointDetails() {
        methodBox.getSelectionModel().select(0);
        pathField.clear();
        contentTypeBox.getSelectionModel().select(0);
        codeArea.clear();
        responseCodeBox.getSelectionModel().select(0);
        endpointDetailsBox.setDisable(true);
    }

    private void setResponseCode(int responseCode) {
        responseCodeBox.setValue(responseCode + " (" + EverestResponse.getReasonPhrase(responseCode) + ")");
    }

    static void pushServerNotification(String message, long duration) {
        snackbar.show(message, duration);
    }
}

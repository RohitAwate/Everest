package com.rohitawate.everest.controllers.mockserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.rohitawate.everest.Main;
import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.highlighters.HighlighterFactory;
import com.rohitawate.everest.format.FormatterFactory;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.models.responses.EverestResponse;
import com.rohitawate.everest.server.mock.Endpoint;
import com.rohitawate.everest.server.mock.MockService;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MockServerDashboardController implements Initializable {
    @FXML
    private StackPane mockDashboardSP;
    @FXML
    private VBox endpointsBox, endpointDetailsBox;
    @FXML
    private ComboBox<String> methodBox, contentTypeBox, responseCodeBox;
    @FXML
    private JFXListView<ServiceCard> servicesList;
    @FXML
    private JFXListView<EndpointCard> endpointsList;
    @FXML
    private JFXTextField endpointPathField, finalURLField;
    @FXML
    private ScrollPane codeAreaScrollPane;
    @FXML
    private JFXButton copyButton;

    private static JFXSnackbar snackbar;
    private EverestCodeArea codeArea;

    private ServiceCard selectedServiceCard;

    private EndpointCard selectedEndpointCard;

    private Stage serviceDetailsStage;
    private ServiceDetailsController serviceDetailsController;

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

        endpointsBox.setDisable(true);
        endpointDetailsBox.setDisable(true);

        endpointPathField.textProperty().addListener(this::pathListener);
        methodBox.valueProperty().addListener(this::methodListener);
        codeArea.textProperty().addListener(this::codeAreaListener);
        contentTypeBox.valueProperty().addListener(this::contentTypeBoxListener);
        responseCodeBox.valueProperty().addListener(this::responseCodeListener);

        copyButton.setOnAction(e -> {
            finalURLField.selectAll();
            finalURLField.copy();
            finalURLField.deselect();
        });
    }

    @FXML
    private void addNewService(ActionEvent actionEvent) {
        if (serviceDetailsStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/mockserver/ServiceDetails.fxml"));
                Parent serviceAdderFXML = loader.load();
                serviceDetailsStage = new Stage();
                serviceDetailsStage.setScene(new Scene(serviceAdderFXML));
                serviceDetailsController = loader.getController();
                serviceDetailsStage.setTitle("Add new mock service - " + Main.APP_NAME);
                serviceDetailsStage.setResizable(false);
                serviceDetailsStage.initModality(Modality.APPLICATION_MODAL);
                serviceDetailsStage.getIcons().add(Main.APP_ICON);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        serviceDetailsController.setMode(ServiceDetailsController.ADD_MODE);
        serviceDetailsStage.showAndWait();

        if (serviceDetailsController.getService() != null) {
            ServiceCard serviceCard = new ServiceCard(serviceDetailsController.getService());
            serviceCard.setOptionsStage(serviceDetailsStage, serviceDetailsController, this);
            servicesList.getItems().add(serviceCard);
            servicesList.getSelectionModel().select(serviceCard);
            onServiceSelected(null);
        }
    }

    @FXML
    private void addNewEndpoint(ActionEvent actionEvent) {
        Endpoint newEndpoint = new Endpoint();
        EndpointCard newCard = new EndpointCard(newEndpoint);
        newCard.path.setText("/");
        endpointsList.getItems().add(newCard);
        endpointsList.getSelectionModel().select(newCard);
        selectedServiceCard.service.addEndpoint(newEndpoint);
        onEndpointSelected(null);
        checkDuplicateEndpoints();
    }

    private void resetEndpointDetails() {
        selectedEndpointCard = null;

        methodBox.getSelectionModel().select(0);
        endpointPathField.clear();
        contentTypeBox.getSelectionModel().select(0);
        codeArea.clear();
        responseCodeBox.getSelectionModel().select(0);

        endpointDetailsBox.setDisable(true);
    }

    private void populateEndpointsList(MockService service) {
        endpointsList.getItems().clear();

        for (Endpoint endpoint : service.getEndpoints()) {
            endpointsList.getItems().add(new EndpointCard(endpoint));
        }
    }

    private void setResponseCode(int responseCode) {
        responseCodeBox.setValue(responseCode + " (" + EverestResponse.getReasonPhrase(responseCode) + ")");
    }

    static void pushServerNotification(String message, long duration) {
        snackbar.show(message, duration);
    }

    private void checkDuplicateEndpoints() {
        boolean duplicate;
        for (EndpointCard outerCard : endpointsList.getItems()) {
            duplicate = false;
            for (EndpointCard innerCard : endpointsList.getItems()) {
                if (innerCard != outerCard) {
                    if (innerCard.endpoint.method.equals(outerCard.endpoint.method) &&
                            innerCard.endpoint.path.equals(outerCard.endpoint.path)) {
                        outerCard.showAlert();
                        innerCard.showAlert();
                        duplicate = true;
                    }
                }

                if (!duplicate) {
                    outerCard.hideAlert();
                }
            }
        }
    }

    void setFinalURLField() {
        if (selectedServiceCard != null && !endpointsBox.isDisable()) {
            String finalURL = "http://localhost:" + String.valueOf(selectedServiceCard.service.getPort());

            if (selectedServiceCard.service.isAttachPrefix()) {
                finalURL += selectedServiceCard.service.getPrefix();
            }

            if (selectedEndpointCard != null) {
                finalURL += selectedEndpointCard.endpoint.path;
            }

            finalURLField.setText(finalURL);
        } else {
            finalURLField.clear();
        }
    }

    // Listeners
    private void pathListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            if (!newVal.isEmpty() && newVal.startsWith("/")) {
                newVal = newVal.substring(1);
                endpointPathField.setText(newVal);
            }

            newVal = "/" + newVal;
            selectedEndpointCard.path.setText(newVal);
            selectedEndpointCard.endpoint.path = newVal;
            setFinalURLField();
            checkDuplicateEndpoints();
        }
    }

    private void methodListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.method.setText(newVal);
            selectedEndpointCard.endpoint.method = newVal;
            EndpointCard.applyStyle(selectedEndpointCard.method);
            checkDuplicateEndpoints();
        }
    }

    private void responseCodeListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.endpoint.responseCode = Integer.parseInt(newVal.substring(0, 3));
        }
    }

    private void codeAreaListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.endpoint.resource = newVal;
        }
    }

    private void contentTypeBoxListener(Observable observable, String oldVal, String newVal) {
        if (selectedEndpointCard != null) {
            selectedEndpointCard.endpoint.contentType = HTTPConstants.getComplexContentType(newVal);
        }
    }

    @FXML
    private void onServiceSelected(MouseEvent event) {
        selectedServiceCard = servicesList.getSelectionModel().getSelectedItem();
        if (selectedServiceCard != null) {
            resetEndpointDetails();
            populateEndpointsList(selectedServiceCard.service);
            endpointsBox.setDisable(false);
            setFinalURLField();
        } else {
            endpointsBox.setDisable(true);
        }
    }

    @FXML
    private void onEndpointSelected(MouseEvent event) {
        resetEndpointDetails();

        selectedEndpointCard = endpointsList.getSelectionModel().getSelectedItem();

        if (selectedEndpointCard != null) {
            endpointPathField.setText(selectedEndpointCard.endpoint.path);
            methodBox.setValue(selectedEndpointCard.endpoint.method);
            contentTypeBox.setValue(HTTPConstants.getSimpleContentType(selectedEndpointCard.endpoint.contentType));
            codeArea.setText(selectedEndpointCard.endpoint.resource, FormatterFactory.getFormatter(contentTypeBox.getValue()),
                    HighlighterFactory.getHighlighter(contentTypeBox.getValue()));
            setResponseCode(selectedEndpointCard.endpoint.responseCode);
            endpointDetailsBox.setDisable(false);
        }
    }
}

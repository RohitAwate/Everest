/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rohitawate.restaurant.homewindow;

import com.rohitawate.restaurant.models.DashboardState;
import com.rohitawate.restaurant.models.requests.DataDispatchRequest;
import com.rohitawate.restaurant.util.Services;
import com.rohitawate.restaurant.util.themes.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ResourceBundle;

/*
    Raw and Binary tabs are embedded in
    this FXML itself.
    URL encoded and Form tabs have special FXMLs.
 */
public class BodyTabController implements Initializable {
    @FXML
    private TabPane bodyTabPane;
    @FXML
    private ComboBox<String> rawInputTypeBox;
    @FXML
    private TextArea rawInputArea;
    @FXML
    private Tab rawTab, binaryTab, formTab, urlTab;
    @FXML
    private TextField filePathField;

    private FormDataTabController formDataTabController;
    private URLTabController urlTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rawInputTypeBox.getItems().addAll("PLAIN TEXT", "JSON", "XML", "HTML");
        rawInputTypeBox.getSelectionModel().select(0);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/FormDataTab.fxml"));
            formTab.setContent(loader.load());
            formDataTabController = loader.getController();

            loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/URLTab.fxml"));
            Parent formTabContent = loader.load();
            ThemeManager.setTheme(formTabContent);
            urlTab.setContent(formTabContent);
            urlTabController = loader.getController();
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not load URL tab.", e, LocalDateTime.now());
        }
    }

    /**
     * Returns a RestaurantRequest object initialized with the request body.
     */
    public DataDispatchRequest getBasicRequest(String requestType) {
        DataDispatchRequest request = new DataDispatchRequest(requestType);

        // Raw and binary types get saved in Body.
        // Form and URL encoded types use tuple objects
        if (rawTab.isSelected()) {
            String contentType;
            switch (rawInputTypeBox.getValue()) {
                case "PLAIN TEXT":
                    contentType = MediaType.TEXT_PLAIN;
                    break;
                case "JSON":
                    contentType = MediaType.APPLICATION_JSON;
                    break;
                case "XML":
                    contentType = MediaType.APPLICATION_XML;
                    break;
                case "HTML":
                    contentType = MediaType.TEXT_HTML;
                    break;
                default:
                    contentType = MediaType.TEXT_PLAIN;
            }
            request.setContentType(contentType);
            request.setBody(rawInputArea.getText());
        } else if (formTab.isSelected()) {
            request.setStringTuples(formDataTabController.getStringTuples());
            request.setFileTuples(formDataTabController.getFileTuples());
            request.setContentType(MediaType.MULTIPART_FORM_DATA);
        } else if (binaryTab.isSelected()) {
            request.setBody(filePathField.getText());
            request.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } else if (urlTab.isSelected()) {
            request.setStringTuples(urlTabController.getStringTuples());
            request.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        return request;
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a binary file to add to request...");
        Window dashboardWindow = filePathField.getScene().getWindow();
        String filePath;
        try {
            filePath = fileChooser.showOpenDialog(dashboardWindow).getAbsolutePath();
        } catch (NullPointerException NPE) {
            filePath = "";
        }
        filePathField.setText(filePath);
    }

    public void setState(DashboardState dashboardState) {
        try {
            switch (dashboardState.getContentType()) {
                case MediaType.TEXT_PLAIN:
                    rawInputArea.setText(dashboardState.getBody());
                    rawInputTypeBox.getSelectionModel().select("PLAIN TEXT");
                    bodyTabPane.getSelectionModel().select(rawTab);
                    break;
                case MediaType.APPLICATION_JSON:
                    rawInputArea.setText(dashboardState.getBody());
                    rawInputTypeBox.getSelectionModel().select("JSON");
                    bodyTabPane.getSelectionModel().select(rawTab);
                    break;
                case MediaType.APPLICATION_XML:
                    rawInputArea.setText(dashboardState.getBody());
                    rawInputTypeBox.getSelectionModel().select("XML");
                    bodyTabPane.getSelectionModel().select(rawTab);
                    break;
                case MediaType.TEXT_HTML:
                    rawInputArea.setText(dashboardState.getBody());
                    rawInputTypeBox.getSelectionModel().select("HTML");
                    bodyTabPane.getSelectionModel().select(rawTab);
                    break;
                case MediaType.MULTIPART_FORM_DATA:
                    // For file tuples
                    for (Map.Entry entry : dashboardState.getFileTuples().entrySet())
                        formDataTabController.addFileField(entry.getKey().toString(), entry.getValue().toString());

                    // For string tuples
                    for (Map.Entry entry : dashboardState.getStringTuples().entrySet())
                        formDataTabController.addStringField(entry.getKey().toString(), entry.getValue().toString());
                    bodyTabPane.getSelectionModel().select(formTab);
                    break;
                case MediaType.APPLICATION_OCTET_STREAM:
                    filePathField.setText(dashboardState.getBody());
                    bodyTabPane.getSelectionModel().select(binaryTab);
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    for (Map.Entry entry : dashboardState.getStringTuples().entrySet())
                        urlTabController.addField(entry.getKey().toString(), entry.getValue().toString());
                    bodyTabPane.getSelectionModel().select(urlTab);
                    break;
            }
        } catch (NullPointerException NPE) {
            Services.loggingService.logInfo("Dashboard loaded with blank request body.", LocalDateTime.now());
        }
    }
}

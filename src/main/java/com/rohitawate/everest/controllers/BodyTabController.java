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

package com.rohitawate.everest.controllers;

import com.rohitawate.everest.controllers.codearea.EverestCodeArea;
import com.rohitawate.everest.controllers.codearea.highlighters.HighlighterFactory;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.ThemeManager;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.state.FieldState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/*
    Raw and Binary tabs are embedded in
    this FXML itself.
    URL encoded and Form tabs have special FXMLs.
 */
public class BodyTabController implements Initializable {
    @FXML
    ComboBox<String> rawInputTypeBox;
    @FXML
    Tab rawTab, binaryTab, formTab, urlTab;
    @FXML
    TextField filePathField;
    @FXML
    private TabPane bodyTabPane;
    @FXML
    private VBox rawVBox;

    EverestCodeArea rawInputArea;
    FormDataTabController formDataTabController;
    URLTabController urlTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rawInputTypeBox.getItems().addAll(
                HTTPConstants.JSON,
                HTTPConstants.XML,
                HTTPConstants.HTML,
                HTTPConstants.PLAIN_TEXT
        );
        rawInputTypeBox.getSelectionModel().select(0);

        rawInputArea = new EverestCodeArea();
        ThemeManager.setSyntaxTheme(rawInputArea);
        rawInputArea.setPrefHeight(1500);   // Hack to make the EverestCodeArea stretch with the Composer
        rawVBox.getChildren().add(new VirtualizedScrollPane<>(rawInputArea));

        rawInputTypeBox.valueProperty().addListener(change -> {
            String type = rawInputTypeBox.getValue();

            if (type.equals(HTTPConstants.PLAIN_TEXT)) {
                rawInputArea.setHighlighter(HighlighterFactory.getHighlighter(type));
                return;
            }

            rawInputArea.setHighlighter(HighlighterFactory.getHighlighter(type));
        });

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
            LoggingService.logSevere("Could not load URL tab.", e, LocalDateTime.now());
        }
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a binary file to add to the request");
        Window dashboardWindow = filePathField.getScene().getWindow();
        String filePath;

        try {
            filePath = fileChooser.showOpenDialog(dashboardWindow).getAbsolutePath();
        } catch (NullPointerException NPE) {
            filePath = "";
        }

        filePathField.setText(filePath);
    }

    @FXML
    private void clearFilePath() {
        filePathField.clear();
    }

    public ComposerState getState() {
        ComposerState state = new ComposerState();

        state.urlStringTuples = urlTabController.getFieldStates();
        state.formStringTuples = formDataTabController.getStringFieldStates();
        state.formFileTuples = formDataTabController.getFileFieldStates();
        state.binaryFilePath = filePathField.getText();

        state.rawBody = rawInputArea.getText();
        state.rawBodyBoxValue = HTTPConstants.getComplexContentType(rawInputTypeBox.getValue());

        switch (bodyTabPane.getSelectionModel().getSelectedIndex()) {
            case 1:
                state.contentType = MediaType.APPLICATION_FORM_URLENCODED;
                break;
            case 2:
                state.contentType = state.rawBodyBoxValue;
                break;
            case 3:
                state.contentType = MediaType.APPLICATION_OCTET_STREAM;
                break;
            default:
                state.contentType = MediaType.MULTIPART_FORM_DATA;
        }

        return state;
    }

    public void setState(ComposerState state) {
        // Adding URL tab's tuples
        if (state.urlStringTuples != null) {
            for (FieldState fieldState : state.urlStringTuples)
                urlTabController.addField(fieldState);
        }

        // Adding Form tab's string tuples
        if (state.formStringTuples != null) {
            for (FieldState fieldState : state.formStringTuples)
                formDataTabController.addStringField(fieldState);
        }

        // Adding Form tab's file tuples
        if (state.formFileTuples != null)
            for (FieldState fieldState : state.formFileTuples)
                formDataTabController.addFileField(fieldState);

        setRawTab(state);
        filePathField.setText(state.binaryFilePath);

        int tab = 0;
        if (state.contentType != null) {
            switch (state.contentType) {
                case MediaType.APPLICATION_OCTET_STREAM:
                    tab = 3;
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED:
                    tab = 1;
                    break;
                case MediaType.APPLICATION_JSON:
                case MediaType.APPLICATION_XML:
                case MediaType.TEXT_HTML:
                case MediaType.TEXT_PLAIN:
                    tab = 2;
                    break;
                default:
                    tab = 0;
            }
        }

        bodyTabPane.getSelectionModel().select(tab);
    }

    void reset() {
        urlTabController.clear();
        formDataTabController.clear();
        rawInputArea.clear();
        rawInputTypeBox.setValue(HTTPConstants.PLAIN_TEXT);
        filePathField.clear();
    }

    private void setRawTab(ComposerState state) {
        if (state.rawBodyBoxValue != null && state.rawBody != null) {
            String simplifiedContentType = HTTPConstants.getSimpleContentType(state.rawBodyBoxValue);
            rawInputTypeBox.setValue(simplifiedContentType);
            rawInputArea.setText(state.rawBody, HighlighterFactory.getHighlighter(simplifiedContentType));
        } else {
            rawInputTypeBox.setValue(HTTPConstants.PLAIN_TEXT);
            rawInputArea.setHighlighter(HighlighterFactory.getHighlighter(HTTPConstants.PLAIN_TEXT));
        }
    }
}

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

import com.rohitawate.restaurant.util.themes.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class FormDataTabController implements Initializable {
    @FXML
    private VBox headersBox;

    private List<StringKeyValueFieldController> stringControllers;
    private List<FileKeyValueFieldController> fileControllers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stringControllers = new ArrayList<>();
        fileControllers = new ArrayList<>();

        addFileField();
        addStringField();
    }

    @FXML
    private void addFileField() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/FileKeyValueField.fxml"));
            Parent headerField = loader.load();
            ThemeManager.setTheme(headerField);
            FileKeyValueFieldController controller = loader.getController();
            fileControllers.add(controller);
            headersBox.getChildren().add(headerField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addStringField() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/StringKeyValueField.fxml"));
            Parent headerField = loader.load();
            StringKeyValueFieldController controller = loader.getController();
            stringControllers.add(controller);
            headersBox.getChildren().add(headerField);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getStringTuples() {
        HashMap<String, String> headers = new HashMap<>();
        for (StringKeyValueFieldController controller : stringControllers) {
            if (controller.isChecked())
                headers.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }
        return headers;
    }

    public HashMap<String, String> getFileTuples() {
        HashMap<String, String> headers = new HashMap<>();
        for (FileKeyValueFieldController controller : fileControllers) {
            if (controller.isChecked())
                headers.put(controller.getHeader().getKey(), controller.getHeader().getValue());
        }
        return headers;
    }
}

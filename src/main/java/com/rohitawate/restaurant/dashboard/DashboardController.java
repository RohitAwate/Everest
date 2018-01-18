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
package com.rohitawate.restaurant.dashboard;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Rohit Awate
 */
public class DashboardController implements Initializable {

	@FXML
	private TextField addressField;
	@FXML
	private ComboBox<String> httpMethodBox;
	@FXML
	private TextArea responseArea;

	private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		httpMethodBox.getItems().addAll(httpMethods);
		httpMethodBox.setValue("GET");
		responseArea.wrapTextProperty().set(true);
	}

	@FXML
	private void sendAction() {
		
	}
}

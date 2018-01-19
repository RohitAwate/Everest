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

import com.jfoenix.controls.JFXSnackbar;
import com.rohitawate.restaurant.requests.RequestManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author Rohit Awate
 */
public class DashboardController implements Initializable {
	@FXML
    private BorderPane dashboard;
	@FXML
	private TextField addressField;
	@FXML
	private ComboBox<String> httpMethodBox;
	@FXML
	private TextArea responseArea;

	private JFXSnackbar snackBar;
	private final String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
	private RequestManager requestManager;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		httpMethodBox.getItems().addAll(httpMethods);
		httpMethodBox.setValue("GET");
		responseArea.wrapTextProperty().set(true);
		
		requestManager = new RequestManager();
		snackBar = new JFXSnackbar(dashboard);
	}

	@FXML
	private void sendAction() {
		try {
			String address = addressField.getText();
			if (address.equals("")) {
				snackBar.show("Please enter a valid address", 7000);
				return;
			}
			String response = "";
			URL url = new URL(address);
			switch (httpMethodBox.getValue()) {
				case "GET":
					response = requestManager.get(url);
					break;
			}
			responseArea.setText(response);
		} catch (MalformedURLException ex) {
			snackBar.show("Invalid URL. Please verify and try again.", 7000);
		} catch (IOException ex) {
			snackBar.show("Server did not respond", 7000);
		}
		
	}
}

/*
 * Copyright 2019 Rohit Awate.
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

package com.rohitawate.everest.controllers.auth;

import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.controllers.auth.oauth2.AuthorizationCodeController;
import com.rohitawate.everest.controllers.auth.oauth2.ImplicitController;
import com.rohitawate.everest.state.auth.OAuth2ControllerState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class OAuth2TabController implements Initializable {
    @FXML
    private TabPane oauth2TabPane;
    @FXML
    private Tab codeTab, implicitTab, ropcTab, clientCredentialsTab;

    private AuthorizationCodeController codeController;
    private ImplicitController implicitController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FXMLLoader codeLoader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/oauth2/AuthorizationCode.fxml"));
            Parent codeFXML = codeLoader.load();
            codeTab.setContent(codeFXML);
            codeController = codeLoader.getController();

            FXMLLoader implicitLoader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/oauth2/Implicit.fxml"));
            Parent implicitFXML = implicitLoader.load();
            implicitTab.setContent(implicitFXML);
            implicitController = implicitLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OAuth2ControllerState getState() {
        return new OAuth2ControllerState(
                codeController.getState(),
                implicitController.getState()
        );
    }

    public void setState(OAuth2ControllerState state) {
        if (state != null) {
            codeController.setState(state.codeState);
            implicitController.setState(state.implicitState);
        }
    }

    public void reset() {
        codeController.reset();
        implicitController.reset();
    }

    AuthProvider getAuthProvider() {
        switch (oauth2TabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                return codeController.getAuthProvider();
            case 1:
                return implicitController.getAuthProvider();
            default:
                return null;
        }
    }
}

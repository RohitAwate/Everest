package com.rohitawate.everest.controllers.auth;

import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.controllers.auth.oauth2.AuthorizationCodeController;
import com.rohitawate.everest.state.OAuth2State;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/oauth2/AuthorizationCode.fxml"));
            Parent codeFXML = loader.load();
            codeTab.setContent(codeFXML);
            codeController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OAuth2State getState() {
        return new OAuth2State(codeController.getState());
    }

    public void setState(OAuth2State state) {
        if (state != null) {
            codeController.setState(state.codeState);
        }
    }

    public void reset() {
        codeController.reset();
    }

    AuthProvider getAuthProvider() {
        switch (oauth2TabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                return codeController.getAuthProvider();
            default:
                return null;
        }
    }
}

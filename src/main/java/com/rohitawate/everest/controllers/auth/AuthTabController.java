package com.rohitawate.everest.controllers.auth;

import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.auth.BasicAuthProvider;
import com.rohitawate.everest.state.ComposerState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthTabController implements Initializable {
    @FXML
    private TabPane authTabPane;
    @FXML
    private Tab basicTab, digestTab;

    private BasicAuthController basicController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/BasicAuth.fxml"));
            Parent basicFXML = loader.load();
            basicTab.setContent(basicFXML);
            basicController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthProvider getAuthProvider() {
        switch (authTabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                return new BasicAuthProvider(
                        basicController.getUsername(), basicController.getPassword(), basicController.isSelected());
            default:
                return null;
        }
    }

    public void getState(ComposerState state) {
        state.basicUsername = basicController.getUsername();
        state.basicPassword = basicController.getPassword();
        state.basicAuthEnabled = basicController.isSelected();
    }

    public void setState(ComposerState state) {
        basicController.setState(state.basicUsername, state.basicPassword, state.basicAuthEnabled);
    }
}

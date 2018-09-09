package com.rohitawate.everest.controllers.auth;

import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.auth.BasicAuthProvider;
import com.rohitawate.everest.auth.DigestAuthProvider;
import com.rohitawate.everest.controllers.DashboardController;
import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.sync.DataManager;
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
    private Tab basicTab, digestTab, oauth2Tab;

    private SimpleAuthController basicController, digestController;
    private OAuth2TabController oAuth2Controller;

    private DashboardController dashboard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FXMLLoader basicLoader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/SimpleAuth.fxml"));
            Parent basicFXML = basicLoader.load();
            basicTab.setContent(basicFXML);
            basicController = basicLoader.getController();

            FXMLLoader digestLoader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/SimpleAuth.fxml"));
            Parent digestFXML = digestLoader.load();
            digestTab.setContent(digestFXML);
            digestController = digestLoader.getController();

            FXMLLoader oauth2Loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/auth/OAuth2.fxml"));
            Parent oauth2FXML = oauth2Loader.load();
            oauth2Tab.setContent(oauth2FXML);
            oAuth2Controller = oauth2Loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthProvider getAuthProvider() {
        switch (authTabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                return new BasicAuthProvider(
                        basicController.getUsername(),
                        basicController.getPassword(),
                        basicController.isSelected()
                );
            case 1:
                return new DigestAuthProvider(
                        dashboard.getAddress(),
                        dashboard.getHttpMethod(),
                        digestController.getUsername(),
                        digestController.getPassword(),
                        digestController.isSelected()
                );
            default:
                return null;
        }
    }

    public void getState(ComposerState state) {
        switch (authTabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                state.authMethod = DataManager.BASIC;
                break;
            case 1:
                state.authMethod = DataManager.DIGEST;
                break;
        }

        state.basicAuthState = basicController.getState();
        state.digestAuthState = digestController.getState();
        state.oAuth2State = oAuth2Controller.getState();
    }

    public void setState(ComposerState state) {
        basicController.setState(state.basicAuthState);
        digestController.setState(state.digestAuthState);
        oAuth2Controller.setState(state.oAuth2State);

        if (state.authMethod == null) {
            authTabPane.getSelectionModel().select(0);
            return;
        }

        switch (state.authMethod) {
            case DataManager.BASIC:
                authTabPane.getSelectionModel().select(0);
                break;
            case DataManager.DIGEST:
                authTabPane.getSelectionModel().select(1);
        }
    }

    public void setDashboard(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    public void reset() {
        basicController.reset();
        digestController.reset();
        oAuth2Controller.reset();
    }
}

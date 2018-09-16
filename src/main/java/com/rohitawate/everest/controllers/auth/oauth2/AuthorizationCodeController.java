package com.rohitawate.everest.controllers.auth.oauth2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.rohitawate.everest.auth.oauth2.AccessToken;
import com.rohitawate.everest.auth.oauth2.code.AuthorizationCodeProvider;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.NoAuthorizationGrantException;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.notifications.NotificationsManager;
import com.rohitawate.everest.state.AuthorizationCodeState;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static com.rohitawate.everest.auth.oauth2.code.AuthorizationCodeProvider.CaptureMethod.BROWSER;
import static com.rohitawate.everest.auth.oauth2.code.AuthorizationCodeProvider.CaptureMethod.WEB_VIEW;

public class AuthorizationCodeController implements Initializable {
    @FXML
    private JFXCheckBox enabled;
    @FXML
    private ComboBox<String> captureMethodBox;
    @FXML
    private JFXTextField authURLField, tokenURLField, redirectURLField,
            clientIDField, clientSecretField, scopeField, stateField,
            headerPrefixField, accessTokenField, refreshTokenField;
    @FXML
    private Label expiryLabel;
    @FXML
    private JFXButton refreshTokenButton;

    // TODO: Re-use provider with setters and getters
    private AuthorizationCodeProvider provider;
    private int tokenExpiry;

    public static final String CAPTURE_METHOD_INTEGRATED = "Integrated WebView";
    public static final String CAPTURE_METHOD_BROWSER = "System Browser";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        captureMethodBox.getItems().addAll(CAPTURE_METHOD_INTEGRATED, CAPTURE_METHOD_BROWSER);
        captureMethodBox.setValue(CAPTURE_METHOD_BROWSER);
        refreshTokenButton.setOnAction(this::refreshToken);
        expiryLabel.setVisible(false);
    }

    private void refreshToken(ActionEvent actionEvent) {
        refreshTokenButton.setDisable(true);
        try {
            provider = new AuthorizationCodeProvider(
                    authURLField.getText(), tokenURLField.getText(),
                    clientIDField.getText(), clientSecretField.getText(),
                    redirectURLField.getText().isEmpty() ? null : redirectURLField.getText(),
                    scopeField.getText(), enabled.isSelected(),
                    captureMethodBox.getValue().equals(CAPTURE_METHOD_INTEGRATED) ? WEB_VIEW : BROWSER
            );

            AccessToken accessToken = provider.getAccessToken();
            if (accessToken != null) {
                accessTokenField.clear();
                refreshTokenField.clear();

                accessTokenField.setText(accessToken.accessToken);

                if (accessToken.refreshToken != null) {
                    refreshTokenField.setText(accessToken.refreshToken);
                }

                setExpiryLabel(accessToken.expiresIn);
            }
        } catch (Exception e) {
            String errorMessage;
            if (e.getClass().equals(AuthWindowClosedException.class)) {
                errorMessage = "Authorization window closed.";
            } else if (e.getClass().equals(NoAuthorizationGrantException.class)) {
                errorMessage = "Grant denied by authorization endpoint.";
            } else if (e.getClass().equals(AccessTokenDeniedException.class)) {
                errorMessage = "Access token denied by token endpoint.";
            } else if (e.getClass().equals(MalformedURLException.class)) {
                errorMessage = "Invalid URL(s).";
            } else {
                errorMessage = "Could not refresh OAuth 2.0 Authorization Code tokens.";
            }

            NotificationsManager.push(errorMessage, 5000);
            LoggingService.logWarning(errorMessage, e, LocalDateTime.now());
        } finally {
            refreshTokenButton.setDisable(false);
        }
    }

    public AuthorizationCodeState getState() {
        return new AuthorizationCodeState(captureMethodBox.getValue(), authURLField.getText(), tokenURLField.getText(), redirectURLField.getText(),
                clientIDField.getText(), clientSecretField.getText(), scopeField.getText(), stateField.getText(),
                headerPrefixField.getText(), accessTokenField.getText(), refreshTokenField.getText(), tokenExpiry, enabled.isSelected());
    }

    public void setState(AuthorizationCodeState state) {
        if (state != null) {
            captureMethodBox.setValue(state.grantCaptureMethod);
            authURLField.setText(state.authURL);
            tokenURLField.setText(state.accessTokenURL);
            redirectURLField.setText(state.redirectURL);
            clientIDField.setText(state.clientID);
            clientSecretField.setText(state.clientSecret);
            scopeField.setText(state.scope);
            stateField.setText(state.state);
            headerPrefixField.setText(state.headerPrefix);
            accessTokenField.setText(state.accessToken);
            refreshTokenField.setText(state.refreshToken);
            setExpiryLabel(state.tokenExpiry);
            enabled.setSelected(state.enabled);
        }
    }

    private void setExpiryLabel(int tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
        expiryLabel.setVisible(true);

        if (tokenExpiry != 0) {
            expiryLabel.setText("Expires in " + tokenExpiry + "s");
        } else {
            expiryLabel.setText("Never expires.");
        }
    }

    public void reset() {
        authURLField.clear();
        tokenURLField.clear();
        redirectURLField.clear();
        clientIDField.clear();
        clientSecretField.clear();
        scopeField.clear();
        stateField.clear();
        headerPrefixField.clear();
        accessTokenField.clear();
        refreshTokenField.clear();
        tokenExpiry = 0;
        expiryLabel.setVisible(false);
        enabled.setSelected(false);
    }
}

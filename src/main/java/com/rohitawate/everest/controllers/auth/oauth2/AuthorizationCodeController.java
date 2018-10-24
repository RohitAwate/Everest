package com.rohitawate.everest.controllers.auth.oauth2;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.rohitawate.everest.auth.AuthProvider;
import com.rohitawate.everest.auth.oauth2.AccessToken;
import com.rohitawate.everest.auth.oauth2.code.AuthorizationCodeProvider;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.NoAuthorizationGrantException;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.notifications.NotificationsManager;
import com.rohitawate.everest.state.AuthorizationCodeState;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    private AuthorizationCodeProvider provider;
    private AccessToken accessToken;

    public class CaptureMethod {
        public final static String WEB_VIEW = "Integrated WebView";
        public final static String BROWSER = "System Browser";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        captureMethodBox.getItems().addAll(CaptureMethod.BROWSER, CaptureMethod.WEB_VIEW);
        captureMethodBox.setValue(CaptureMethod.BROWSER);
        refreshTokenButton.setOnAction(this::refreshToken);
        expiryLabel.setVisible(false);

        expiryLabel.setOnMouseClicked(e -> setExpiryLabel());
        expiryLabel.setTooltip(new Tooltip("Click to check expiry status"));
    }

    private void refreshToken(ActionEvent actionEvent) {
        /*
            Opening a system browser window need not be done on the JavaFX Application Thread.
            Hence, this is performed on a separate thread.

            However, a WebView can only be opened on the JavaFX Application Thread hence it is
            NOT performed on some other thread.
         */
        if (captureMethodBox.getValue().equals(CaptureMethod.BROWSER)) {
            ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactory() {
                /*
                    Custom ThreadFactory which produces daemon threads so that
                    the CaptureServer doesn't keep the JVM from exiting.
                 */
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread newThread = new Thread(runnable);
                    newThread.setDaemon(true);
                    return newThread;
                }
            });
            service.submit(new TokenFetcher());
        } else {
            TokenFetcher fetcher = new TokenFetcher();
            try {
                accessToken = fetcher.call();
                onRefreshSucceeded();
            } catch (Exception e) {
                onRefreshFailed(e);
            }
        }
    }

    public AuthorizationCodeState getState() {
        if (this.accessToken != null) {
            /*
                Setting these values again before adding the AccessToken to the AuthCodeState
                since they can be manually changed in the UI.
             */
            this.accessToken.setAccessToken(accessTokenField.getText());
            this.accessToken.setRefreshToken(refreshTokenField.getText());
        }

        return new AuthorizationCodeState(captureMethodBox.getValue(), authURLField.getText(), tokenURLField.getText(), redirectURLField.getText(),
                clientIDField.getText(), clientSecretField.getText(), scopeField.getText(), stateField.getText(),
                headerPrefixField.getText(), this.accessToken, enabled.isSelected());
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

            if (state.accessToken != null) {
                accessToken = state.accessToken;
                accessTokenField.setText(state.accessToken.getAccessToken());
                refreshTokenField.setText(state.accessToken.getRefreshToken());
                setExpiryLabel();
            }

            enabled.setSelected(state.enabled);

            try {
                provider = new AuthorizationCodeProvider(getState());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setExpiryLabel() {
        if (accessToken != null) {
            expiryLabel.setVisible(true);

            if (accessToken.getExpiresIn() == 0) {
                expiryLabel.setText("Never expires.");
            } else {
                long timeToExpiry = accessToken.getTimeToExpiry();
                if (timeToExpiry < 0) {
                    expiryLabel.setText("Token expired.");
                } else {
                    expiryLabel.setText("Expires in " + timeToExpiry + "s");
                }
            }
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
        expiryLabel.setVisible(false);
        enabled.setSelected(false);
    }

    public AuthProvider getAuthProvider() {
        try {
            provider.setState(getState());
        } catch (MalformedURLException e) {
            NotificationsManager.push("Invalid URL: " + e.getMessage(), 7000);
        }

        if (accessTokenField.getText().isEmpty() && enabled.isSelected()) {
            refreshToken(null);
        }

        return provider;
    }

    private void onRefreshSucceeded() {
        accessTokenField.clear();
        refreshTokenField.clear();

        accessTokenField.setText(accessToken.getAccessToken());

        if (accessToken.getRefreshToken() != null) {
            refreshTokenField.setText(accessToken.getRefreshToken());
        }

        setExpiryLabel();
    }

    private void onRefreshFailed(Throwable exception) {
        String errorMessage;
        if (exception.getClass().equals(AuthWindowClosedException.class)) {
            errorMessage = "Authorization window closed.";
        } else if (exception.getClass().equals(NoAuthorizationGrantException.class)) {
            errorMessage = "Grant denied by authorization endpoint.";
        } else if (exception.getClass().equals(AccessTokenDeniedException.class)) {
            errorMessage = "Access token denied by token endpoint.";
        } else if (exception.getClass().equals(MalformedURLException.class)) {
            errorMessage = "Invalid URL(s).";
        } else {
            errorMessage = "Could not refresh OAuth 2.0 Authorization Code tokens.";
        }

        NotificationsManager.push(errorMessage, 10000);
        LoggingService.logWarning(errorMessage, (Exception) exception, LocalDateTime.now());
    }

    private class TokenFetcher extends Task<AccessToken> {
        @Override
        protected AccessToken call() throws Exception {
            AuthorizationCodeState state = new AuthorizationCodeState(captureMethodBox.getValue(), authURLField.getText(), tokenURLField.getText(), redirectURLField.getText(),
                    clientIDField.getText(), clientSecretField.getText(), scopeField.getText(), stateField.getText(),
                    headerPrefixField.getText(), null, enabled.isSelected());

            provider = new AuthorizationCodeProvider(state);
            return provider.getAccessToken();
        }

        @Override
        protected void succeeded() {
            accessToken = getValue();
            onRefreshSucceeded();
        }

        @Override
        protected void failed() {
            Throwable exception = getException();
            onRefreshFailed(exception);
        }
    }
}

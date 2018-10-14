package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.EverestUtilities;
import com.sun.webkit.network.CookieManager;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Opens the OAuth 2.0 authorization window in a JavaFX WebView
 * and captures the authorization grant by detecting redirects.
 */
public class WebViewCapturer implements AuthorizationGrantCapturer {
    private String authURL;
    private String callbackURL;

    private Stage authStage;
    private WebView webView;
    private WebEngine engine;

    private String grant;

    WebViewCapturer(String finalGrantURL, String callbackURL) {
        CookieHandler.setDefault(new CookieManager());
        this.authURL = finalGrantURL;
        this.callbackURL = callbackURL;
        this.webView = new WebView();
        this.engine = webView.getEngine();
    }

    @Override
    public String getAuthorizationGrant() throws AuthWindowClosedException {
        engine.locationProperty().addListener((obs, oldVal, newVal) -> {
            try {
                HashMap<String, String> urlParams = EverestUtilities.parseParameters(new URL(newVal));
                if (urlParams != null && urlParams.containsKey("code")) {
                    grant = urlParams.get("code");
                    authStage.close();
                }
            } catch (MalformedURLException e) {
                LoggingService.logWarning("Invalid URL while authorizing application.", e, LocalDateTime.now());
            }
        });

        authStage = new Stage();
        authStage.setScene(new Scene(webView));
        authStage.titleProperty().bind(engine.titleProperty());
        engine.load(authURL);
        authStage.showAndWait();

        if (grant == null)
            throw new AuthWindowClosedException();
        return grant;
    }
}

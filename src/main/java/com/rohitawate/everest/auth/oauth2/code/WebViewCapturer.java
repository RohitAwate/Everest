package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebViewCapturer implements AuthorizationGrantCapturer {
    private String authURL;
    private String callbackURL;

    private Stage authStage;
    private WebView webView;
    private WebEngine engine;

    private String grant;

    WebViewCapturer(String finalGrantURL, String callbackURL) {
        this.authURL = finalGrantURL;
        this.callbackURL = callbackURL;
        this.webView = new WebView();
        this.engine = webView.getEngine();
    }

    @Override
    public String getAuthorizationGrant() throws AuthWindowClosedException {
        Pattern pattern;
        if (callbackURL != null) {
            pattern = Pattern.compile(callbackURL + ".*code=(?<GRANT>.*)&?");
        } else {
            pattern = Pattern.compile(".*code=(?<GRANT>.*)&?");
        }

        engine.locationProperty().addListener((obs, oldVal, newVal) -> {
            Matcher matcher = pattern.matcher(newVal);

            if (matcher.matches()) {
                grant = matcher.group("GRANT");
                authStage.close();
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

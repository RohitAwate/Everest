package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.logging.LoggingService;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebViewCapturer implements AuthorizationGrantCapturer {
    private String authURL;

    private Stage authStage;
    private WebView webView;
    private WebEngine engine;

    private String grant;

    public WebViewCapturer(String finalGrantURL) {
        this.authURL = finalGrantURL;
        this.webView = new WebView();
        this.engine = webView.getEngine();
    }

    @Override
    public String getAuthorizationGrant() {
        Pattern pattern = Pattern.compile(".*code=(?<GRANT>.*)&?");
        engine.locationProperty().addListener((obs, oldVal, newVal) -> {
            Matcher matcher = pattern.matcher(newVal);

            if (matcher.matches()) {
                grant = matcher.group("GRANT");
                authStage.close();
            } else {
                LoggingService.logInfo("Redirected to " + newVal, LocalDateTime.now());
            }
        });

        authStage = new Stage();
        authStage.setScene(new Scene(webView));
        authStage.titleProperty().bind(engine.titleProperty());
        engine.load(authURL);
        authStage.showAndWait();

        return grant;
    }
}

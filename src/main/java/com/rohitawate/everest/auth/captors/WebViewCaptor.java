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

package com.rohitawate.everest.auth.captors;

import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.tokens.ImplicitToken;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Opens the OAuth 2.0 authorization window in a JavaFX WebView
 * and captures the authorization grant by detecting redirects.
 */
public class WebViewCaptor implements UserAgentCaptor {
    private String authURL;

    private Stage authStage;
    private WebView webView;
    private WebEngine engine;

    private String grant;
    private ImplicitToken token;

    private static final String USER_AGENT_STRING = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

    public WebViewCaptor(String authURL) {
        this.authURL = authURL;
        this.webView = new WebView();
        this.engine = webView.getEngine();
        this.engine.setUserAgent(USER_AGENT_STRING);
    }

    @Override
    public String getAuthorizationGrant() throws AuthWindowClosedException {
        engine.locationProperty().addListener((obs, oldVal, newVal) -> {
            String captureKey = "code";
            try {
                HashMap<String, String> urlParams = EverestUtilities.parseParameters(new URL(newVal), "\\?");
                if (urlParams != null && urlParams.containsKey(captureKey)) {
                    grant = urlParams.get(captureKey);
                    authStage.close();
                }
            } catch (MalformedURLException e) {
                Logger.warning("Invalid URL while authorizing application.", e);
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

    public ImplicitToken getImplicitToken() throws AuthWindowClosedException {
        engine.locationProperty().addListener((obs, oldVal, newVal) -> {
            try {
                HashMap<String, String> urlParams = EverestUtilities.parseParameters(new URL(newVal), "#");
                if (urlParams != null && urlParams.containsKey("access_token")) {
                    token = new ImplicitToken();
                    token.setAccessToken(urlParams.get("access_token"));
                    token.setExpiresIn(Integer.parseInt(urlParams.get("expires_in")));
                    token.setTokenType(urlParams.get("token_type"));
                    token.setState(urlParams.get("state"));
                    token.setScope(urlParams.get("scope"));

                    authStage.close();
                }
            } catch (MalformedURLException e) {
                Logger.warning("Invalid URL while authorizing application.", e);
            }
        });

        authStage = new Stage();
        authStage.setScene(new Scene(webView));
        authStage.titleProperty().bind(engine.titleProperty());
        engine.load(authURL);
        authStage.showAndWait();

        if (token == null)
            throw new AuthWindowClosedException();

        return token;
    }
}

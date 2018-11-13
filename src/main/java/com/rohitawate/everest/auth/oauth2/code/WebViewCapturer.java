/*
 * Copyright 2018 Rohit Awate.
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

package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
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
public class WebViewCapturer implements AuthorizationGrantCapturer {
    private String authURL;

    private Stage authStage;
    private WebView webView;
    private WebEngine engine;

    private String grant;

    WebViewCapturer(String authURL) {
        this.authURL = authURL;
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
}

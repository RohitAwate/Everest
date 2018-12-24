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

import com.rohitawate.everest.auth.oauth2.AccessToken;
import com.rohitawate.everest.auth.oauth2.OAuth2Provider;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AccessTokenDeniedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.AuthWindowClosedException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.NoAuthorizationGrantException;
import com.rohitawate.everest.auth.oauth2.code.exceptions.UnknownAccessTokenTypeException;
import com.rohitawate.everest.controllers.auth.oauth2.AuthorizationCodeController;
import com.rohitawate.everest.controllers.auth.oauth2.AuthorizationCodeController.CaptureMethod;
import com.rohitawate.everest.logging.Logger;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.state.auth.AuthorizationCodeState;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Authorization provider for OAuth 2.0's Authorization Code flow.
 * Makes requests to authorization and access token endpoints and returns
 * either the final 'Authorization' header or an AccessToken object.
 */
public class AuthorizationCodeProvider implements OAuth2Provider {
    private URL authURL;
    private URL accessTokenURL;

    private String authGrant;
    private boolean authGrantUsed;

    private String clientID;
    private String clientSecret;
    private URL redirectURL;
    private String scope;
    private String state;
    private String headerPrefix;
    private boolean enabled;

    private String captureMethod;
    private AccessToken accessToken;
    private AuthorizationCodeController controller;

    public AuthorizationCodeProvider(AuthorizationCodeController controller) {
        this.controller = controller;
        this.authGrantUsed = true;
    }

    public void setState(AuthorizationCodeState state) {
        if (state == null) {
            this.accessToken = null;
            return;
        }

        try {
            this.authURL = new URL(state.authURL);
            this.accessTokenURL = new URL(state.accessTokenURL);
        } catch (MalformedURLException e) {
            if (!state.authURL.isEmpty() || !state.accessTokenURL.isEmpty()) {
                Logger.warning("Invalid URL: " + e.getMessage(), e);
            }
        }

        this.clientID = state.clientID;
        this.clientSecret = state.clientSecret;
        this.captureMethod = state.grantCaptureMethod;

        try {
            if (state.redirectURL.isEmpty() || state.grantCaptureMethod.equals(CaptureMethod.BROWSER)) {
                this.redirectURL = new URL(BrowserCapturer.LOCAL_SERVER_URL);
            } else {
                this.redirectURL = new URL(state.redirectURL);
            }
        } catch (MalformedURLException e) {
            if (!state.redirectURL.isEmpty()) {
                Logger.warning("Invalid URL: " + e.getMessage(), e);
            }
        }

        this.scope = state.scope;
        this.state = state.state;

        if (state.headerPrefix == null || state.headerPrefix.isEmpty()) {
            this.headerPrefix = "Bearer";
        } else {
            this.headerPrefix = state.headerPrefix;
        }

        this.accessToken = state.accessToken;
        this.enabled = state.enabled;
    }

    private void fetchAuthorizationGrant() throws Exception {
        if (accessToken.getRefreshToken().isEmpty() && authGrantUsed) {
            StringBuilder grantURLBuilder = new StringBuilder(authURL.toString());
            grantURLBuilder.append("?response_type=code");
            grantURLBuilder.append("&client_id=");
            grantURLBuilder.append(clientID);
            grantURLBuilder.append("&redirect_uri=");
            grantURLBuilder.append(redirectURL.toString());

            if (scope == null || !scope.isEmpty()) {
                grantURLBuilder.append("&scope=");
                grantURLBuilder.append(scope);
            }

            AuthorizationGrantCapturer capturer;
            switch (captureMethod) {
                // TODO: Re-use capturers
                case CaptureMethod.WEB_VIEW:
                    capturer = new WebViewCapturer(grantURLBuilder.toString());
                    break;
                default:
                    capturer = new BrowserCapturer(grantURLBuilder.toString());
            }

            authGrant = capturer.getAuthorizationGrant();
            authGrantUsed = false;
        }
    }

    private void refreshAccessToken()
            throws AccessTokenDeniedException, UnknownAccessTokenTypeException, IOException {
        URL tokenURL = new URL(accessTokenURL.toString());
        StringBuilder tokenURLBuilder = new StringBuilder();
        tokenURLBuilder.append("client_id=");
        tokenURLBuilder.append(clientID);
        tokenURLBuilder.append("&client_secret=");
        tokenURLBuilder.append(clientSecret);
        tokenURLBuilder.append("&grant_type=refresh_token");
        tokenURLBuilder.append("&refresh_token=");
        tokenURLBuilder.append(accessToken.getRefreshToken());
        if (scope != null && !scope.isEmpty()) {
            tokenURLBuilder.append("&scope=");
            tokenURLBuilder.append(scope);
        }

        byte[] body = tokenURLBuilder.toString().getBytes(StandardCharsets.UTF_8);
        AccessTokenRequest tokenRequest = new AccessTokenRequest(tokenURL, body);
        String refreshToken = accessToken.getRefreshToken();
        accessToken = tokenRequest.accessToken;
        accessToken.setRefreshToken(refreshToken);
    }

    private void fetchNewAccessToken()
            throws NoAuthorizationGrantException, IOException, UnknownAccessTokenTypeException, AccessTokenDeniedException {
        if (authGrant == null) {
            throw new NoAuthorizationGrantException(
                    "OAuth 2.0 Authorization Code: Authorization grant not found. Aborting access token fetch."
            );
        }

        URL tokenURL = new URL(accessTokenURL.toString());
        StringBuilder tokenURLBuilder = new StringBuilder();
        tokenURLBuilder.append("client_id=");
        tokenURLBuilder.append(clientID);
        tokenURLBuilder.append("&client_secret=");
        tokenURLBuilder.append(clientSecret);
        tokenURLBuilder.append("&grant_type=authorization_code");
        tokenURLBuilder.append("&code=");
        tokenURLBuilder.append(authGrant);
        tokenURLBuilder.append("&redirect_uri=");
        tokenURLBuilder.append(redirectURL);
        if (scope != null && !scope.isEmpty()) {
            tokenURLBuilder.append("&scope=");
            tokenURLBuilder.append(scope);
        }

        byte[] body = tokenURLBuilder.toString().getBytes(StandardCharsets.UTF_8);
        AccessTokenRequest tokenRequest = new AccessTokenRequest(tokenURL, body);
        accessToken = tokenRequest.accessToken;
        authGrantUsed = true;
    }

    @Override
    public AccessToken getAccessToken() throws Exception {
        fetchAuthorizationGrant();

        if (accessToken.getRefreshToken().isEmpty()) {
            fetchNewAccessToken();
        } else {
            refreshAccessToken();
        }

        // This will display the new AccessToken in the UI
        controller.setAccessToken(accessToken);

        return accessToken;
    }

    @Override
    public String getAuthHeader() throws Exception {
        /*
            Integrated WebView calls will already have been resolved in AuthorizationCodeController,
            hence, they are skipped here.
         */
        if (accessToken.getAccessToken().isEmpty()) {
            /*
                Checking if refreshToken is available. If it is, we can still fetch a new AccessToken and complete
                this request without re-authorizing. (which would require a WebView which cannot be invoked here)
             */
            if (captureMethod.equals(CaptureMethod.WEB_VIEW) && !accessToken.getRefreshToken().isEmpty()) {
                throw new AuthWindowClosedException();
            }

            fetchAuthorizationGrant();
            getAccessToken();
        }

        return headerPrefix + " " + accessToken.getAccessToken();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Makes a request to the access token endpoint, parses the response into an AccessToken object.
     */
    private static class AccessTokenRequest {
        private AccessToken accessToken;
        private URL tokenURL;
        private byte[] body;
        private HttpURLConnection connection;

        /**
         * @param tokenURL The access token endpoint
         * @param body     The application/x-www-form-urlencoded request body
         */
        AccessTokenRequest(URL tokenURL, byte[] body)
                throws IOException, UnknownAccessTokenTypeException, AccessTokenDeniedException {
            this.tokenURL = tokenURL;
            this.body = body;
            openConnection();
            parseTokenResponse();
        }

        private void openConnection() throws IOException {
            connection = (HttpURLConnection) tokenURL.openConnection();
            connection.setRequestMethod(HTTPConstants.POST);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Length", String.valueOf(body.length));
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
            connection.getOutputStream().write(body);
        }

        private void parseTokenResponse()
                throws UnknownAccessTokenTypeException, AccessTokenDeniedException, IOException {
            StringBuilder tokenResponseBuilder = new StringBuilder();
            if (connection.getResponseCode() == 200) {
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext())
                    tokenResponseBuilder.append(scanner.nextLine());
                // Removes the "charset" part
                String contentType = connection.getContentType().split(";")[0];

                switch (contentType) {
                    case MediaType.APPLICATION_JSON:
                        accessToken = EverestUtilities.jsonMapper.readValue(tokenResponseBuilder.toString(), AccessToken.class);
                        break;
                    case MediaType.APPLICATION_FORM_URLENCODED:
                        accessToken = new AccessToken();
                        HashMap<String, String> params =
                                EverestUtilities.parseParameters(new URL(tokenURL + "?" + tokenResponseBuilder.toString()));
                        if (params != null) {
                            params.forEach((key, value) -> {
                                switch (key) {
                                    case "access_token":
                                        accessToken.setAccessToken(value);
                                        break;
                                    case "token_type":
                                        accessToken.setTokenType(value);
                                        break;
                                    case "expires_in":
                                        accessToken.setExpiresIn(Integer.parseInt(value));
                                        break;
                                    case "refresh_token":
                                        accessToken.setRefreshToken(value);
                                        break;
                                    case "scope":
                                        accessToken.setScope(value);
                                        break;
                                }
                            });
                        }
                        break;
                    default:
                        throw new UnknownAccessTokenTypeException("Unknown access token type: " + contentType + "\nBody: " + tokenResponseBuilder.toString());
                }
            } else {
                Scanner scanner = new Scanner(connection.getErrorStream());
                while (scanner.hasNext())
                    tokenResponseBuilder.append(scanner.nextLine());
                throw new AccessTokenDeniedException(tokenResponseBuilder.toString());
            }
        }
    }
}

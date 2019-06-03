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

package com.rohitawate.everest.auth.oauth2;

import com.rohitawate.everest.auth.oauth2.exceptions.UnknownAccessTokenTypeException;
import com.rohitawate.everest.auth.oauth2.tokens.OAuth2Token;
import com.rohitawate.everest.auth.oauth2.tokens.ROPCToken;
import com.rohitawate.everest.controllers.auth.oauth2.ROPCController;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.state.auth.OAuth2FlowState;
import com.rohitawate.everest.state.auth.ROPCState;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

/**
 * Authorization provider for OAuth 2.0's Resource Owner Password Credentials flow.
 * Makes requests to access accessToken endpoints and returns either the final
 * 'Authorization' header or an ROPCToken object.
 */
public class ROPCProvider implements OAuth2Provider {
    private ROPCController controller;

    public ROPCProvider(ROPCController controller) {
        this.controller = controller;
    }

    private void fetchToken() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(controller.getState().accessTokenURL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(generateRequestBody()))
                .build();
        client.sendAsync(request, BodyHandlers.ofString())
                .thenAccept(response -> {
                    ROPCState state = controller.getState();

                    String contentType = response.headers().firstValue("Content-Type").orElse("");
                    try {
                        state.accessToken = parseTokenResponse(response.body(), contentType);
                    } catch (UnknownAccessTokenTypeException | IOException e) {
                        e.printStackTrace();
                    }
                });

    }

    private ROPCToken parseTokenResponse(String response, String contentType)
            throws UnknownAccessTokenTypeException, IOException {
        ROPCToken token;
        switch (contentType) {
            case MediaType.APPLICATION_JSON:
                token = EverestUtilities.jsonMapper.readValue(response, ROPCToken.class);
                break;
            case MediaType.APPLICATION_FORM_URLENCODED:
                token = new ROPCToken();
                String accessTokenURL = controller.getState().accessTokenURL;
                HashMap<String, String> params =
                        EverestUtilities.parseParameters(new URL(accessTokenURL + "?" + response), "\\?");
                if (params != null) {
                    params.forEach((key, value) -> {
                        switch (key) {
                            case "access_token":
                                token.setAccessToken(value);
                                break;
                            case "token_type":
                                token.setTokenType(value);
                                break;
                            case "expires_in":
                                token.setExpiresIn(Integer.parseInt(value));
                                break;
                            case "refresh_token":
                                token.setRefreshToken(value);
                                break;
                            case "scope":
                                token.setScope(value);
                                break;
                        }
                    });
                }
                break;
            default:
                throw new UnknownAccessTokenTypeException("Unknown access accessToken type: " + contentType + "\nBody: " + response);
        }

        return token;
    }

    private String generateRequestBody() {
        ROPCState state = controller.getState();
        return String.format("grant_type=password&username=%s&password=%s&client_id=%s&client_secret=%s&scope=%s",
                state.username, state.password, state.clientID, state.clientSecret, state.scope);
    }

    @Override
    public OAuth2Token getAccessToken() throws Exception {
        return null;
    }

    @Override
    public void setState(OAuth2FlowState state) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getAuthHeader() throws Exception {
        return null;
    }
}

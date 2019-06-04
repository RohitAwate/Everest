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
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

/**
 * Authorization provider for OAuth 2.0's Resource Owner Password Credentials flow.
 * Makes requests to access token endpoints and returns either the final
 * 'Authorization' header or an ROPCToken object.
 */
public class ROPCProvider implements OAuth2Provider {
    private ROPCController controller;

    public ROPCProvider(ROPCController controller) {
        this.controller = controller;
    }

    private void fetchAccessToken(RequestType type) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(controller.getState().accessTokenURL))
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString(generateRequestBody(type)))
                .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        ROPCState state = controller.getState();

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        try {
            state.accessToken = parseTokenResponse(response.body(), contentType);
        } catch (UnknownAccessTokenTypeException | IOException e) {
            e.printStackTrace();
        }
    }

    private ROPCToken parseTokenResponse(String response, String contentType)
            throws UnknownAccessTokenTypeException, IOException {
        ROPCToken token;
        if (contentType.startsWith(MediaType.APPLICATION_JSON)) {
            token = EverestUtilities.jsonMapper.readValue(response, ROPCToken.class);
        } else if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
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
        } else {
            throw new UnknownAccessTokenTypeException("Unknown access token type: " + contentType + "\nBody: " + response);
        }

        return token;
    }

    /**
     * Represents the type of request.
     * Used to generate the appropriate body.
     */
    private enum RequestType {
        // While issuing new tokens
        NEW_TOKEN,

        // While issuing a token using a refresh token
        REFRESH_TOKEN
    }

    private String generateRequestBody(RequestType type) {
        ROPCState state = controller.getState();

        if (type == RequestType.NEW_TOKEN) {
            return String.format("grant_type=password&username=%s&password=%s&client_id=%s&client_secret=%s&scope=%s",
                    state.username, state.password, state.clientID, state.clientSecret, state.scope);
        } else if (type == RequestType.REFRESH_TOKEN) {
            return String.format("grant_type=refresh_token&refresh_token=%s&client_id=%s&scope=%s",
                    state.accessToken.getRefreshToken(), state.clientID, state.scope);
        }

        return "";
    }

    @Override
    public ROPCToken getAccessToken() throws Exception {
        ROPCState state = controller.getState();

        if (state.accessToken.getRefreshToken().isBlank()) {
            fetchAccessToken(RequestType.NEW_TOKEN);
        } else {
            fetchAccessToken(RequestType.REFRESH_TOKEN);
        }

        return state.accessToken;
    }

    @Override
    public void setState(OAuth2FlowState state) {

    }

    @Override
    public boolean isEnabled() {
        return controller.getState().enabled;
    }

    @Override
    public String getAuthHeader() throws Exception {
        ROPCState state = controller.getState();
        if (state.accessToken.getAccessToken().isBlank()) {
            getAccessToken();
        }

        String headerPrefix;
        if (state.headerPrefix == null || state.headerPrefix.isEmpty()) {
            headerPrefix = "Bearer";
        } else {
            headerPrefix = state.headerPrefix;
        }

        return String.format("%s %s", headerPrefix, state.accessToken.getAccessToken());
    }
}

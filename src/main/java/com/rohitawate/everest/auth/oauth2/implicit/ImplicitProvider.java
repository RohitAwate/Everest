package com.rohitawate.everest.auth.oauth2.implicit;

import com.rohitawate.everest.auth.captors.AuthorizationGrantCaptor;
import com.rohitawate.everest.auth.captors.BrowserCaptor;
import com.rohitawate.everest.auth.captors.WebViewCaptor;
import com.rohitawate.everest.auth.oauth2.OAuth2Provider;
import com.rohitawate.everest.auth.oauth2.tokens.ImplicitToken;
import com.rohitawate.everest.controllers.auth.oauth2.AuthorizationCodeController.CaptureMethod;
import com.rohitawate.everest.controllers.auth.oauth2.ImplicitController;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.state.auth.ImplicitState;
import com.rohitawate.everest.state.auth.OAuth2FlowState;

public class ImplicitProvider implements OAuth2Provider {
    private ImplicitController controller;
    private ImplicitState state;

    public ImplicitProvider(ImplicitController controller) {
        this.controller = controller;
    }

    @Override
    public ImplicitToken getAccessToken() throws Exception {
        if (this.state == null) {
            setState(controller.getState());
        }

        StringBuilder builder = new StringBuilder(state.authURL);
        builder.append("?response_type=token");
        builder.append("&client_id=");
        builder.append(state.clientID);
        builder.append("&redirect_uri=");
        builder.append(EverestUtilities.encodeURL(state.redirectURL));

        if (state.scope != null && !state.scope.isBlank()) {
            builder.append("&scope=");
            builder.append(state.scope);
        }

        if (state.state != null && !state.state.isBlank()) {
            builder.append("&state=");
            builder.append(state.state);
        }

        AuthorizationGrantCaptor captor;
        String captureKey = "access_token";
        switch (state.captureMethod) {
            case CaptureMethod.WEB_VIEW:
                captor = new WebViewCaptor(builder.toString(), captureKey);
                break;
            default:
                captor = new BrowserCaptor(builder.toString(), captureKey);
        }

        state.accessToken = parseToken(captor.getRedirectedURL());

        // This will display the new AuthCodeToken in the UI
        controller.setAccessToken(state.accessToken);

        return state.accessToken;
    }

    private static ImplicitToken parseToken(String redirectURL) {
        String pair[] = redirectURL.split("#");

        if (pair.length != 2) {
            return null;
        }

        ImplicitToken token = new ImplicitToken();
        String paramPairs[] = pair[1].split("&");
        for (String paramPair : paramPairs) {
            pair = paramPair.split("=");
            if (pair.length == 2) {
                switch (pair[0]) {
                    case "access_token":
                        token.setAccessToken(pair[1]);
                        break;
                    case "token_type":
                        token.setTokenType(pair[1]);
                        break;
                    case "expires_in":
                        token.setExpiresIn(Integer.parseInt(pair[1]));
                        break;
                    case "state":
                        token.setState(pair[1]);
                        break;
                    case "scope":
                        token.setScope(pair[1]);
                        break;
                }
            }
        }

        return token;
    }

    @Override
    public String getAuthHeader() throws Exception {
        return null;
    }

    @Override
    public void setState(OAuth2FlowState state) {
        if (state == null) {
            this.state = null;
            return;
        }

        this.state = (ImplicitState) state;

        if (this.state.redirectURL.isEmpty() || this.state.captureMethod.equals(CaptureMethod.BROWSER)) {
            this.state.redirectURL = BrowserCaptor.LOCAL_SERVER_URL;
        }

        if (state.headerPrefix == null || state.headerPrefix.isEmpty()) {
            state.headerPrefix = "Bearer";
        }
    }

    @Override
    public boolean isEnabled() {
        // Checking if there has been a change in the state of the enabled checkbox
        setState(controller.getState());
        return state.enabled;
    }

    public void setController(ImplicitController controller) {
        this.controller = controller;
    }
}
